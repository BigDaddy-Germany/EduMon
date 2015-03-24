(function() {

	/* Constructor */
	function EduMon() {
		var self = this;
		this.show_debug = true;
		this.session_id = ""; //necessary?

		this.messenger = new Messenger(function(event){
			self.handleIncomingData(event);
		});

		this.debug("EduMon created");
	};


	/* Debug output to JS console */
	EduMon.prototype.debug = function(msg){
		if (this.show_debug){
			console.log(msg);
		}
	};


	/* Packet handling */
	function Messenger(eventCallback){
		var w = new Worker('js/app.worker.js');
			w.onmessage = function(e) {
				c(e.data);
			};

		var c = eventCallback;

		this.sendEvent = function(event) {
			w.postMessage(event);
		};

		this.kill = function() {
			w.terminate();
		};
	};


	/* EduMon startup */
	EduMon.prototype.init = function(){
		var self = this;

		this.debug("*** All Glory to the EduMon! ***");
	};


	/* Handle incoming data */
	EduMon.prototype.handleIncomingData = function(event){
		if ("inbox" in event && event.inbox.length > 0){
			this.debug("Received packet[s]:");
			this.debug(event.inbox);
			for (var i = 0; i < event.inbox.length; ++i) {
				this.processPacket(event.inbox[i]);
			}
		}

		if ("errorMessages" in event && event.errorMessages.length > 0){
			for (var i = 0; i < event.errorMessages.length; ++i) {
				this.debug(event.errorMessages[i]);
			}
		}

		if ("clientId" in event){
			this.session_id = event.clientId;
		}
	}


	/* Process packet */
	EduMon.prototype.processPacket = function(event){
	}


	/* [DEV] Send demo packet */
	EduMon.prototype.sendDemo = function(typenumber){
		var self = this;

		jQuery.getJSON("js/demoDataOut.json",function(data){
				self.sendPacket(data["demo_type_"+typenumber]);
		});
	}


	/* Queue packet for sending */
	EduMon.prototype.sendPacket = function(packet){
		this.commandWorker(packet);
	}


	/* Command message worker */
	EduMon.prototype.commandWorker = function(cmd){
		this.messenger.sendEvent(cmd);
	}


	window.EduMon = new EduMon();

})();
