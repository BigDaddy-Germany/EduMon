EduMon = new function() {
	var that = this;

	var util;

	var loginErrorCodes = {
		nameAlreadyUsed: 1 << 0,
		seatAlreadyUsed: 1 << 1,
		nameNotAvailable: 1 << 2,
		seatNotAvailable: 1 << 3
	};

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

		this.Gui.init();
		this.Timeline.init();

		this.Analytics = new EduMon.Analytics();
		util = EduMon.Util;
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
				 clients tries to log in
				 body: { name: 'Some Name', seat: {x: 2, y: 3} }
				 */
				processLogin(packet.from, packet.body);
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


	/**
	 * Tries to login a client by its name and its seat. If
	 * @param {String} sender the client's session id
	 * @param {Object} body the body sent with the package to the server
	 */
	var processLogin = function(sender, body) {
		var seat = body.seat;
		var name = body.name;

		var seatingPlan = EduMon.Prefs.currentLecture.seatingPlan;
		var activeStudents = EduMon.Prefs.currentLecture.activeStudents;
		var room = EduMon.Prefs.currentLecture.room;
		var course = EduMon.Prefs.currentLecture.course;

		var errorBits = new util.BitSet();

        var responsePacket = {
			type: 3,
			time: Math.round(new Date().getTime() / 1000),
			from: 'MODERATOR',
			to: sender,
			room: room.roomName,
			body: {}
		};

		// check state of chosen seat
		// if seat exists
		if (seat.x <= room.width && seat.y <= room.height) {
			seatingPlan[seat.x] = seatingPlan[seat.x] || [];

			var seatState = seatingPlan[seat.x][seat.y];
			// if seat is blocked
			if (seatState != undefined) {
				errorBits.set(loginErrorCodes.seatAlreadyUsed);
			}
		} else {
			errorBits.set(loginErrorCodes.seatNotAvailable);
		}


		// check state of chosen name
		var nameExists = false;
		var groupForName;
		course.students.forEach(function(studentId) {
			var allStudents = EduMon.Prefs.students[studentId];
            if (name == allStudents.studentName) {
				nameExists = true;
				groupForName = allStudents.group;
			}
		});

		if (nameExists) {
			var nameState;
			util.forEachField(activeStudents, function (sessionId, student) {
				if (student.studentName == name) {
					nameState = sessionId;
				}
			});
			if (nameState != undefined) {
				errorBits.set(loginErrorCodes.nameAlreadyUsed);
			}
		} else {
			errorBits.set(loginErrorCodes.nameNotAvailable);
		}



		// if seat and name are in use already, it's okay, if they are blocked by the client's session id
		if (errorBits.contain(loginErrorCodes.nameAlreadyUsed | loginErrorCodes.seatAlreadyUsed)) {
			if (nameState == seatState && seatState == sender) {
				errorBits.clear(loginErrorCodes.nameAlreadyUsed | loginErrorCodes.seatAlreadyUsed)
			}
		}

		// if error code is 0, user can be logged in
		if (errorBits.equals(0)) {
			activeStudents[sender] = activeStudents[sender] || {
				studentName: name,
				group: groupForName,
				seat: seat,
				disturbance: 0,
				history: [],
				micHistory: []
			};

			seatingPlan[seat.x] = seatingPlan[seat.x] || [];
			seatingPlan[seat.x][seat.y] = seatingPlan[seat.x][seat.y] || sender;
		}

		responsePacket.body.successCode = errorBits.bits;

		that.sendPacket(responsePacket);

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
