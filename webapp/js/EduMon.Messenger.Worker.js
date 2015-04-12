/* Message-Worker communicates with the message server */

EduMon = {};
importScripts('EduMon.Util.js');

//config
var debugging = true;
var url = "localhost";
var room = encodeURIComponent("42A");
var moderatorPassphrase = encodeURIComponent("secretpassword");
var outgoing = [];
var interval = 1000;
var timer = -1; // TODO -1 under the assumption that only positive IDs are assigned by browsers
var requests_failed = 0;
var configured = false;


/* Create a JSON-POST-HTTP-Request */
function createRequest() {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", url + "?room=" + room + "&moderatorPassphrase=" + moderatorPassphrase, true);
	xhr.withCredentials = true;
	xhr.setRequestHeader("Content-type", "text/plain; charset=UTF-8"); //forged to force cross-site request of the "simple" type
	xhr.setRequestHeader("Accept", "application/json");
	return xhr;
}

var commands = {
	config: function(data) {
		if ("url"                 in data) url = data.url;
		if ("room"                in data) room = encodeURIComponent(data.room);
		if ("moderatorPassphrase" in data) moderatorPassphrase = encodeURIComponent(data.moderatorPassphrase);
		if ("interval"            in data) interval = data.interval;
		configured = true;
	},
	start: function() {
		if (!configured){
			console.log("Worker cannot be started: Not yet configured");
		} else {
			if (timer !== undefined) {
				clearInterval(timer);
				console.log("Queue processing was already running, stopped it!");
			}
			console.log('Queue processing started.');
			timer = setInterval(processQueue, interval);
		}
	},
	stop: function() {
		clearInterval(timer);
		timer = undefined;
	}
};


/* Handle call for action from main app */
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


/* Forward incoming data to main app */
// TODO what is the expected benefit of wrapping postMessage() with handleEvent() ?
function handleEvent(event) {
	postMessage(event);
}


/* Send queued packets */
function processQueue() {
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
