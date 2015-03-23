/* Message-Worker communicates with the message server */

//config
var url = "demoDataIn.json";
var outgoing = [];
var interval = 1000;
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
onmessage = function(e) {
	//Configuration command
	if ("command" in e){
		if (e.command=="config"){
			url = e.url;
		}
	//Packet to queue
	} else {
		outgoing.push(e.data);
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


/* Continuous sending of queued packets */
setInterval(function() {
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

}, interval);
