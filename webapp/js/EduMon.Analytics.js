/*
    Depends on EduMon.Prefs
 */
EduMon.Analytics = new function() {

    var analytics = EduMon.Prefs.currentLecture.analytics;
    var activeStudents = EduMon.Prefs.currentLecture.activeStudents;
    var globalReferenceValues = analytics.globalReferenceValues;

    var micNormalizationPeriod = 60*5;
    var micMinimumEntries = 10;
    var curValPeriod = 5;
    var minimalGlobalReferenceValues = 5;

    var weights = {
        microphone: 1,
        keyboard: 1,
        mouseDistance: 1,
        mouseClicks: 1
    };

    /*
        Packet Body format
        body: { keys: 69, mdist: 999, mclicks: 23, volume: 0.42 }
     */

    /**
     * Processes data sent by the client
     * @param {int} sender the sender
     * @param {int} time the time
     * @param {Object} data the body
     */
    this.processData = function(sender, time, data) {
        var student = activeStudents[sender];

        student.history = truncateHistory(student.history);
        student.micHistory = truncateHistory(student.micHistory, true);


        var currentValues = getAverageValueOfHistory(student.history);
        currentValues.microphone = getNormalizedMicValue(currentValues.microphone, student.micHistory);

        if (!currentValues.microphone) {
            delete currentValues.microphone;
        }

        globalReferenceValues[sender] = currentValues;



    };


    /**
     * Calculates the disturbance for all active students, if there are at least
     * minimalGlobalReferenceValues entries in minimalGlobalReference
     */
    var calculateAllDisturbances = function() {
        var averageValues = {};
        var minimumValues = {};
        var maximumValues = {};
        var numberOfValues = {};

        for (var sender in analytics.globalReferenceValues) {
            if (analytics.globalReferenceValues.hasOwnProperty(sender)) {
                var referenceValue = analytics.globalReferenceValues[sender];
                for (var propertyName in referenceValue) {
                    if (referenceValue.hasOwnProperty(propertyName)) {
                        if (averageValues[propertyName]) {
                            // then every ...Value[propertyName] exists
                            averageValues[propertyName] += referenceValue[propertyName];
                            minimumValues[propertyName] = Math.min(minimumValues[propertyName], referenceValue[propertyName]);
                            maximumValues[propertyName] = Math.max(maximumValues[propertyName], referenceValue[propertyName]);
                            ++numberOfValues[propertyName];
                        } else {
                            averageValues[propertyName] = referenceValue[propertyName];
                            minimumValues[propertyName] = referenceValue[propertyName];
                            maximumValues[propertyName] = referenceValue[propertyName];
                            numberOfValues[propertyName] = 1;
                        }
                    }
                }
            }
        }


        for (propertyName in numberOfValues) {
            if (numberOfValues.hasOwnProperty(propertyName)) {

                // only calculate the index, if the minimal number is reached
                if (numberOfValues[propertyName] >= minimalGlobalReferenceValues) {
                    averageValues[propertyName] /= numberOfValues[propertyName];

                    // iterate over all them senders
                    for (sender in analytics.globalReferenceValues) {
                        if (analytics.globalReferenceValues.hasOwnProperty(sender)) {
                            var senderValues = analytics.globalReferenceValues[sender];

                        }
                    }
                }
            }
        }

    };

    /**
     * Interpolates a polynomial with Lagrange
     * @param {... Array} - The points to interpolate with Lagrange
     * @return {Function}
     */
    this.interpolatePolynomialByLagrange = function() {
        function createLagrangePolynomial(k, points) {
            return function (x) {
                var returnValue = 1;
                for (var i = 0; i < n; i++) {
                    if (i != k) {
                        returnValue *= (x - points[i][0]) / (points[k][0] - points[i][0]);
                    }
                }
                return returnValue;
            };
        }

        var points = arguments;
        var n = points.length;
        var lagrangePolynomials = [];

        for (var k = 0; k < n; k++) {
            lagrangePolynomials.push(createLagrangePolynomial(k, points));
        }

        return function(x) {
            var returnValue = 0;
            for (var i = 0; i < n; i++) {
                returnValue += points[i][1] * lagrangePolynomials[i](x);
            }
            return returnValue;
        };
    };


    /**
     * Deletes old values out of the history, which are older than the time configured above
     * @param {Array} history The sender's history to update
     * @param {Boolean} [isMicHistory=false] should the microphone's history time be used?
     */
    var truncateHistory = function(history, isMicHistory) {
        isMicHistory = isMicHistory || false;

        var evaluationPeriod;
        if (!isMicHistory) {
            evaluationPeriod = curValPeriod;
        } else {
            evaluationPeriod = micNormalizationPeriod;
        }

        var newHistory = history.filter(function(historyEntry) {
            return historyEntry.time > Math.round((new Date()) / 1000) - evaluationPeriod;
        });


        /*
            If it is not the history, everything is okay
            If it is the history entry, we have to check, that at least the minimum number of
            entries remain in the history
         */
        if (!isMicHistory) {
            return newHistory;
        } else {
            return newHistory.slice(newHistory.length-micMinimumEntries);
        }
    };


    /**
     * Normalizes the given microphone value using the user's microphone history
     * @param {number} micValue the given microphone value
     * @param {Array} micHistory the sender's history of microphone values
     * @return {number|Boolean} the normalized microphone value,
     *         if micHistory contains at least micMinimumEntries entries, false otherwise
     */
    var getNormalizedMicValue = function(micValue, micHistory) {
        // todo which algorithm to normalize the microphone value?
        // dummy algorithm: calculate average and set in relation to current value

        var historyAverage = 0;
        var historyCount = 0;

        micHistory.forEach(function(historyEntry) {
            historyAverage += historyEntry.value;
            ++historyCount;
        });

        if (historyCount == 0) {
            return false;
        }

        return micValue * historyCount / Math.max(historyAverage, 1);
    };



    /**
     * Calculates the average value of a given Array of numbers
     * @param {Array} values The values to calculate the average
     * @returns {number} The calculated average
     */
    var arithmeticAverage = function(values) {
        if (values.length == 0) {
            return 0;
        }
        var sum = 0;
        values.forEach(function(value) {
            sum += value;
        });
        return sum/values.length;
    };



    /**
     * Calculate the average value for each entry of the given history
     * @param {Array} history the history
     * @returns {Object} An history object containing the averages for every key
     */
    var getAverageValueOfHistory = function(history) {
        var historyAverage = {};
        var historyCount = {};

        history.forEach(function(historyEntry) {
            for (var historyKey in historyEntry) {
                if (historyEntry.hasOwnProperty(historyKey) && historyKey != 'time') {
                    historyAverage[historyKey] = historyAverage[historyKey] + historyEntry[historyKey]
                                                 || historyEntry[historyKey];

                    historyCount[historyKey] = historyCount[historyKey] + 1 || 1;
                }
            }
        });

        for (var historyKey in historyAverage) {
            if (historyAverage.hasOwnProperty(historyKey)) {
                historyAverage[historyKey] /= historyCount[historyKey];
            }
        }

        return historyAverage;
    };
};