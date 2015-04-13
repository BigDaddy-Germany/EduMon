EduMon.Feedback = new function() {
	var that = this;

	/**
	 * Sends out a thumb feedback request
	 * @method requestThumbFeedback
	 * @param {String} type Feedback type "thumb" | "rating"
	 * @return packet Copy of the sent request packet
	 */
	this.requestFeedback = function(type){
		$("#btnThumbs").addClass("disabled");
		setInterval(function(){$("#btnThumbs").removeClass("disabled");},5000);

		var analytics = EduMon.Prefs.currentLecture.analytics;
		var feedbackId = analytics.nextFeedbackId++;

		var packet = EduMon.Data.createBasePacket(5,"BROADCAST",{"id": feedbackId, "type": type});
		analytics.studentFeedback[feedbackId] = {
			"type": type,
			"time": packet.time,
			"currentAverage": 0,
			"studentVoting": {}
		};

		EduMon.sendPacket(packet);
		EduMon.Gui.showToast("Daumenfeedback gestartet!");

		return packet;
	};
};
