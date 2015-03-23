(function() {

	/* Constructor */
	function EduMon() {
		var self = this;
		this.show_debug = true;

		this.messenger = new Messenger(function(event){
			self.handleIncomingPacket(event);
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
		var w;
		var c = eventCallback;

		this.start = function(){
			w = new Worker('js/app.worker.js');

			w.onmessage = function(e) {
				c(e.data);
			};
		}

		this.sendEvent = function(event) {
			w.postMessage(event);
		};

		this.stop = function() {
			w.terminate();
		};
	};


	/* EduMon startup */
	EduMon.prototype.init = function(){
		var self = this;

		this.debug("*** All Glory to the EduMon! ***")

		this.messenger.start();
	};


	/* Handle incoming packets */
	EduMon.prototype.handleIncomingPacket = function(event){
		//this.debug(event);
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
		this.messenger.sendEvent(packet);
	}


	window.EduMon = new EduMon();

})();
