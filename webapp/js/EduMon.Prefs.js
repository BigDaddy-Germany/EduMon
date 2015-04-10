EduMon.Prefs = {
    courses: [],        // an array of all courses
    rooms: [],          // an array of all rooms
    lectures: [],       // an array of all lectures
    students: [],       // an array of all students

    currentLecture: {}  // the current running lecture (copy everything!)

};


// todo DEBUG
EduMon.Prefs.currentLecture = {
    lectureName: "My fancy lecture",
    room: {
        roomName: "160C",
        width: 6,
        height: 8
    },
    course: {
        courseName: "TINF13AIBC",
        students: []
    },
    activeStudents: {
        '44aa488f082b42f5fdc0090878f8ef3f': {
            studentName: 'Steyer',
            group: 'ShitGroup',
            seat: {x: 3, y: 2},
            disturbance: 0,
            history: [/*
                {
                    time: 1234566,
                    microphone: 12,
                    keyboard: 13,
                    mouseDistance: 145,
                    mouseClicks: 13
                }
            */],
            micHistory: [/*
                {
                    time: 123456,
                    value: 123
                }
            */]
        }
    },
    analytics: {
        globalReferenceValues: {
            /*
             sender: {
                 microphone: {},
                 keyboard: {},
                 mouseDistance: {},
                 mouseClicks: {}
             }
             */
        }
    }
};