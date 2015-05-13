/* Message-Worker communicates with the message server */
/*
  @author Phillip
  @author Niko (configuration API)
*/

importScripts('RPC.js');

//config
var debugging = true;
var url = "localhost";
var room = encodeURIComponent("42A");
var moderatorPassphrase = encodeURIComponent("secretpassword");
var outgoing = [];
var onlineInterval = 1000;
var offlineInterval = 30 * 1000;
var timer = -1;
var configured = false;
var failedRequests = 0;
var failedRequestsForOffline = 20;
var isOnline = true;

var commands = {

	/**
	 * Configure connection
	 *
	 * @param {Object} options Setup parameters: url, room, moderatorPassphrase, interval (in msec)
	 */
	configure: function(options) {
		if ("url" in options) {
			url = options.url;
		}
		if ("room" in options) {
			room = encodeURIComponent(options.room);
		}
		if ("moderatorPassphrase" in options) {
			moderatorPassphrase = encodeURIComponent(options.moderatorPassphrase);
		}
		if ("interval" in options) {
			onlineInterval = options.interval;
		}

		configured = true;
		if (timer !== -1) {
			if (debugging) {
				console.log("Worker started and configured, queue will be processed");
			}
		}
	},

	/**
	 * Enable queue processing (is automatically paused until first configuration)
	 */
	start: function() {
		if (!configured) {
			console.warn("Worker started, but will only send once configured");
		}
		if (timer > -1) {
			clearInterval(timer);
			if (debugging) {
				console.log("Queue timer was already running, restarting it!");
			}
		}
		isOnline = true;
		timer = setInterval(processQueue, onlineInterval);
		if (debugging) {
			console.log('Queue timer started.');
		}
	},

	/**
	 * Stop queue processing
	 */
	stop: function() {
		clearInterval(timer);
		timer = -1;
	},

	queueEvent: function(e) {
		outgoing.push(e);
	}
};

var rpc = RPC.WorkerRPC(self, commands);

/**
 * Forward incoming data to main app
 *
 * @param {Object} event Data to forward
 */
function handleEvent(event) {
	rpc.invoke('handleEvent', event);
}

/**
 * Create a new JSON-POST-HTTP-Request
 *
 * @return {XMLHttpRequest}
 */
function createRequest() {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", url + "?room=" + room + "&moderatorPassphrase=" + moderatorPassphrase, true);
	xhr.withCredentials = true;
	// forged to force cross-site request of the "simple" type
	xhr.setRequestHeader("Content-Type", "text/plain; charset=UTF-8");
	xhr.setRequestHeader("Accept", "application/json");
	return xhr;
}

function checkOnline(error) {
	if (isOnline) {
		if (error) {
			failedRequests++;
			if (failedRequests >= failedRequestsForOffline) {
				isOnline = false;
				goOffline();
			}
		} else {
			failedRequests = 0;
		}
	} else {
		if (!error) {
			failedRequests = 0;
			isOnline = true;
			goOnline();
		}
	}
}

function goOffline() {
	clearInterval(timer);
	timer = setInterval(processQueue, offlineInterval);
	rpc.invoke('goOffline');
}

function goOnline() {
	clearInterval(timer);
	timer = setInterval(processQueue, onlineInterval);
	rpc.invoke('goOnline');
}

/**
 * Send queued packets (called by queue timer)
 */
function processQueue() {
	if (!configured) {
		return;
	}

	var toBeSent = outgoing;
	outgoing = [];

	function onError() {
		checkOnline(true);
		outgoing = outgoing.concat(toBeSent);
		if (debugging) {
			console.debug("Request failed (" + toBeSent.length + " packets waiting)");
		}
	}

	function onLoad() {
		if (req.status == 200) {
			checkOnline(false);
			handleEvent(JSON.parse(req.responseText))
		} else {
			onError();
		}
	}

	var req = createRequest();
	req.onerror = onError;
	req.onload = onLoad;
	req.send(JSON.stringify(toBeSent));
}
