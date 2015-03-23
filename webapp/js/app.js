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


	/* Debugausgabe in die Konsole */
	EduMon.prototype.debug = function(msg){
		if (this.show_debug){
			console.log(msg);
		}
	};


	/* Nachrichten-Handling */
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


	/* EduMon starten */
	EduMon.prototype.init = function(){
		var self = this;

		this.messenger.start();

		this.debug("EduMon initiated");
		this.debug("*** All Glory to the EduMon! ***")
	};


	/* Eingehende Pakete behandeln */
	EduMon.prototype.handleIncomingPacket = function(event){
		this.debug(event);
	}


	window.EduMon = new EduMon();

})();
