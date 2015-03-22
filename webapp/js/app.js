
(function() {

    function Messenger(eventCallback) {

        var w = new Worker('js/app.worker.js');

        w.onmessage = function(e) {
            eventCallback(e.data);
        };

        var self = this;

        this.sendEvent = function(event) {
            w.postMessage(event);
        };

        this.stop = function() {
            w.terminate();
        }
    }


    var messenger = new Messenger(function(event) {
        //console.log(event);
    });

    window.Messenger = messenger;
})();
