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

	/**
	 * EduMon startup, to be called when DOM is ready
	 * @method init
	 * @return undefined
	 */
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

		this.tryRestoreApp();
		this.enablePersistApp();

		bindFortuneWheel(this.Prefs);

		EduMon.sendPacket({command:'start'});
	};


	/**
	 * Debug output to JS console 
	 * @method debug
	 * @param {String} msg Message to display
	 * @return undefined
	 */
	this.debug = function(msg){
		if (that.debugging){
			console.log(msg);
		}
	};


	/**
	 * Process incoming container data (bundled packets+errors)
	 * @method handleIncomingData
	 * @param {Object} event Incoming JSON as delivered by proxy server
	 * @return undefined
	 */
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


	/**
	 * Process single packet 
	 * @method processPacket
	 * @param {Object} packet Packet as defined by architecture
	 * @return undefined
	 */
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
				var packageLogging = $('#packageLogging');
				packageLogging.text(packageLogging.text() + ',' + JSON.stringify(packet));
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
				processBreakRequest();
				break;

			default:
				console.error('Unknown packet type ' + packetType + ' received.');
		}
	};

	/**
	 * React to a break request by incrementing a counter and displaying a newsfeed alert with varying intensity
	 * @method processBreakRequest
	 * @return undefined
	 */
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
	 * Tries to login a client by its name and its seat and sends confirmation/denial message
	 * @method processLogin
	 * @param {String} sender the client's session id
	 * @param {Object} body The body sent with the package to the server
	 * @return int Success code as delivered to client (0 = success, >0 error codes)
	 */
	var processLogin = function(sender, body) {
		var seat = body.seat;
		var name = body.name;

		var currentLecture = EduMon.Prefs.currentLecture;

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

		// if user is already logged in, clear data
		var currentSession = currentLecture.activeStudents[sender];
		if (currentSession &&
			(seat.x != currentSession.seat.x || seat.y != currentSession.seat.y || name != currentSession.name)
		) {
			delete currentLecture.activeStudents[sender];
			EduMon.Gui.deleteSeat(currentSession.seat.x, currentSession.seat.y);
		}

		// check state of chosen seat
		// if seat exists
		if (seat.x <= room.width && seat.y <= room.height) {
			currentLecture.seatingPlan[seat.x] = currentLecture.seatingPlan[seat.x] || [];

			var seatState = currentLecture.seatingPlan[seat.x][seat.y];
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
			util.forEachField(currentLecture.activeStudents, function (sessionId, student) {
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
			currentLecture.activeStudents[sender] = {
				name: name,
				group: groupForName,
				seat: seat,
				disturbance: 0,
				history: [],
				micHistory: []
			};

			currentLecture.seatingPlan[seat.x] = currentLecture.seatingPlan[seat.x] || [];
			currentLecture.seatingPlan[seat.x][seat.y] = sender;
		}

		responsePacket.body.successCode = errorBits.bits;

		that.sendPacket(responsePacket);
		return errorBits.bits;
	};


	/**
	 * Broadcasts the current lecture (room dimensions + list of names)
	 * @method broadcastCurrentLecture
	 * @return packet Copy of the sent broadcast packet
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

	/**
	 * Opens the lecture starting dialog
	 * @method lectureStartDialog
	 * @return undefined
	 */
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

	/**
	 * Initializes the current lecture: connect to server, refresh timeline, reload seating plan, restore gui, broadcast setup
	 * @method initLecture
	 * @param {boolean} [broadCastIt=true] should a broadcast be sended?
	 * @return undefined
	 */
	this.initLecture = function(broadCastIt){
		broadCastIt = !!broadCastIt;

		that.updateConnection();
		EduMon.Gui.initSeating();
		var pultup = EduMon.Prefs.currentLecture.gui.pultup;
		if (pultup!==""){
			EduMon.Gui.openPultUpMode(pultup,EduMon.Feedback.updateFeedback);
		}
		if (broadCastIt) {
			EduMon.Timeline.reset();
			broadcastCurrentLecture();
		}
	};

	/**
	 * Configures the message worker to use the connection settings of the current lecture
	 * @method updateConnection
	 * @return undefined
	 */
	this.updateConnection = function(){
		that.sendPacket({command:"config",
			url: EduMon.Prefs.currentLecture.messaging.serverUrl,
			room: EduMon.Prefs.currentLecture.room.roomName,
			moderatorPassphrase: EduMon.Prefs.currentLecture.messaging.moderatorPassphrase
		});
	};

	/**
	 * Stops the message worker to temporarily deactivate network communication
	 * @method stopLecture
	 * @return undefined
	 */
	this.stopLecture = function(){
		EduMon.sendPacket({command:'stop'});
	};

	/**
	 * [DEV] Send demo packet - TESTING ONLY
	 * @method sendDemo
	 * @param {int} typenumber Type of package to send
	 * @return undefined
	 */
	this.sendDemo = function(typenumber){
		jQuery.getJSON("js/demoDataOut.json",function(data){
			that.sendPacket(data["demo_type_"+typenumber]);
		});
	};

	/**
	 * Activate continuous saving of app status to local storage
	 * @method enablePersistApp
	 * @return undefined
	 */
	this.enablePersistApp = function(){
		setTimeout(function(){
			setInterval(function(){
				localStorage.setItem("EduMon.Prefs",JSON.stringify(EduMon.Prefs));
				//EduMon.Gui.showToast("App state saved");
			},10000); //persist every 10sec
		},5000); //start persisting app after 5sec
	};

	/**
	 * Restore a locally stored app status if available
	 * @method tryRestoreApp
	 * @return {Boolean} stored status found
	 */
	this.tryRestoreApp = function(){
		var stored = localStorage.getItem("EduMon.Prefs");
		if (stored!==null){
			var Prefs = JSON.parse(stored);

			// save currentLecture to use it later
			var currentLecture = Prefs.currentLecture;
			Prefs.currentLecture = {activeStudents: [], seatingPlan: []};

			EduMon.Prefs = Prefs;

			if (currentLecture.room && currentLecture.course) {
				EduMon.Gui.showPopup(
					"Vorlesung wiederherstellen",
					"In Ihren Einstellungen wurde eine aktive Vorlesung gefunden. Soll diese nun wiederhergestellt werden?",
					['yes', 'no'],
					function(chosenOption) {
						if (chosenOption == 'yes') {
							EduMon.Prefs.currentLecture = currentLecture;
							that.initLecture(false);
						}
					}
				);
			}

			EduMon.Gui.showToast("App loaded");

			return true;
		} else {
			return false;
		}
	};

	/**
	 * Queue single packet for sending 
	 * @method sendPacket
	 * @param {Object} packet
	 * @return undefined
	 */
	this.sendPacket = function(packet){
		that.messenger.sendEvent(packet);
	};


	/**
	 * devOnly
	 */
	this.testAllThemAnalytics = function(timeoutCall) {

		if (!EduMon.Prefs.currentLecture) {
			EduMon.Gui.showPopup("Error", "Lecture must be started to use this function.", ['ok']);
			return;
		}

		var properties = ['keys', 'mdist', 'mclicks', 'volume'];
		var users = ['niko', 'phillip', 'jonas', 'marco'];


		var i = 0;
		users.forEach(function (user) {
			++i;

			if (!timeoutCall) {
				// set active students
				EduMon.Prefs.currentLecture.activeStudents[user] = {
						name: user,
						group: user,
						seat: {x: i, y: 1},
						disturbance: 0,
						history: [],
						micHistory: []
				};

				EduMon.Prefs.currentLecture.seatingPlan[i] = [];
				EduMon.Prefs.currentLecture.seatingPlan[i][1] = user;
			}

			// shuffle all them things
			var packet = {};
			properties.forEach(function(property) {
				packet[property] = EduMon.Math.randomInteger(0, 20) + i;
			});

			EduMon.Analytics.processData(user, Math.round(new Date().getTime() / 1000), packet);
		});

		window.setTimeout(function() { EduMon.testAllThemAnalytics(true); }, 200);
	};

	var wheelWindow = null;
	function bindFortuneWheel() {
		var button = $('#btnWheel');

		if (wheelWindow) {
			wheelWindow = null;
			wheelWindow.close();
		}

		var wheelData = EduMon.Prefs.wheel;

		button.on('click', function() {

			wheelWindow = EduMon.Util.openWindow("wheel.html", {
				top: wheelData.top,
				left: wheelData.left,
				width: wheelData.width,
				height: wheelData.height,
				resizable: true
			}, "blank");

			wheelWindow.onunload = function() {
				wheelData.top = this.screenY;
				wheelData.left = this.screenX;
				wheelData.width = $(this).width();
				wheelData.height = $(this).height();
			};

			wheelWindow.onload = function() {
				wheelWindow.focus();

				var controller = new EduMon.XWindowRPC(wheelWindow, {
					wheelFinished: function(segment) {
						EduMon.Gui.showToast("Aaaand the winner is: " + segment);
					}
				});

				controller.invoke('showWheel', EduMon.Prefs.currentLecture.course).then(function() {
					EduMon.Gui.showToast("Glückrad geöffnet!");
				}).catch(function(e) {
					throw e;
				});
			};
		});
	}
};
