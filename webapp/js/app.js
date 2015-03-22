
var w = new Worker('js/app.worker.js');
w.onmessage = function(e) {
    console.log(e);
    w.postMessage(e.data);
};

w.postMessage("Test");
