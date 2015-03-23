/* Message-Worker communicates with the message server */

//config
var show_debug = true;
var url = "demoDataEmpty.json";
var outgoing = [];
var interval = 1000;
var timer = undefined;
var requests_failed = 0;


/* Create a JSON-POST-HTTP-Request */
function createRequest() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.withCredentials = true;
    xhr.contentType = 'application/json';
    xhr.accepts = 'application/json';
    xhr.dataType = 'json';
    return xhr;
}


/* Handle call for action from main app */
onmessage = function(input) {
	//Configuration command
	if ("command" in input.data){

		if (input.data.command==="config"){
			url = input.data.url || "demoDataEmpty.json";
			interval = input.data.interval || 1000;

		} else if(input.data.command==="start") {
			if (timer !== undefined){
				clearInterval(timer);
				console.log("Queue processing already started!");
			}
			timer = setInterval(sendQueue,interval);

		} else if(input.data.command==="stop") {
				clearInterval(timer);
				timer = undefined;
		}

	//Packet to queue
	} else {
		outgoing.push(input.data);
	}
};


/* Process incoming paket[-array] */
function handleEvent(event) {
    if (event instanceof Array) {
        for (var i = 0; i < event.length; ++i) {
            postMessage(event[i]);
        }
    } else {
        postMessage(event);
    }
}


/* Send queued packets */
function sendQueue() {
    var toBeSent = outgoing;
    outgoing = [];

    var req = createRequest();

    function onError() {
		requests_failed++;
        outgoing = outgoing.concat(toBeSent);
    }

    function onLoad() {
        if (req.status == 200) {
            handleEvent(JSON.parse(req.responseText))
        } else {
            onError();
        }
    };

    req.onerror = onError;
	req.onload = onLoad;
    req.send(JSON.stringify(toBeSent));
}
