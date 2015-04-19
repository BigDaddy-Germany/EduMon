EduMon.DefaultPrefs = {};
/**
 * Returns the default prefs. Inside a function, so that they cannot be changed at any time.
 * @return {{courses: Array, rooms: Array, lectures: Array, currentLecture: {activeStudents: Array, seatingPlan: Array}, wheel: {top: number, left: number, width: number, height: number, lastMode: null}, messaging: {outgoingPackageId: number, serverUrl: string, moderatorPassphrase: string}}}
 */
EduMon.DefaultPrefs.get = function() {
    return {
        courses: [],        // an array of all courses
        rooms: [],          // an array of all rooms
        lectures: [],       // an array of all lectures

        currentLecture: {
            activeStudents: [], seatingPlan: []
        },  // the current running lecture (copy everything!)

        wheel: {
            top: 0,
            left: 0,
            width: 1048,
            height: 652,
            lastMode: null
        },

        messaging: {
            outgoingPackageId: 1,
            serverUrl: "http://vps2.code-infection.de/edumon/mailbox.php",
            moderatorPassphrase: "alohomora"
        }
    };
};