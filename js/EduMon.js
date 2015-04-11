window.EduMon = new EduMon();

/* Main app class */
function EduMon() {
	var that = this;

	var sessionId = ""; //session id assinged by message server TODO necessary?

	this.debugging = true; //show debug messages in javascript console

	/* EduMon startup */
	this.init = function(){
		that.debug("*** All Glory to the EduMon! ***");
		that.debug("EduMon awakening...");
		that.messenger = new EduMon.Messenger(handleIncomingData);

		this.Prefs.rooms.push(new EduMon.Data.Room("160C",5,5));
		this.Prefs.students.push(new EduMon.Data.Student("Max Mustermann","Mustergruppe"));
		this.Prefs.courses.push(new EduMon.Data.Course("DevCourse",[0]));
		this.Prefs.lectures.push(new EduMon.Data.Lecture("DevLecture",[0],[0]));

		this.Prefs.currentLecture = EduMon.Data.createCurrentLecture(0);
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

		//Save session id assigned by message server
		if ("clientId" in event){
			sessionId = event.clientId;
		}
	};


	/* Process packet */
	var processPacket = function(packet){
		//TODO to be implemented
		that.debug("Received packet:");
		that.debug(packet);
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
}
