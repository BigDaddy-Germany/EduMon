EduMon.Feedback = new function() {
	var that = this;

	var actionTimer = undefined;

	/**
	 * Sends out a thumb feedback request
	 *
	 * @param {string} type Feedback type "thumb" | "rating"
	 * @return {Object} packet Copy of the sent request packet
	 */
	this.requestFeedback = function(type) {
		var buttonId = "#btnRating, #btnThumbs"; //(type==="rating" ? "#btnRating" : "#btnThumbs");
		$(buttonId).addClass("disabled");
		setInterval(function() {
			$(buttonId).removeClass("disabled");
		}, 10000); //cooldown 10 seconds

		var analytics = EduMon.Prefs.currentLecture.analytics;
		analytics.currentFeedbackId = analytics.nextFeedbackId++;

		var packet = EduMon.Data.createBasePacket(5, "BROADCAST", {
			id: analytics.currentFeedbackId,
			type: type
		});
		analytics.studentFeedback[analytics.currentFeedbackId] = {
			type: type,
			time: packet.time,
			currentAverage: 0,
			studentVoting: {}
		};

		EduMon.sendPacket(packet);
		EduMon.Gui.showToast((type === "rating" ? "Abstimmung" : "Daumenfeedback") + " gestartet!");
		EduMon.Gui.openPultUpMode(type, that.updateFeedback);

		return packet;
	};


	/**
	 * Starts (and optionally resets) the action timer in the pult-up display
	 *
	 * @param {boolean} reset Execute time reset (e.g. use false to restore app state)
	 */
	this.restartActionTimer = function(reset) {
		if (reset) {
			EduMon.Prefs.currentLecture.gui.actionTime = -1;
		}

		if (typeof actionTimer !== 'undefined') {
			clearInterval(actionTimer);
		}

		var incrementor = function() {
			if (!EduMon.lectureIsActive()) {
				clearInterval(actionTimer);
				return;
			}
			var seconds = ++EduMon.Prefs.currentLecture.gui.actionTime;
			var minutes = (seconds - (seconds % 60)) / 60;
			seconds -= minutes * 60;
			seconds = ("0" + seconds).slice(-2);
			$("#pultup").find(".stats .time .value").text(minutes + ":" + seconds);
		};
		actionTimer = setInterval(incrementor, 1000);
		incrementor();
	};


	this.updateFeedback = function() {
		var analytics = EduMon.Prefs.currentLecture.analytics;
		var feedback = analytics.studentFeedback[analytics.currentFeedbackId];
		if (feedback === undefined) {
			return;
		}

		var numAnswers = EduMon.Util.countFields(feedback.studentVoting);
		var numOnline = EduMon.Util.countFields(EduMon.Prefs.currentLecture.activeStudents);
		$("#pultup").find(".stats .participation .value").text(numAnswers + "/" + numOnline);

		if (feedback.type === "rating") {
			that.updateRating(feedback.currentAverage);
		} else {
			that.updateThumbs(feedback.currentAverage);
		}
	};


	/**
	 * Update the feedback thumbs and percentage display
	 *
	 * @param {number} voting How good the feedback is (average), 0 = shitty to 1 = awesome
	 */
	this.updateThumbs = function(voting) {
		var degrees = Math.round((1 - voting) * 180);
		var percent = Math.round(voting * 100);

		var $thumb = $('#pultup').find('.feedback .thumb');
		$thumb.find("img:first-of-type").css("transform", "rotate(" + degrees + "deg)");
		$thumb.find("img:last-of-type").css("transform", "rotate(-" + degrees + "deg) scaleX(-1)");
		$thumb.find(".value").text(percent + "%");
	};


	/**
	 * Update star rating
	 *
	 * @param {number} voting How good the feedback is (average), 0 = shitty to 1 = awesome
	 */
	this.updateRating = function(voting) {
		var percent = Math.round(voting * 100);
		var stars = Math.round(voting * 5);

		var $rating = $('#pultup').find('.feedback .rating');
		for (var i = 1; i <= 5; i++) {
			var fillStar = (i <= stars);
			$rating.find("i:nth-of-type(0n+" + i + ")").toggleClass("glyphicon-star", fillStar).toggleClass("glyphicon-star-empty", !fillStar);
		}
		$rating.find(".value").text(percent + "%");
	};


};
