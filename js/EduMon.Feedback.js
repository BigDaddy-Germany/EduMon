EduMon.Feedback = new function() {
	var that = this;
	
	var actionTimer = undefined;

	/**
	 * Sends out a thumb feedback request
	 * @method requestThumbFeedback
	 * @param {String} type Feedback type "thumb" | "rating"
	 * @return packet Copy of the sent request packet
	 */
	this.requestFeedback = function(type){
		var buttonId = (type==="rating" ? "#btnRating" : "#btnThumbs");
		$(buttonId).addClass("disabled");
		setInterval(function(){$(buttonId).removeClass("disabled");},5000);

		var analytics = EduMon.Prefs.currentLecture.analytics;
		analytics.currentFeedbackId = analytics.nextFeedbackId++;

		var packet = EduMon.Data.createBasePacket(5,"BROADCAST",{"id": analytics.currentFeedbackId, "type": type});
		analytics.studentFeedback[analytics.currentFeedbackId] = {
			"type": type,
			"time": packet.time,
			"currentAverage": 0,
			"studentVoting": {}
		};

		EduMon.sendPacket(packet);
		EduMon.Gui.showToast((type==="rating" ? "Abstimmung" : "Daumenfeedback")+" gestartet!");
		EduMon.Gui.openPultUpMode(type,that.updateFeedback);

		return packet;
	};


	/**
	 * Starts (and optionally resets) the action timer in the pult-up display
	 * @method restartActionTimer
	 * @param {Boolean} reset Execute time reset (e.g. use false to restore app state)
	 * @return undefined
	 */
	this.restartActionTimer = function(reset){
		if (reset){
			EduMon.Prefs.currentLecture.gui.actionTime = -1;
		}

		if (typeof actionTimer !== 'undefined'){
			clearInterval(actionTimer);
		}

		var incrementor = function(){
			var seconds = ++EduMon.Prefs.currentLecture.gui.actionTime;
			var minutes = (seconds-(seconds%60))/60;
			seconds -= minutes*60;
			seconds = ("0"+seconds).slice(-2);
			$("#pultup .stats .time .value").text(minutes+":"+seconds);
		};
		actionTimer = setInterval(incrementor,1000);
		incrementor();
	};


	this.updateFeedback = function(){
		var analytics = EduMon.Prefs.currentLecture.analytics;
		var feedback = analytics.studentFeedback[analytics.currentFeedbackId];
		if (feedback===undefined){
			return;
		}

		var numAnswers = EduMon.Util.countFields(feedback.studentVoting);
		var numOnline = EduMon.Util.countFields(EduMon.Prefs.currentLecture.activeStudents);
		$("#pultup .stats .participation .value").text(numAnswers+"/"+numOnline);

		if (feedback.type==="rating"){
			that.updateRating(feedback.currentAverage);
		} else {
			that.updateThumbs(feedback.currentAverage);
		}
	};


	/**
	 * Update the feedback thumbs and percentage display
	 * @method updateThumbs
	 * @param {Float} voting How good the feedback is (average), 0 = shitty to 1 = awesome
	 * @return undefined
	 */
	this.updateThumbs = function(voting){
		var degrees = Math.round((1-voting)*180);
		var percent = Math.round(voting*100);

		$("#pultup .feedback .thumb img:first-of-type").css("transform","rotate("+degrees+"deg)");
		$("#pultup .feedback .thumb img:last-of-type").css("transform","rotate(-"+degrees+"deg) scaleX(-1)");
		$("#pultup .feedback .thumb .value").text(percent+"%");
	};


	/**
	 * Update star rating
	 * @method updateRating
	 * @param {Float} voting How good the feedback is (average), 0 = shitty to 1 = awesome
	 * @return undefined
	 */
	this.updateRating = function(voting){
		var percent = Math.round(voting*100);
		var stars = Math.round(voting*5);

		for(var i=1; i<=5; i++){
			var fillStar = (i<=stars);
			$("#pultup .feedback .rating i:nth-of-type(0n+"+i+")").toggleClass("glyphicon-star",fillStar).toggleClass("glyphicon-star-empty",!fillStar);
		}
		$("#pultup .feedback .rating .value").text(percent+"%");
	};


};
