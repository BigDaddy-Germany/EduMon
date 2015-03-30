/* Packet handling - Messager class connects with message worker */
window.EduMon.Messenger = function Messenger(eventCallback){
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
	worker.callbackWrapper = eventCallback; // TODO dafuq?
	worker.onmessage = function(e) {
		this.callbackWrapper(e.data);
	};
};
