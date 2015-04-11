/* Packet handling - Messager class connects with message worker */
EduMon.Messenger = function(eventCallback){
	var worker;

	/* Send command to message worker */
	this.sendEvent = function(event){
		worker.postMessage(event);
	};


	/* Destroy message worker */
	this.kill = function(){
		worker.terminate();
	};


	worker = new Worker('js/EduMon.Messenger.Worker.js');
	worker.onmessage = function(e) {
		eventCallback(e.data);
	};
};
