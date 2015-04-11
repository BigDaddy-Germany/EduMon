/*
    Depends on
       - EduMon.Prefs
       - EduMon.Math
 */
EduMon.Analytics = new function() {

    var analytics = EduMon.Prefs.currentLecture.analytics;
    var activeStudents = EduMon.Prefs.currentLecture.activeStudents;
    var globalReferenceValues = analytics.globalReferenceValues;

    var micNormalizationPeriod = 60*5;
    var micMinimumEntries = 10;
    var curValPeriod = 5;
    var minimalGlobalReferenceValues = 3;
    
    var upperBoundGiniFactor = 0.8;

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
     *
     * This is really fancy magic including but not limited to unicorns :)
     */
    this.calculateAllDisturbances = function() {
        var setOfValues = {};
        var averageValues = {};
        var minimumValues = {};
        var maximumValues = {};

        // iterate over all senders to get minimum, maximum average of each property
        EduMon.Util.forEachField(analytics.globalReferenceValues, function(sender, referenceValue) {
            EduMon.Util.forEachField(referenceValue, function(propertyName, values) {
                if (setOfValues[propertyName]) {
                    setOfValues[propertyName].push(values);
                } else {
                    setOfValues[propertyName] = [values];
                }
            });
        });


        // functions to scale user later go here
        var scales = {};

        // iterate over properties to calculate scaling function
        EduMon.Util.forEachField(setOfValues, function(propertyName, values) {
            // only calculate the index, if the minimal number is reached
            if (values.length >= minimalGlobalReferenceValues) {

                averageValues[propertyName] = EduMon.Math.arithmeticAverage(values);
                minimumValues[propertyName] = EduMon.Math.min(values);
                maximumValues[propertyName] = EduMon.Math.max(values);

                // get function to scale student in a really fancy way with lagrange :)
                var upperLimit = (1-EduMon.Math.giniIndex(values)) * maximumValues[propertyName] * upperBoundGiniFactor;
                upperLimit = Math.max(upperLimit, maximumValues[propertyName]);

                /**
                 * Creates the function to access the variables inside the closure
                 * If the given x is lower than the upper limit, the interpolated function will be called
                 * If the given x is higher or equal to the upper limit, 10 will be returned
                 * @param lowerLimit the function's lower limit
                 * @param averageValue the function's average value
                 * @param upperLimit the function's upper limit
                 * @return {Function} the generated function
                 */
                function scaleFunctionCreator(lowerLimit, averageValue, upperLimit) {
                    return function(x) {
                        if (x < upperLimit) {
                            var returnValue = EduMon.Math.interpolatePolynomialByLagrange(
                                [lowerLimit, 1],
                                [averageValue, 5],
                                [upperLimit, 10]
                            )(x);

                            return EduMon.Math.log(returnValue)*10;
                        }
                        return 10;
                    };
                }

                scales[propertyName] = scaleFunctionCreator(
                    minimumValues[propertyName],
                    averageValues[propertyName],
                    maximumValues[propertyName]
                );
            }
        });


        // finally, iterate over all senders again to rate them per property and calculate final index
        EduMon.Util.forEachField(analytics.globalReferenceValues, function(sender, senderValue) {
            var theReallyFinalIndex = 0;
            var sumPropertyWeights = 0;

            // iterate over properties to calculate final rating now
            EduMon.Util.forEachField(weights, function(propertyName, weight) {
                theReallyFinalIndex += weight * scales[propertyName](senderValue[propertyName]);
                sumPropertyWeights += weight;
            });

            theReallyFinalIndex /= sumPropertyWeights;


            activeStudents[sender].disturbance = theReallyFinalIndex;
        });

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
     * Calculate the average value for each entry of the given history
     * @param {Array} history the history
     * @returns {Object} An history object containing the averages for every key
     */
    var getAverageValueOfHistory = function(history) {
        var historyAverage = {};
        var historyCount = {};

        history.forEach(function(historyEntry) {
            EduMon.Util.forEachField(historyEntry, function(historyKey, value) {
                if (historyKey != 'time') {
                    historyAverage[historyKey] = historyAverage[historyKey] + value || value;
                    historyCount[historyKey] = historyCount[historyKey] + 1 || 1;
                }
            });
        });

        EduMon.Util.forEachField(historyAverage, function(historyKey) {
            historyAverage[historyKey] /= historyCount[historyKey];
        });

        return historyAverage;
    };
};