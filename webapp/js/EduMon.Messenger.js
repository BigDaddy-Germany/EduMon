/**
 * Packet handling - Messager class connects with message worker 
 * @method Messenger
 * @param {} eventCallback
 * @return 
 */
EduMon.Messenger = function(eventCallback){
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
	 * [DEV] Destroy message worker - EMERGENCY EXIT!
	 * @method kill
	 * @return undefined
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
