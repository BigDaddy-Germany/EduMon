(function() {

	/* Constructor */
	function EduMon() {
		var self = this;
		this.show_debug = true; //show debug messages in javascript console
		this.session_id = ""; //session id assinged by message server //TODO: necessary?

		/* Packet handling - Messager class connects with message worker */
		this.Messenger = (function(){
			//private vars
			var _cback, _worker;

			//private methods
			function _eventCallback(event){
				self.handleIncomingData(event);
			};

			//public vars & methods
			return {
				/* Constructor */
				"init": function(){
					_cback = _eventCallback;
					_worker = new Worker('js/app.messageworker.js');
					_worker.onmessage = function(e) {
						_eventCallback(e.data);
					};
				},
					/* Send command to message worker */
					"sendEvent": function(event){
						_worker.postMessage(event);
					},
					/* Destroy message worker */
					"kill": function(){
						_worker.terminate();
					}
			};
		}());

		this.debug("EduMon awakening...");
		this.Messenger.init();
	};


	/* Debug output to JS console */
	EduMon.prototype.debug = function(msg){
		if (this.show_debug){
			console.log(msg);
		}
	};


	/* EduMon startup */
	EduMon.prototype.init = function(){
		var self = this;

		this.debug("*** All Glory to the EduMon! ***");
	};


	/* Handle incoming data */
	EduMon.prototype.handleIncomingData = function(event){
		//Process packets received by message server
		if ("inbox" in event && event.inbox.length > 0){
			this.debug("Received packet[s]:");
			this.debug(event.inbox);
			for (var i = 0; i < event.inbox.length; ++i) {
				this.processPacket(event.inbox[i]);
			}
		}

		//Handle any errors
		if ("errorMessages" in event && event.errorMessages.length > 0){
			for (var i = 0; i < event.errorMessages.length; ++i) {
				this.debug(event.errorMessages[i]);
			}
		}

		//Save session id assigned by message server
		if ("clientId" in event){
			this.session_id = event.clientId;
		}
	}


	/* Process packet */
	EduMon.prototype.processPacket = function(event){
		//TODO to be implemented
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
		this.cmdConnection(packet);
	}


	/* Command message worker */
	EduMon.prototype.cmdConnection = function(cmd){
		this.Messenger.sendEvent(cmd);
	}


	window.EduMon = new EduMon();

})();
