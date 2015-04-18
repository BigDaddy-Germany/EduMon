/**
 * Packet handling - Messenger class connects with message worker
 * @constructor
 * @param {Function} eventCallback
 */
EduMon.Messenger = function(eventCallback){
	var that = this;
	var worker;

	/**
	 * Send a packet or command to the message worker 
	 * @method sendEvent
	 * @param {Object} event Packet or command
	 * @return undefined
	 */
	this.sendEvent = function(event){
		worker.postMessage(event);
	};

	/**
	 * Starts the worker
	 */
	this.start = function() {
		that.sendEvent({
			command: 'start'
		})
	};

	/**
	 * Stops the worker without terminating it
	 */
	this.stop = function() {
		that.sendEvent({
			command: 'stop'
		})
	};

	/**
	 * Configures the worker.
	 *
	 * @param {Object} options the options for the worker
	 */
	this.configure = function(options) {
		options.command = 'config';
		that.sendEvent(options);
	};

	/**
	 * Terminates the web worker.
	 * The worker can not be restarted with the same Messenger instance.
	 */
	this.kill = function(){
		worker.terminate();
	};


	//on construction initialize worker
	worker = new Worker('js/EduMon.Messenger.Worker.js');
	worker.onmessage = function(e) {
		eventCallback(e.data);
	};
};
