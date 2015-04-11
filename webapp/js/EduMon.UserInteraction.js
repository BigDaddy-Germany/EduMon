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
     * Executes the given dialog function returning a promise and
     * returns both the data persisted before and the new calculated data
     * @param {Object} persistedData the data persisted before
     * @param {Function} dialogOpener the function to open the dialog
     * @param {Array} arguments all parameters to call the function
     * @return {Promise} fulfill gets both the persisted data and the new calculated data as an array
     */
    this.openStackedDialog = function(persistedData, dialogOpener, arguments) {
        var newArguments = arguments.slice(2);

        return new Promise(function(fulfill, reject) {
            dialogOpener.call(newArguments)
                .then(function(data) {
                    fulfill([persistedData, data]);
                })
                .catch(function(data) {
                    reject([persistedData, data]);
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
        if (lastOpenedDialog) {
            gui.switchDialog(lastOpenedDialog);
        } else {
            gui.closeDialog();
        }
    };


    /**
     * Opens a given dialog and executes the value calculator before closing it
     * @param {String} dialogId the ID to open the dialog
     * @param {Function} valueCalculator a function to calculate the values, the user entered
     * @return {Promise} fulfill gets the calculated values
     */
    this.promisingDialog = function(dialogId, valueCalculator) {
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
                    $('#dialogBtnBack')
                        .off('click')
                        .on('click', function() {
                            reject('Action aborted by user');
                            switchOrClose(lastOpenedDialog);
                        });

                    $('#dialogBtnSubmit')
                        .off('click')
                        .on('click', function() {
                            var values = valueCalculator();

                            switchOrClose(lastOpenedDialog);

                            fulfill(values);
                        });
                })
                .catch(reject);
        });
    };

};