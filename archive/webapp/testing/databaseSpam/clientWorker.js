function consoleLog(someObject) {
    console.log(someObject);
    reDo();
}

function reDo() {
    setTimeout(doRequest, 1);
}

var url = 'http://localhost/mailbox.php?room=testRoom';

var myPackage = {};

function doRequest() {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.withCredentials = true;
    // forged to force cross-site request of the "simple" type
    xhr.setRequestHeader("Content-Type", "text/plain; charset=UTF-8");
    xhr.setRequestHeader("Accept", "application/json");


    xhr.onerror = consoleLog;
    xhr.onload = reDo;

    xhr.send(JSON.stringify(myPackage));
}


doRequest();