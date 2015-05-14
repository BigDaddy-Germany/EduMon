shuffle = new function () {

    this.mode = 1;

    var randomRunning = false;
    var that = this;

    var memberNames = [
        "Marco Dörfler",
        "Inga Miadowicz",
        "Kim Rosenberg",
        "Marvin Klose",
        "Michael Wieneke",
        "Niko Berkmann",
        "Jonas Dann",
        "Phillip Schichtel",
        "Nick Steyer",
        "Tim Adamek",
        "Andreas Geis",
        "Petra Heynen-Mewis",
        "Stefanie Raschke",
        "Laura Hillenbrand",
        "Ricardo Bischof",
        "Sebastian Lubos",
        "Patrick Weber"
    ];

    var memberGroups = [
        "BigDaddy",
        "Level out Laziness",
        "CoffeeProductions",
        "Die Apfel"
    ];


    var printLuckyOnes = function(luckyName, luckyGroup) {
        $('#resName').val(memberNames[luckyName]);
        $('#resGroup').val(memberGroups[luckyGroup]);
    };

    var chooseNextOne = function() {
        if (randomRunning) {
            var luckyName = Math.round(Math.random() * (memberNames.length - 1));
            var luckyGroup = Math.round(Math.random() * (memberGroups.length - 1));

            printLuckyOnes(luckyName, luckyGroup);

            setTimeout(chooseNextOne, 75);
        }
    };

    this.printInitial = function() {
        printLuckyOnes(0, 0);
    };

    this.startRandom = function() {
        if (randomRunning) {
            return;
        }
        randomRunning = true;
        chooseNextOne();
    };

    this.endRandom = function() {
        if (!randomRunning) {
            return;
        }
        randomRunning = false;

        if (that.mode == 1) {
            that.printInitial();
        }
    };

};


var $window = $(window);

$window.on('keydown', function(e) {
    switch(e.keyCode) {
        case 120:
            shuffle.startRandom();
            break;
        case 226:
            shuffle.mode = 2;
            break;
    }
});

$window.on('keyup', function(e) {
    switch (e.keyCode) {
        case 120:
            shuffle.endRandom();
            break;
        case 226:
            shuffle.mode = 1;
            break;
    }
});

$(function() {
    shuffle.printInitial();

    $('#excel').draggable();
});