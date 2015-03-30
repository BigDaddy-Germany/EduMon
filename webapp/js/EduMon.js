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


	// TODO wrapping the wrapping wrapper that wraps a worker (say that 10 times in a row without mistakes!)
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
