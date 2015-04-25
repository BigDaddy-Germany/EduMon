EduMon.Timeline = new function() {
	var that = this;

	var tick_interval = 2; //in seconds
	var tick_value = 60; //in seconds, set != tick_interval for non-realtime testing
	var timer = undefined;

	/**
	 * Get the current time in the format H:MM (e.g. "9:23")
	 *
	 * @return {string} Time in hours and minutes
	 */
	var getTime = function() {
		var now = new Date();
		return now.getHours() + ":" + ("0" + now.getMinutes()).slice(-2);
	};

	/**
	 * Set the end value of the previous (than the current) slice
	 */
	var setPreviousEnd = function() {
		var timeline = EduMon.Prefs.currentLecture.timeline;
		if (timeline.slices.length > 1) {
			timeline.slices[timeline.slices.length - 2].end = getTime();
		}
	};

	/**
	 * Clears the timeline, deletes timeline data and reinitializes timeline; forces start of new lecture as intended
	 * side effect
	 */
	this.reset = function() {
		if (timer !== undefined) {
			clearInterval(timer);
		}
		$("#progressdisplay").empty();
		$("#hourdisplay").empty();
		EduMon.Prefs.currentLecture.timeline = new EduMon.Data.Timeline();
		that.init();
	};

	/**
	 * Lecture-Flow-Control: start or resume a lecture block
	 */
	this.play = function() {
		var timeline = EduMon.Prefs.currentLecture.timeline;
		if (timeline.status !== "play") {
			timeline.slices.push({seconds: 0, type: "lecture"});
			setPreviousEnd();
			EduMon.Gui.showToast("Vorlesung läuft!");
			timeline.status = "play";
			timeline.start = getTime();
			timeline.started = true;
		} else throw "Timeline-Play nicht erlaubt, Timer läuft bereits";
		that.update();
	};

	/**
	 * Lecture-Flow-Control: pauses the current lecture block
	 */
	var pause = function() {
		var timeline = EduMon.Prefs.currentLecture.timeline;
		if (timeline.status === "play") {
			timeline.slices.push({seconds: 0, type: "break"});
			setPreviousEnd();
			EduMon.Gui.showToast("Vorlesung pausiert.");
			timeline.status = "pause";
			EduMon.Prefs.currentLecture.analytics.breakRequests = 0;
			EduMon.Util.forEachField(EduMon.Prefs.currentLecture.activeStudents, function(id) {
				EduMon.Prefs.currentLecture.activeStudents[id].sentBreakRequest = false;
			});
		} else throw "Timeline-Unterbrechung nicht erlaubt, Timer lief nicht";
		that.update();
	};

	/**
	 * Lecture-Flow-Control: completely stops the lecture
	 */
	var stop = function() {
		var timeline = EduMon.Prefs.currentLecture.timeline;
		EduMon.Gui.showPopup("Vorlesung beenden?", "Sind Sie sicher? Eine beendete Vorlesung kann nicht wieder aufgenommen werden.",
			["yes", "no"], function(result) {
				if (result === "yes") {
					clearInterval(timer);
					EduMon.Gui.showToast("Vorlesung beendet.");
					EduMon.Gui.togglePultup(false);
					timeline.status = "stop";
					timeline.ended = true;
					that.update();
					EduMon.stopLecture();
				}
			}, true);
	};

	/**
	 * Wrapper around reset (called by restart button)
	 */
	var restart = function() {
		//TODO intended?
		that.reset();
	};

	/**
	 * Timeline update timer tick
	 *
	 * @param {boolean} [onlyUpdate=true] Only refresh the timeline display but do not alter timeline data
	 */
	var tick = function(onlyUpdate) {
		if (typeof EduMon.Prefs.currentLecture.timeline !== "undefined") {
			var timeline = EduMon.Prefs.currentLecture.timeline;
			if (onlyUpdate !== true && EduMon.Prefs.currentLecture.timeline.status !== "stop") {
				var currentSlice = timeline.slices[timeline.slices.length - 1];
				currentSlice.seconds += tick_value;
				timeline.totalSeconds += tick_value;
			}
			that.update();
		}
	};

	/**
	 * Initialize time line module: start update timer and bind click handlers
	 */
	this.init = function() {
		//Timer is always active, but timer tick does not always trigger action
		//this prevents seconds getting lost
		timer = setInterval(tick, tick_interval * 1000);
		that.update();

		$("#btnPlay").off("click").click(function() {
			if (typeof EduMon.Prefs.currentLecture.timeline !== "undefined" && EduMon.Prefs.currentLecture.timeline.started) {
				that.play();
			} else {
				EduMon.lectureStartDialog();
			}
		});
		$("#btnPause").off("click").on('click', pause);
		$("#btnStop").off("click").on('click', stop);
		$("#btnRestart").off("click").on('click', restart);
	};

	/**
	 * Update the time line display and the flow controls
	 */
	this.update = function() {
		var timeline = EduMon.Prefs.currentLecture.timeline;
		var totalPercentage = 0; //remember how full the bar is
		var barPercentage;
		var isLastBar = false;
		var isFirstBar = false;

		for (var i = 0; i < timeline.slices.length; i++) { //refresh all slices
			isLastBar = (i == timeline.slices.length - 1);
			isFirstBar = (i == 0);

			//create new slice if necessary
			var progressDisplay = $("#progressdisplay");
			var hourDisplay = $("#hourdisplay");
			if (progressDisplay.children().length <= i) {
				//insert new slice in both timeline and hour display
				var barType = (timeline.slices[i].type === "lecture" ? "success" : "warning");
				progressDisplay.append($("<div/>").addClass("progress-bar progress-bar-" + barType));
				hourDisplay.append($("<span/>").addClass("hour"));

				if (timeline.slices[i].type === "lecture") { //when opening a new lecture slice, insert end time of the previous one as start
					var startTime = timeline.start;
					if (i > 0) {
						startTime = timeline.slices[i - 1].end;
					}
					hourDisplay.children().eq(i).prepend(
						$("<span/>").addClass("start").text(startTime)
					);
				}
				if (i > 0 && timeline.slices[i - 1].type === "lecture") { //when opening a new slice, insert end time of the previous lecture slice
					hourDisplay.children().eq(i - 1).append(
						$("<span/>").addClass("end").text(timeline.slices[i - 1].end)
					);
				}
			}

			//calculate percentage and add it up
			barPercentage = Math.floor(timeline.slices[i].seconds / timeline.totalSeconds * 100);
			totalPercentage += barPercentage;
			if (isLastBar) {
				//fill the space if we are at the last element and below 100 because of rounding
				barPercentage += 100 - totalPercentage;
			}

			//update slice widths
			progressDisplay.children().eq(i)
				.width(barPercentage + "%")
				.toggleClass("active", isLastBar && timeline.status !== "stop");
			hourDisplay.children().eq(i).css("width", barPercentage + "%");

			//update all texts
			var minutes = Math.floor(timeline.slices[i].seconds / 60);
			if (minutes > 0) {
				var hours = (minutes - (minutes % 60)) / 60;
				if (hours > 0) {
					minutes -= hours * 60;
					hours = hours + " h ";
				} else {
					hours = "";
				}
				progressDisplay.children().eq(i).text(hours + minutes + " min");
			}
		}

		//update clock and state display
		$("#currenttime").text(getTime());
		$("#currentstate").find("i")
			.removeClass("glyphicon-play glyphicon-pause glyphicon-stop")
			.addClass("glyphicon-" + timeline.status);

		//display fitting control buttons
		$("#btnPlay").toggle(timeline.status !== "play" && !timeline.ended);
		$("#btnPause").toggle(timeline.status === "play");
		$("#btnStop").toggle(timeline.status !== "stop");
		$("#btnRestart").toggle(timeline.ended);
	};
};
