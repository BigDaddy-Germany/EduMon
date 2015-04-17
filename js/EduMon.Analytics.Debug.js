var useConstantData = false;

var allPackages = useConstantData ? debugDataConstant : debugDataReal;

var seats = [];

var names = ['Marco', 'Phillip', 'Jonas', 'Niko'];

for (var i = 1; i <= 5; ++i) {
    for (var j = 1; j <= 5; ++j) {
        seats.push({x: i, y: j});
    }
}

function getSeatIndex() {
    var seatIndex = Math.floor(Math.random() * seats.length);
    if (seatIndex != seats.length) {
        return seatIndex;
    }
    return getSeatIndex();
}

/**
 *
 * @param {
 *          {
 *              type: {int},
 *              time: {int},
 *              from: {string},
 *              to: {string},
 *              room: {string},
 *              body: {
 *                  keys: {int},
 *                  mdist: {int},
 *                  mclicks: {int},
 *                  volume: {int}
 *              }
 *          }
 *       } thisPacket
 */
function performPackage(thisPacket) {
    if (!EduMon.Prefs.currentLecture.activeStudents[thisPacket.from]) {
        var seatIndex = getSeatIndex();
        var seat = seats[seatIndex];

        EduMon.Prefs.currentLecture.activeStudents[thisPacket.from] = {
            name: names[0],
            group: 'ShitGroup',
            seat: seat,
            disturbance: 0,
            history: [],
            micHistory: []
        };

        EduMon.Prefs.currentLecture.seatingPlan[seat.x] = EduMon.Prefs.currentLecture.seatingPlan[seat.x] || [];
        EduMon.Prefs.currentLecture.seatingPlan[seat.x][seat.y] = thisPacket.from;

        seats.splice(seatIndex, 1);
        names.splice(0,1);
    }

    EduMon.processPacketPublic(thisPacket);
}


var currentIndex = 0;
function startAnalyticDebugger() {
    performPackage(allPackages[currentIndex++]);
    if (currentIndex < allPackages.length) {
        setTimeout(startAnalyticDebugger, 50);
    }
}