/**
 * Packet handling - Messenger class connects with message worker
 *
 * @param {Object} procedures a map of remote procedures
 * @constructor
 */
EduMon.Messenger = function(procedures) {
	var worker = new Worker('js/EduMon.Messenger.Worker.js');
	var rpc = RPC.WorkerRPC(worker, procedures);

	/**
	 * Send a packet or command to the message worker
	 *
	 * @param {Object} event Packet or command
	 * @return {Promise} a promise
	 */
	this.sendEvent = function(event){
		console.log(event);
		return rpc.invoke('queueEvent', event);
	};

	/**
	 * Starts the worker.
	 *
	 * @return {Promise} a promise
	 */
	this.start = function() {
		return rpc.invoke('start');
	};

	/**
	 * Stops the worker without terminating it.
	 *
	 * @return {Promise} a promise
	 */
	this.stop = function() {
		return rpc.invoke('stop');
	};

	/**
	 * Configures the worker.
	 *
	 * @param {Object} options the options for the worker
	 * @return {Promise} a promise
	 */
	this.configure = function(options) {
		return rpc.invoke('configure', options);
	};

	/**
	 * Terminates the web worker.
	 * The worker can not be restarted with the same Messenger instance.
	 */
	this.kill = function(){
		rpc.shutdown();
		worker.terminate();
	};

};
