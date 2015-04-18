/* Message-Worker communicates with the message server */

importScripts('EduMon.Util.js');

//config
var debugging = true;
var url = "localhost";
var room = encodeURIComponent("42A");
var moderatorPassphrase = encodeURIComponent("secretpassword");
var outgoing = [];
var interval = 1000;
var timer = -1; // under the assumption that only positive IDs are assigned by browsers
var requests_failed = 0;
var configured = false;


/**
 * Create a new JSON-POST-HTTP-Request 
 * @method createRequest
 * @return {XMLHttpRequest}
 */
function createRequest() {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", url + "?room=" + room + "&moderatorPassphrase=" + moderatorPassphrase, true);
	xhr.withCredentials = true;
	xhr.setRequestHeader("Content-type", "text/plain; charset=UTF-8"); //forged to force cross-site request of the "simple" type
	xhr.setRequestHeader("Accept", "application/json");
	return xhr;
}

/* fitting function is executed if "command" attribute is present in incoming data for worker */
var commands = {
	/**
	 * Configure connection
	 * @param {Object} data Setup parameters: url, room, moderatorPassphrase, interval (in msec)
	 */
	config: function(data) {
		if ("url"                 in data) url = data.url;
		if ("room"                in data) room = encodeURIComponent(data.room);
		if ("moderatorPassphrase" in data) moderatorPassphrase = encodeURIComponent(data.moderatorPassphrase);
		if ("interval"            in data) interval = data.interval;
		configured = true;
		if (timer!==-1){
			console.log("Worker started and configured, queue will be processed");
		} 
	},
	/**
	 * Enable queue processing (is automatically paused until first configuration)
	 */
	start: function() {
		if (!configured){
			console.log("Warning: Worker started, but will only send once configured");
		} 
		if (timer !== -1) {
			clearInterval(timer);
			console.log("Queue timer was already running, restarted it!");
		}
		timer = setInterval(processQueue, interval);
		console.log('Queue timer started.');
	},
	/**
	 * Stop queue processing
	 */
	stop: function() {
		clearInterval(timer);
		timer = undefined;
	}
};


/**
 * Handle call for action from main app 
 * @param {Object} input Inter-thread communication object
 */
onmessage = function (input) {
	//Configuration command
	var data = input.data;
	if ("command" in data) {
		var cmd = data.command;
		if (commands.hasOwnProperty(cmd)) {
			commands[cmd](data);
		} else {
			console.log("Received unknown command '" + cmd + "' ... ignoring it.")
		}

	} else {
		//Packet to queue
		outgoing.push(data);
	}
};

/**
 * Forward incoming data to main app
 * @method handleEvent
 * @param {Object} event Data to forward
 * @return undefined
 */
function handleEvent(event) {
	postMessage(event);
}

/**
 * Send queued packets (called by queue timer)
 * @method processQueue
 * @return undefined
 */
function processQueue() {
	if (!configured){
		return;
	}

	var toBeSent = outgoing;
	outgoing = [];

	var req = createRequest();

	function onError() {
		requests_failed++;
		outgoing = outgoing.concat(toBeSent);
		if (debugging) console.log("Request failed (" + toBeSent.length + " packets waiting)");
	}

	function onLoad() {
		if (req.status == 200) {
			handleEvent(JSON.parse(req.responseText))
		} else {
			onError();
		}
	}

	req.onerror = onError;
	req.onload = onLoad;
	req.send(JSON.stringify(toBeSent));
}
