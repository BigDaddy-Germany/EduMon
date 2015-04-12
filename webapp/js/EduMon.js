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

		this.Prefs.rooms.push(new EduMon.Data.Room("160C",6,4));
		this.Prefs.courses.push(new EduMon.Data.Course("DemoCourse",[
					new EduMon.Data.Student("Max Mustermann","Gruppe 1"),
					new EduMon.Data.Student("Anna Mustermann","Gruppe 1"),
					new EduMon.Data.Student("Lieschen Müller","Gruppe 2"),
					new EduMon.Data.Student("Arno Nymous","Gruppe 2")
					]));
		this.Prefs.lectures.push(new EduMon.Data.Lecture("DemoLecture",0,0));

		this.Prefs.currentLecture = EduMon.Data.createCurrentLecture(0);

		//UI preparations
		this.Timeline.init();
		this.Gui.init();

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
				processBreakRequest()
				break;

			default:
				console.error('Unknown packet type ' + packetType + ' received.');
		}
	};

	var processBreakRequest = function(){
		if (EduMon.Prefs.currentLecture.timeline.status==="play"){
			var analytics = EduMon.Prefs.currentLecture.analytics;
			var numStudents = EduMon.Util.countFields(EduMon.Prefs.currentLecture.activeStudents);
			analytics.breakRequests++;

			var style = "info";
			if (analytics.breakRequests>(numStudents*0.2)) style = "warning";
			if (analytics.breakRequests>(numStudents*0.5)) style = "danger";

			EduMon.Gui.showFeedMessage(style,"Pausenanfrage",
					(analytics.breakRequests>1 ? analytics.breakRequests+" Teilnehmer bitten" : "Ein Teilnehmer bittet")+" um eine Pause."
					);
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
		course.students.forEach(function(student) {
            if (name == student.name) {
				nameExists = true;
				groupForName = student.group;
			}
		});

		if (nameExists) {
			var nameState;
			util.forEachField(activeStudents, function (sessionId, student) {
				if (student.name == name) {
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
				name: name,
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


	/**
	 * Broadcasts the current lecture (room dimensions + list of names)
	 * @returns {Object} the sent packet
	 */
	var broadcastCurrentLecture = function(){
		var room = EduMon.Prefs.currentLecture.room;
		var body = {
			"names": [],
			"room": room.roomName,
			"dimensions": {"width": room.width, "height": room.height}
		};
		var students = EduMon.Prefs.currentLecture.course.students;
		for (var i=0; i<students.length; i++){
			body.names.push(students[i].name);
		}

		var packet = EduMon.Data.createBasePacket(1,"BROADCAST",body);
		that.sendPacket(packet);
		return packet;
	};

	this.lectureStartDialog = function() {
		EduMon.UserInteraction.selectLecture()
			.then(function(lectureId) {
				EduMon.Prefs.currentLecture = EduMon.Data.createCurrentLecture(lectureId);
				that.initLecture();
			})
			.catch(function(err) {
				console.log(err);
			})
	};

	this.initLecture = function(){
		that.updateConnection();
		EduMon.Gui.initSeating();
		broadcastCurrentLecture();
		EduMon.sendPacket({command:'start'});
	};

	this.updateConnection = function(){
		that.sendPacket({command:"config",
			url: EduMon.Prefs.currentLecture.messaging.serverUrl,
			room: EduMon.Prefs.currentLecture.room.roomName,
			moderatorPassphrase:EduMon.Prefs.currentLecture.messaging.moderatorPassphrase
		});
	};

	this.stopLecture = function(){
		EduMon.sendPacket({command:'stop'});
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
