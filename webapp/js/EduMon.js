EduMon = new function() {
	var that = this;

	this.debugging = true; //show debug messages in javascript console

	/* EduMon startup */
	this.init = function(){
		that.debug("*** All Glory to the EduMon! ***");
		that.debug("EduMon awakening...");
		that.messenger = new EduMon.Messenger(handleIncomingData);

		this.Prefs.rooms.push(new EduMon.Data.Room("160C",5,5));
		this.Prefs.students.push(new EduMon.Data.Student("Max Mustermann","Mustergruppe"));
		this.Prefs.courses.push(new EduMon.Data.Course("DevCourse",[0]));
		this.Prefs.lectures.push(new EduMon.Data.Lecture("DevLecture",0,0));

		this.Prefs.currentLecture = EduMon.Data.createCurrentLecture(0);

		this.Timeline.init();

		this.Analytics = new EduMon.Analytics();
	};


	/* Debug output to JS console */
	this.debug = function(msg){
		if (that.debugging){
			console.log(msg);
		}
	};


	/* Handle incoming data */
	var handleIncomingData = function(event){
		//Process packets received by message server
		if ("inbox" in event && event.inbox.length > 0){
			for (var i = 0; i < event.inbox.length; ++i) {
				processPacket(event.inbox[i]);
			}
		}

		//Handle any errors
		if ("errorMessages" in event && event.errorMessages.length > 0){
			for (i = 0; i < event.errorMessages.length; ++i) {
				that.debug(event.errorMessages[i]);
			}
		}
	};


	/* Process packet */
	var processPacket = function(packet){
		console.log(packet);
		var packetType = parseInt(packet.type);
		switch (packetType) {
			case 2:
				/*
					client sends name and seat
					body: { name: "..", seat: {x: 4, y: 1}}
				 */
				// todo do fancy things
				break;

			case 4:
				/*
					client sends data to server
					body: { keys: 69, mdist: 999, mclicks: 23, volume: 0.42 }
				 */
				EduMon.Analytics.processData(packet.from, packet.time, packet.body);
				break;

			case 6:
				/*
					Client sends feedback
					body: { id: 123, value: 0.69 }
				 */
				EduMon.Analytics.processFeedback(packet.from, packet.body);
				break;

			case 8:
				/*
					Client asks for break
					body: {}
				 */
				// todo @Niko: Break Request
				break;

			default:
				console.error('Unknown packet type ' + packetType + ' received.');
		}
	};


	/* [DEV] Send demo packet */
	this.sendDemo = function(typenumber){
		jQuery.getJSON("js/demoDataOut.json",function(data){
			that.sendPacket(data["demo_type_"+typenumber]);
		});
	};


	/* Queue packet for sending */
	this.sendPacket = function(packet){
		that.messenger.sendEvent(packet);
	};
};
