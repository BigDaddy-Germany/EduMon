/*
 Depends on
 - EduMon.Prefs
 - EduMon.Math
 */
EduMon.Analytics = function () {

	var debugging = false;
	var util = EduMon.Util;
	var math = EduMon.Math;

	var currentLecture = EduMon.Prefs.currentLecture;
	var analytics = EduMon.Prefs.currentLecture.analytics;

	var micNormalizationPeriod = 60 * 10;
	var micMinimumEntries = 10;
	var curValPeriod = 10;
	var minimalGlobalReferenceValues = debugging ? 2 : 5;

	var upperBoundGiniFactor = 0.8;

	var weights = {
		microphone: 3,
		keyboard: 3,
		mouseDistance: 3,
		mouseClicks: 1
	};

	var fieldMapping = {
		keys: 'keyboard',
		mdist: 'mouseDistance',
		mclicks: 'mouseClicks',
		volume: 'microphone'
	};


	/**
	 * Processes data sent by the client
	 * @param {String} sender the sender
	 * @param {int} time the time
	 * @param {Object} data the body
	 */
	this.processData = function (sender, time, data) {

		currentLecture = EduMon.Prefs.currentLecture;
		analytics = EduMon.Prefs.currentLecture.analytics;

		/*
		 Packet Body format
		 body: { keys: 69, mdist: 999, mclicks: 23, volume: 0.42 }
		 */

		if (!currentLecture.activeStudents[sender]) {
			return;
		}
		var student = currentLecture.activeStudents[sender];

		// create new history entry for given set of data
		var historyEntry = {time: time};
		util.forEachField(data, function (key, value) {
			if (fieldMapping[key]) {
				historyEntry[fieldMapping[key]] = value;
			}
		});
		// todo better way?
		student.history = student.history || [];
		student.history.push(historyEntry);

		student.micHistory = student.micHistory || [];
		student.micHistory.push({
			time: time,
			value: historyEntry.microphone
		});

		// remove old history entry (and the new one, if it is too old)
		student.history = truncateHistory(student.history);
		student.micHistory = truncateHistory(student.micHistory, true);


		var currentValues = getAverageValueOfHistory(student.history);
		currentValues.microphone = getNormalizedMicValue(currentValues.microphone, student.micHistory);

		if (!currentValues.microphone) {
			delete currentValues.microphone;
		}

		analytics.globalReferenceValues[sender] = currentValues;

		calculateAllDisturbances();
	};


	/**
	 * Calculates a percentage with custom scaling out of the given disturbance index
	 * @param {number} disturbanceIndex The given disturbance index
	 * @return {number} the calculated percentage
	 */
	this.scaleDisturbanceToPercentage = math.linearIntervalFunction(
		[0, 0],
		[3, 0.05],
		[6, 0.4],
		[7, 0.7],
		[8, 0.95],
		[10, 1]
	);


	/**
	 * Processes a feedback gotten by the user and calculates the new average
	 * @param {String} sender the sender's session id
	 * @param {Object} data the feedback package's body
	 */
	this.processFeedback = function (sender, data) {
		/*
		 Package body format:
		 body: { id: 123, value: 0.69 }
		 */
		var feedback = analytics.feedbackValues[data.id];
		if (!feedback) {
			return;
		}

		feedback.studentVoting[sender] = data.value;

		// calculate average again
		var values = [];
		util.forEachField(feedback.studentVoting, function (key, value) {
			values.push(value);
		});

		feedback.currentAverage = math.arithmeticAverage(values);
	};


	/**
	 * Calculates the disturbance for all active students, if there are at least
	 * minimalGlobalReferenceValues entries in minimalGlobalReference
	 *
	 * This is really fancy magic including but not limited to unicorns :)
	 */
	var calculateAllDisturbances = function () {
		var setsOfValues = {};
		var averageValues = {};
		var minimumValues = {};
		var maximumValues = {};

		// iterate over all senders to get minimum, maximum average of each property
		util.forEachField(analytics.globalReferenceValues, function (sender, referenceValue) {
			util.forEachField(referenceValue, function (propertyName, value) {
				if (setsOfValues[propertyName]) {
					setsOfValues[propertyName].push(value);
				} else {
					setsOfValues[propertyName] = [value];
				}
			});
		});


		// functions to scale user later go here
		var scales = {};

		// iterate over properties to calculate scaling function
		util.forEachField(setsOfValues, function (propertyName, values) {
			// only calculate the index, if the minimal number is reached
			if (values.length >= minimalGlobalReferenceValues) {

				averageValues[propertyName] = math.arithmeticAverage(values);
				minimumValues[propertyName] = math.min(values);
				maximumValues[propertyName] = math.max(values);

				var lowerLimit = Math.min(minimumValues[propertyName] + 0.3 * averageValues[propertyName], averageValues[propertyName] * 0.99);
				var upperLimit = averageValues[propertyName] * 1.9;

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
					return function (x) {
						if (x < lowerLimit) {
							return 0;
						}
						if (x < upperLimit) {
							return math.linearIntervalFunction(
								[lowerLimit, 0],
								[averageValue, 5],
								[upperLimit, 10]
							)(x);
						}
						return 10;
					};
				}

				scales[propertyName] = scaleFunctionCreator(
					lowerLimit,
					averageValues[propertyName],
					upperLimit
				);
			}
		});


		// finally, iterate over all senders again to rate them per property and calculate final index
		util.forEachField(analytics.globalReferenceValues, function (sender, senderValue) {
			var theReallyFinalIndex = 0;
			var sumPropertyWeights = 0;

			// iterate over properties to calculate final rating now
			util.forEachField(weights, function (propertyName, weight) {
				if (senderValue[propertyName] && scales[propertyName]) {
					theReallyFinalIndex += weight * scales[propertyName](senderValue[propertyName]);
					sumPropertyWeights += weight;
				}
			});

			if (sumPropertyWeights > 0) {
				theReallyFinalIndex /= sumPropertyWeights;
				currentLecture.activeStudents[sender].disturbance = theReallyFinalIndex;
			}
		});

	};

	var truncateHistoryByCount = function (history, isMic) {
		var count = isMic ? micNormalizationPeriod : curValPeriod;

		return history.slice(Math.max(0, history.length - count));
	};


	/**
	 * Deletes old values out of the history, which are older than the time configured above
	 * @param {Array} history The sender's history to update
	 * @param {Boolean} [isMicHistory=false] should the microphone's history time be used?
	 */
	var truncateHistory = function (history, isMicHistory) {
		isMicHistory = isMicHistory || false;

		if (debugging) {
			return truncateHistoryByCount(history, isMicHistory);
		}

		var evaluationPeriod;
		if (!isMicHistory) {
			evaluationPeriod = curValPeriod;
		} else {
			evaluationPeriod = micNormalizationPeriod;
		}

		var newHistory = history.filter(function (historyEntry) {
			return historyEntry.time > Math.round((new Date().getTime()) / 1000) - evaluationPeriod;
		});


		/*
		 If it is not the microphone history, everything is okay
		 If it is the microphone history entry, we have to check, that at least the minimum number of
		 entries remain in the history
		 */
		if (!isMicHistory || newHistory.length >= micMinimumEntries) {
			return newHistory;
		} else {
			if (history.length >= micMinimumEntries) {
				return history.slice(history.length - micMinimumEntries);
			} else {
				return history;
			}
		}
	};


	/**
	 * Normalizes the given microphone value using the user's microphone history
	 * @param {number} micValue the given microphone value
	 * @param {Array} micHistory the sender's history of microphone values
	 * @return {number|Boolean} the normalized microphone value,
	 *         if micHistory contains at least micMinimumEntries entries, false otherwise
	 */
	var getNormalizedMicValue = function (micValue, micHistory) {
		// todo which algorithm to normalize the microphone value?
		// dummy algorithm: calculate average and set in relation to current value

		var historyAverage = 0;
		var historyCount = 0;

		micHistory.forEach(function (historyEntry) {
			if ($.isNumeric(historyEntry.value)) {
				historyAverage += historyEntry.value;
				++historyCount;
			}
		});

		if (historyCount < micMinimumEntries) {
			return false;
		}

		return micValue * historyCount / Math.max(historyAverage, 1);
	};


	/**
	 * Calculate the average value for each entry of the given history
	 * @param {Array} history the history
	 * @returns {Object} An history object containing the averages for every key
	 */
	var getAverageValueOfHistory = function (history) {
		var historyAverage = {};
		var historyCount = {};

		history.forEach(function (historyEntry) {
			util.forEachField(historyEntry, function (historyKey, value) {
				if (historyKey != 'time' && $.isNumeric(value)) {
					historyAverage[historyKey] = historyAverage[historyKey] + value || value;
					historyCount[historyKey] = historyCount[historyKey] + 1 || 1;
				}
			});
		});

		util.forEachField(historyAverage, function (historyKey) {
			historyAverage[historyKey] /= historyCount[historyKey];
		});

		return historyAverage;
	};
};
