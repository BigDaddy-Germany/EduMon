

function createRequest(url) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.withCredentials = true;
    return xhr;
}



var outgoing = [];

onmessage = function(e) {
    outgoing.push(e.data);
};

function handleEvent(event) {
    if (event instanceof Array) {
        for (var i = 0; i < event.length; ++i) {
            postMessage(event[i]);
        }
    } else {
        postMessage(event);
    }
}

setInterval(function() {
    var toBeSent = outgoing;
    outgoing = [];

    var req = createRequest('demoDataIn.json');

    function onError() {
        console.log("Failed to send events to server, requeuing...");
        outgoing = outgoing.concat(toBeSent);
    }

    req.onload = function() {
        if (req.status == 200) {
            handleEvent(JSON.parse(req.responseText))
        } else {
            onError();
        }
    };
    req.onerror = onError;
    req.contentType = 'application/json';
    req.accepts = 'application/json';
    req.dataType = 'json';
    req.send(JSON.stringify(toBeSent));

}, 1000);
