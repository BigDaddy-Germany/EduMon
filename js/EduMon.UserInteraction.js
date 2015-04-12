/*
    Depends on:
        - EduMon.Gui
 */


/**
 * This class performs the interaction with the user via pop ups etc.
 * E.g. to import users via a CSV file or to add new rooms/courses etc.
 */
EduMon.UserInteraction = new function() {
    var gui = EduMon.Gui;
    var that = this;


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
            var oldDialog = $('#dialogContainer').clone(true, true);
            dialogOpener.call(dialogOpener, newArguments)
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
                    fulfill(EduMon.CSV.parseCsv(data['csvContent'], data['csvSeparator'], data['csvDelimiter']));
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

        };

        // checks, that names are unique
        var validator = function(values) {
            if (values.name == '') {
                return 'Every course must get a name.';
            }
            if (values.students.length == 0) {
                return 'At least one student is needed.';
            }
            var givenNames = [];
            var duplicate = false;
            var memberName = $('.courseMemberName');
            memberName.each(function() {
                var thisName = $(this).val();
                if (givenNames.indexOf(thisName) != -1) {
                    duplicate = true;
                }
                givenNames.push(thisName);
            });

            if (duplicate) {
                return 'Names of students have to be unique.';
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
        // todo implement me
        return new Promise();
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
     * @return {Promise} fulfill gets the calculated values
     */
    this.promisingDialog = function(dialogId, valueCalculator, initializer, validator) {
        validator = validator || function() { return true; };

        return new Promise(function(fulfill, reject) {
            var lastOpenedDialog = gui.getOpenedDialog();
            var promise;
            if (!lastOpenedDialog) {
                promise = gui.showDialog(dialogId);
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
                })
                .catch(reject);
        });
    };

};