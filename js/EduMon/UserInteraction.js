/*
    Depends on:
        - EduMon.Gui
 */

Promise = Promise || {};

/**
 * This class performs the interaction with the user via pop ups etc.
 * E.g. to import users via a CSV file or to add new rooms/courses etc.
 */
EduMon.UserInteraction = new function() {
    var gui = EduMon.Gui;
    var that = this;


    /**
     * Starts the lecture manager and returns the chosen lecture
     * @param {Boolean} [replaceStartByOk=false] Indicates, whether the start lecture button should be replaced by OK
     * @param {int} [selectedLectureId] If a lecture should be selected, the ID can be passed here
     * @return {Promise} fulfill gets the selected lecture ID
     */
    this.selectLecture = function(replaceStartByOk, selectedLectureId) {
        replaceStartByOk = replaceStartByOk || false;

        // get the selected lecture
        var valueCalculator = function() {
            var val = $('#startId').val();

            return val ? val.trim() : '';
        };

        // load all lectures and insert them into the select
        var initializer = function() {
            var lectureSelect = $('#startId');
            var lectures = EduMon.Prefs.lectures;

            for (var i = 0; i < lectures.length; i++) {
                if (lectures[i]) {
                    lectureSelect.append('<option value="' + i + '">' + lectures[i].lectureName + '</option>')
                }
            }

            if (replaceStartByOk) {
                $('#dialogBtnSubmit').html('OK');
            }

            if (selectedLectureId) {
                lectureSelect.find('option[value=' + selectedLectureId + ']').attr('selected', 'selected');
            }

            lectureSelect.sortSelectBox();
        };

        // check, whether the given lecture exists
        var validator = function(value) {
            if (!EduMon.Prefs.lectures[value]) {
                return 'Bitte wählen Sie eine gültige Vorlesung aus oder legen Sie diese an.';
            }
            return true;
        };

        return new Promise(function(fulfill, reject) {
            that.promisingDialog('lectureManager', valueCalculator, initializer, validator, true)
                .then(fulfill)
                .catch(reject);
        });
    };


    /**
     * Executes the given dialog function returning a promise
     * The previous dialog will be stashed and restored
     * @param {Function} dialogOpener the function to open the dialog
     * @param {... Array} [openerArguments] all parameters to call the function
     * @return {Promise} fulfill gets both the persisted data and the new calculated data as an array
     */
    this.openStackedDialog = function(dialogOpener, openerArguments) {
        var newArguments = Array.prototype.slice.call(arguments, 1);

        return new Promise(function(fulfill, reject) {
            var oldDialog = $('#dialogContainer').cloneWithSelectState(true, true);
            
            dialogOpener.apply(dialogOpener, newArguments)
                .then(function(data) {
                    $('#dialogContainer').replaceWith(oldDialog);
                    fulfill(data);
                })
                .catch(function(data) {
                    $('#dialogContainer').replaceWith(oldDialog);
                    reject(data);
                })
        });
    };


    /**
     * Opens a dialog to give the user the chance to insert CSV content and parses the given value
     * @return {Promise} fulfill gets the parsed CSV data as a nested Array
     */
    this.importCsvContent = function() {
        return new Promise(function(fulfill, reject) {
            that.simplePromisingFormDialog('csvImport', ['csvContent', 'csvSeparator', 'csvDelimiter'])
                .then(function(data) {
                    fulfill(EduMon.CSV.parse(data['csvContent'], data['csvSeparator'], data['csvDelimiter']));
                })
                .catch(reject);
        });
    };


    /**
     * Get updated data for new or existing course
     * @param {int} [courseId] if update, the course
     * @return {Promise} fulfill gets the updated course data
     */
    this.getCourseData = function(courseId) {

        // get the values out of the fields
        var valueCalculator = function() {
            var courseName = $('#courseName').val().trim();
            var students = [];

            $('.courseMemberLine').each(function() {
                var name = $(this).find('.courseMemberName').val().trim();

                if (name != '') {
                    students.push({
                        name: name,
                        group: $(this).find('.courseMemberGroup').val()
                    })
                }
            });

            return EduMon.Data.Course(courseName, students);
        };

        // load saved course and initialize input fields
        var initializer = function() {

            // get the course data out of the preferences
            var courseData = EduMon.Prefs.courses[courseId];
            if (!courseData) {
                return;
            }

            // set course name field
            $('#courseName').val(courseData.name);

            // add all fields, which are needed (one is there already)
            var line = $('.courseMemberLine');
            var lineHtml = line[0].outerHTML;
            for (var i = 0; i < courseData.students.length; i++) {
                if (i != 0) {
                    line.after(lineHtml);
                    line = line.next();
                }
                line.find('.courseMemberName').val(courseData.students[i].name);
                line.find('.courseMemberGroup').val(courseData.students[i].group);
            }

            // add new lines to the member form by clicking on the add button
            // set the delete listeners
            $('.courseMemberDelete').off('click').on('click', courseDeleteMember);

        };

        // checks, that names are unique
        var validator = function(values) {
            if (values.name == '') {
                return 'Jedem Kurs muss ein Name zugewiesen werden.';
            }
            if (values.students.length == 0) {
                return 'Jeder Kurs benötigt mindestens einen Studenten.';
            }
            var givenNames = [];
            var duplicate = false;
            values.students.forEach(function(student) {
                var thisName = student.name;
                if (givenNames.indexOf(thisName) != -1) {
                    duplicate = true;
                }
                givenNames.push(thisName);
            });

            if (duplicate) {
                return 'Zur Identifikation müssen die Namen der Studenten eindeutig sein.';
            }
            return true;
        };

        return new Promise(function(fulfill, reject) {
            that.promisingDialog('editCourse', valueCalculator, initializer, validator)
                .then(fulfill)
                .catch(reject);
        });
    };


    /**
     * Get updated data for new or existing lecture
     * @param {int} [lectureId] if update, the lecture ID
     * @return {Promise} fulfill gets the updated lecture data
     */
    this.getLectureData = function(lectureId) {

        // get the values out of the fields
        var valueCalculator = function() {
            var roomValue = $('#lectureRoom').val();
            var courseValue = $('#lectureCourse').val();

            var lectureName = $('#lectureName').val().trim();
            var lectureRoom = roomValue ? roomValue.trim() : '';
            var lectureCourse = courseValue ? courseValue.trim() : '';

            return EduMon.Data.Lecture(lectureName, lectureRoom, lectureCourse);
        };


        // load saved lecture and initialize input fields
        var initializer = function() {
            // fill the selects for room and course with options
            var roomSelect = $('#lectureRoom');
            var courseSelect = $('#lectureCourse');

            var rooms = EduMon.Prefs.rooms;
            var courses = EduMon.Prefs.courses;

            for (var i = 0; i < rooms.length; i++) {
                if (rooms[i]) {
                    roomSelect.append('<option value="' + i + '">' + rooms[i].roomName + '</option>');
                }
            }
            for (i = 0; i < courses.length; i++) {
                if (courses[i]) {
                    courseSelect.append('<option value="' + i + '">' + courses[i].name + '</option>');
                }
            }

            courseSelect.sortSelectBox();
            roomSelect.sortSelectBox();

            var lecture = EduMon.Prefs.lectures[lectureId];
            if (!lecture) {
                return;
            }

            $('#whoami').val(lectureId);

            $('#lectureName').val(lecture.lectureName);
            roomSelect.val(lecture.room);
            courseSelect.val(lecture.course);
        };


        // checks, that all fields are selected and room and course do exist
        var validator  = function(values) {
            if (values.lectureName == '') {
                return 'Der Name der Vorlesung darf nicht leer sein.';
            }
            if (!EduMon.Prefs.rooms[values.room]) {
                return 'Der ausgewählte Raum existiert nicht.';
            }
            if (!EduMon.Prefs.courses[values.course]) {
                return 'Die ausgewählte Kurs existiert nicht.';
            }
            return true;
        };

        return new Promise(function(fulfill, reject) {
            that.promisingDialog('editLecture', valueCalculator, initializer, validator)
                .then(fulfill)
                .catch(reject);
        });
    };


    /**
     * Get the room data updated by the user
     * @param {id} roomId the room's id
     * @return {Promise} fulfill gets the updated room data
     */
    this.getRoomData = function(roomId) {

        // get the values out of the fields
        var valueCalculator = function() {
            var roomName = $('#roomName').val().trim();
            var roomX = parseInt($('#roomX').val().trim());
            var roomY = parseInt($('#roomY').val().trim());

            return EduMon.Data.Room(roomName, roomX, roomY);
        };

        // load saved room and initialize fields
        var initializer = function() {
            var room = EduMon.Prefs.rooms[roomId];
            if (!room) {
                return;
            }

            $('#roomName').val(room.roomName);
            $('#roomX').val(room.width);
            $('#roomY').val(room.height);
        };

        // check, that all values are valid
        var validator = function(values) {
            if (values.roomName == '') {
                return 'Der Raum muss einen Namen tragen.';
            }
            if (isNaN(values.width) || isNaN(values.height) || values.width <= 0 || values.height <= 0) {
                return 'Die Höhe und Breite muss in positiven Zahlen angegeben werden.';
            }
            return true;
        };

        return new Promise(function(fulfill, reject) {
            that.promisingDialog('editRoom', valueCalculator, initializer, validator)
                .then(fulfill)
                .catch(reject);
        });
    };



    /**
     * Opens a dialog and returns a promise to get the values entered by the user
     * @param {String} dialogId the dialog's ID
     * @param {Array} formIds all form IDs to return their content
     * @return {Promise} fulfill will get a map from field IDs to their content
     */
    this.simplePromisingFormDialog = function(dialogId, formIds) {
        return new Promise(function(fulfill, reject) {
            var valueCalculator = function() {
                var values = {};
                formIds.forEach(function (formId) {
                    var formField = $('#' + formId);
                    if (formField) {
                        values[formId] = formField.val();
                    }
                });
                return values;
            };

            that.promisingDialog(dialogId, valueCalculator)
                .then(fulfill)
                .catch(reject);
        });
    };




    /**
     * Closes dialog, if lastOpenedDialog is undefined, otherwise switches to it
     * @param {String} lastOpenedDialog the dialog to switch to
     */
    var switchOrClose = function(lastOpenedDialog) {
        return new Promise(function(fulfill, reject) {
            if (lastOpenedDialog) {
                gui.switchDialog(lastOpenedDialog)
                    .then(fulfill)
                    .catch(reject);
            } else {
                gui.closeDialog();
                fulfill();
            }
        });
    };


    /**
     * Opens a given dialog and executes the value calculator before closing it
     * @param {String} dialogId the ID to open the dialog
     * @param {Function} valueCalculator a function to calculate the values, the user entered
     * @param {Function} [initializer] a function, which initializes the new opened dialog
     * @param {Function} [validator] a function, which validates the data before submitting it.
     *                      It returns true in case of success and an error message in case of failure
     *                      It gets the result of the valueCalculator
     * @param {Boolean} [noOtherDialogAllowed=false] If set to true, there is no opened dialog allowed
     * @return {Promise} fulfill gets the calculated values
     */
    this.promisingDialog = function(dialogId, valueCalculator, initializer, validator, noOtherDialogAllowed) {
        validator = validator || function() { return true; };
        noOtherDialogAllowed = noOtherDialogAllowed || false;

        return new Promise(function(fulfill, reject) {
            var lastOpenedDialog = gui.getOpenedDialog();
            var promise;
            if (!lastOpenedDialog || noOtherDialogAllowed) {
                promise = gui.showDialog(dialogId, true);
            } else {
                promise = gui.switchDialog(dialogId);
            }

            promise
                .then(function() {
                    if (initializer) {
                        initializer();
                    }
                    $('#dialogBtnBack')
                        .off('click')
                        .on('click', function() {
                            switchOrClose(lastOpenedDialog)
                                .then(function() {
                                    reject('Action aborted by user');
                                })
                                .catch(function() {
                                    reject('Action aborted by user');
                                });
                        });

                    $('#dialogBtnSubmit')
                        .off('click')
                        .on('click', function() {
                            var values = valueCalculator();
                            var status = validator(values);

                            if (status != true) {
                                gui.showPopup("Error", status, ['ok']);
                            } else {
                                switchOrClose(lastOpenedDialog)
                                    .then(function () {
                                        fulfill(values);
                                    })
                                    .catch(reject);
                            }
                        });
                    $('.smallBtn').css('cursor', 'pointer');
                })
                .catch(reject);
        });
    };


    this.checkAndDelete = function(resourceType, resourceId, domSelect) {
        // check, whether deletion is allowed
        var allowedUser = false;
        var whoAmI = $('#whoami').val();
        if (whoAmI != '') {
            allowedUser = whoAmI;
        }

        var deletionErrors = EduMon.Data.checkDeletionErrors(resourceType, resourceId, allowedUser);

        if (!deletionErrors) {
            var storage;
            var whereAmI;

            switch (resourceType) {
                case 'room':
                    storage = EduMon.Prefs.rooms;
                    whereAmI = EduMon.Prefs.lectures;
                    break;
                case 'course':
                    storage = EduMon.Prefs.courses;
                    whereAmI = EduMon.Prefs.lectures;
                    break;
                case 'lecture':
                    storage = EduMon.Prefs.lectures;
                    whereAmI = [];
                    break;
                default:
                    throw 'Unknown resource type.';
            }

            storage[resourceId] = undefined;
            domSelect.find('[value=' + resourceId + ']').remove();

            if (whereAmI[whoAmI] && whereAmI[whoAmI][resourceType] == resourceId) {
                whereAmI[whoAmI][resourceType] = storage.firstUsedElement();
            }


        } else {
            var errorMsg;
            switch(deletionErrors) {
                case 1:
                    errorMsg = 'Es muss mindestens eine Ressource existieren.';
                    break;
                case 2:
                    errorMsg = 'Diese Ressource wird noch verwendet.';
                    break;
                case 3:
                    errorMsg = 'Unbekannter Fehler.';
                    break;
            }

            EduMon.Gui.showPopup('Error', errorMsg, ['ok']);
        }
    }
};
