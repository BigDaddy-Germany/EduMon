/* Packet handling - Messager class connects with message worker */
window.EduMon.Messenger = function Messenger(eventCallback){
	var that = this;

	////////////////////private vars
	var _worker;

	////////////////////public vars

	////////////////////private methods

	////////////////////public methods

	/* Send command to message worker */
	this.sendEvent = function(event){
		_worker.postMessage(event);
	};


	/* Destroy message worker */
	this.kill = function(){
		_worker.terminate();
	};


	////////////////////constructor
	_worker = new Worker('js/EduMon.Messenger.Worker.js');
	_worker.callbackWrapper = eventCallback;
	_worker.onmessage = function(e) {
		this.callbackWrapper(e.data);
	};
}
