EduMon = new function () {
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
	 */
	this.init = function () {
		that.debug("*** All Glory to the EduMon! ***");
		that.debug("EduMon awakening...");
		that.messenger = new EduMon.Messenger(handleIncomingData);

		this.Prefs.rooms.push(new EduMon.Data.Room("160C", 6, 4));
		this.Prefs.courses.push(new EduMon.Data.Course("DemoCourse", [
			new EduMon.Data.Student("Max Mustermann", "Gruppe 1"),
			new EduMon.Data.Student("Anna Mustermann", "Gruppe 1"),
			new EduMon.Data.Student("Lieschen Müller", "Gruppe 2"),
			new EduMon.Data.Student("Arno Nymous", "Gruppe 2")
		]));
		this.Prefs.lectures.push(new EduMon.Data.Lecture("DemoLecture", 0, 0));

		this.Prefs.currentLecture = EduMon.Data.createCurrentLecture();

		//UI preparations
		this.Timeline.init();
		this.Gui.init();

		this.Analytics = new EduMon.Analytics();
		util = EduMon.Util;

		this.tryRestoreApp();
		this.enablePersistApp();

		bindFortuneWheel(this.Prefs);

		if (applicationCache && applicationCache.status === 1) {
			console.info("Updating the application cache...");
			applicationCache.update();
		}

		that.messenger.start();
	};


	/**
	 * Debug output to JS console
	 *
	 * @param {String} msg Message to display
	 */
	this.debug = function (msg) {
		if (that.debugging) {
			console.log(msg);
		}
	};


	/**
	 * Process incoming container data (bundled packets+errors)
	 *
	 * @param {Object} event Incoming JSON as delivered by proxy server
	 */
	var handleIncomingData = function (event) {
		//Process packets received by message server
		if ("inbox" in event) {
			event.inbox.forEach(processPacket);
		}

		//Handle any errors
		if ("errorMessages" in event) {
			event.errorMessages.forEach(that.debug);
		}
	};

	this.processPacketPublic = function (packet) {
		processPacket(packet);
	};


	/**
	 * Process single packet
	 *
	 * @param {Object} packet Packet as defined by architecture
	 */
	var processPacket = function (packet) {
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
				EduMon.Feedback.updateFeedback();
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
	 */
	var processBreakRequest = function () {
		if (EduMon.Prefs.currentLecture.timeline.status == "play") {
			var analytics = EduMon.Prefs.currentLecture.analytics;
			var numStudents = EduMon.Util.countFields(EduMon.Prefs.currentLecture.activeStudents);
			analytics.breakRequests++;

			var style = "info";
			if (analytics.breakRequests > (numStudents * 0.2)) {
				style = "warning";
			}
			if (analytics.breakRequests > (numStudents * 0.5)) {
				style = "danger";
			}

			var msgStart = (analytics.breakRequests > 1 ? analytics.breakRequests + " Teilnehmer bitten" : "Ein Teilnehmer bittet");
			EduMon.Gui.showFeedMessage(style, "Pausenanfrage", msgStart + " um eine Pause.");
		}
	};

	/**
	 * Tries to login a client by its name and its seat and sends confirmation/denial message
	 *
	 * @param {String} sender the client's session id
	 * @param {Object} body The body sent with the package to the server
	 * @return {int} Success code as delivered to client (0 = success, >0 error codes)
	 */
	var processLogin = function (sender, body) {
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
		course.students.forEach(function (student) {
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

		that.messenger.sendEvent(responsePacket);
		return errorBits.bits;
	};


	/**
	 * Broadcasts the current lecture (room dimensions + list of names)
	 *
	 * @return {Object} packet Copy of the sent broadcast packet
	 */
	var broadcastCurrentLecture = function () {
		var room = EduMon.Prefs.currentLecture.room;
		var body = {
			"names": [],
			"room": room.roomName,
			"dimensions": {"width": room.width, "height": room.height}
		};
		var students = EduMon.Prefs.currentLecture.course.students;
		for (var i = 0; i < students.length; i++) {
			body.names.push(students[i].name);
		}

		var packet = EduMon.Data.createBasePacket(1, "BROADCAST", body);
		that.messenger.sendEvent(packet);
		return packet;
	};

	/**
	 * Opens the lecture starting dialog
	 */
	this.lectureStartDialog = function () {
		EduMon.UserInteraction.selectLecture()
			.then(function (lectureId) {
				EduMon.Prefs.currentLecture = EduMon.Data.createCurrentLecture(lectureId);
				that.initLecture();
				EduMon.Timeline.play();
			})
			.catch(function (err) {
				console.log(err);
			})
	};

	/**
	 * Initializes the current lecture: connect to server, refresh timeline, reload seating plan, restore gui, broadcast setup
	 *
	 * @param {boolean} [broadCastIt=true] should a broadcast be sent?
	 */
	this.initLecture = function (broadCastIt) {
		if (broadCastIt === undefined) {
			broadCastIt = true;
		}

		that.updateConnection();
		that.messenger.start();
		EduMon.Gui.initSeating();
		var pultUp = EduMon.Prefs.currentLecture.gui.pultup;
		if (pultUp !== "") {
			EduMon.Gui.openPultUpMode(pultUp, EduMon.Feedback.updateFeedback);
		}
		if (broadCastIt) {
			EduMon.Timeline.reset();
			broadcastCurrentLecture();
		}
	};

	/**
	 * Configures the message worker to use the connection settings of the current lecture
	 */
	this.updateConnection = function () {
		that.messenger.configure({
			url: EduMon.Prefs.currentLecture.messaging.serverUrl,
			room: EduMon.Prefs.currentLecture.room.roomName,
			moderatorPassphrase: EduMon.Prefs.currentLecture.messaging.moderatorPassphrase
		});
	};

	/**
	 * Stops the message worker to temporarily deactivate network communication
	 */
	this.stopLecture = function () {
		that.messenger.stop();
		EduMon.Prefs.currentLecture = {
			activeStudents: [],
			seatingPlan: []
		};
	};

	/**
	 * [DEV] Send demo packet - TESTING ONLY
	 *
	 * @param {int} typenumber Type of package to send
	 */
	this.sendDemo = function (typenumber) {
		jQuery.getJSON("js/demoDataOut.json", function (data) {
			that.messenger.sendEvent(data["demo_type_" + typenumber]);
		});
	};

	/**
	 * Activate continuous saving of app status to local storage
	 */
	this.enablePersistApp = function () {
		setTimeout(function () {
			setInterval(function () {
				localStorage.setItem("EduMon.Prefs", JSON.stringify(EduMon.Prefs));
				//EduMon.Gui.showToast("App state saved");
			}, 1000); //persist every 1sec
		}, 5000); //start persisting app after 5sec
	};

	/**
	 * Restore a locally stored app status if available
	 *
	 * @return {Boolean} stored status found
	 */
	this.tryRestoreApp = function () {
		var stored = localStorage.getItem("EduMon.Prefs");
		if (stored) {
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
					function (chosenOption) {
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
	 *
	 * @param {Object} packet
	 */
	this.sendPacket = function (packet) {
		that.messenger.sendEvent(packet);
	};


	/**
	 * devOnly
	 */
	this.testAllThemAnalytics = function (timeoutCall) {

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
			properties.forEach(function (property) {
				packet[property] = EduMon.Math.randomInteger(0, 20) + i;
			});

			EduMon.Analytics.processData(user, Math.round(new Date().getTime() / 1000), packet);
		});

		window.setTimeout(function () {
			EduMon.testAllThemAnalytics(true);
		}, 200);
	};

	/**
	 * This function binds the fortune wheel logic to the UI button and holds its state and RPC API
	 */
	function bindFortuneWheel() {
		var button = $('#btnWheel');
		var wheelWindow = null;
		var controller = null;
		var spaceKey = 32;

		var messageShown = false;
		$(window).on('keydown', function (e) {
			if (e.which != spaceKey) {
				return;
			}
			if (wheelWindow && !messageShown) {
				if (wheelWindow.closed) {
					wheelWindow = null;
					return;
				}
				EduMon.Gui.showToast("Das Fenster ist leider nicht im Vordergrund!");
				messageShown = true;
			}
		}).on('keyup', function (e) {
			if (e.which != spaceKey || !messageShown) {
				return;
			}
			messageShown = false;
		}).on('unload', function () {
			if (wheelWindow) {
				wheelWindow.close();
				wheelWindow = null;
			}
		});

		button.on('click', function () {

			if (!EduMon.Prefs.currentLecture || EduMon.Prefs.currentLecture.activeStudents.length == 0) {
				EduMon.Gui.showToast("Es ist keine Vorlesung aktiv!");
				return;
			}

			// Close the wheel if it is still open
			if (wheelWindow) {
				wheelWindow.close();
				wheelWindow = null;
			}

			// properly shutdown the last controller if not already done
			if (controller) {
				controller.shutdown();
				controller = null;
			}

			var $nameField = $('#pultup').find('div.wheel.choice');

			EduMon.Gui.openPultUpMode('wheel', function () {
				$nameField.text("Noch nichts ausgewählt.");
			});

			var wheelData = EduMon.Prefs.wheel;

			// open the window with its old location and size
			wheelWindow = EduMon.Util.openWindow("wheel.html", {
				top: wheelData.top,
				left: wheelData.left,
				width: wheelData.width,
				height: wheelData.height,
				resizable: true
			}, "blank");

			// connect to the wheel window
			controller = new EduMon.XWindowRPC(wheelWindow, {
				wheelFinished: function (name, mode, selection) {
					wheelData.lastMode = mode;
					var pre = (selection == 'groups' ? "Gruppe: " : "Student: ");
					$nameField.text(pre + name);
				},
				getLecture: function () {
					return EduMon.Prefs.currentLecture;
				}
			});
			wheelWindow.focus();

			wheelWindow.onload = function () {
				EduMon.Gui.showToast("Glückrad geöffnet!");

				// set the previously selected mode
				if (wheelData.lastMode) {
					controller.invoke('switchMode', wheelData.lastMode);
				}

				wheelWindow.onunload = function () {
					// store the current location and size
					wheelData.top = this.screenY;
					wheelData.left = this.screenX;
					wheelData.width = $(this).width();
					wheelData.height = $(this).height();

					// try shutting down the controller a second after the unload event, the window should be closed
					// by then
					setTimeout(function () {
						// in case the user refreshed the window this will called as well, so prevent the shutdown in
						// that case
						if (wheelWindow.closed) {
							controller.shutdown();
							controller = null;
						}
					}, 1000);
				};
			};
		});
	}
};
