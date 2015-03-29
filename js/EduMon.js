window.EduMon = new EduMon();

/* Main app class */
function EduMon() {
	var that = this;

	////////////////////private vars
	var session_id = ""; //session id assinged by message server //TODO: necessary?

	////////////////////public vars
	this.show_debug = true; //show debug messages in javascript console

	////////////////////private methods

	////////////////////public methods

	/* EduMon startup */
	this.init = function(){
		that.debug("*** All Glory to the EduMon! ***");
		that.debug("EduMon awakening...");
		that.messenger = new EduMon.Messenger(this.handleIncomingData);
	};


	/* Debug output to JS console */
	this.debug = function(msg){
		if (that.show_debug){
			console.log(msg);
		}
	};


	/* Handle incoming data */
	this.handleIncomingData = function(event){
		//Process packets received by message server
		if ("inbox" in event && event.inbox.length > 0){
			for (var i = 0; i < event.inbox.length; ++i) {
				that.processPacket(event.inbox[i]);
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
			that.session_id = event.clientId;
		}
	};


	/* Process packet */
	this.processPacket = function(packet){
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
		that.cmdConnection(packet);
	};


	/* Command message worker */
	this.cmdConnection = function(cmd){
		that.messenger.sendEvent(cmd);
	};


	////////////////////constructor
	//moved to init to ensure dom-ready start
}
