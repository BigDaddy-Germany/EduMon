window.EduMon.Data = new function Data() {
	/**
	 * Creates a new student object
	 * @param {String} studentName the student's name
	 * @param {String} group the group, the student is in
	 */
	this.Student = function Student(studentName, group) {
		return {
			studentName: studentName,
			group: group
		};
	};

	/**
	 * Creates a new course object
	 * @param {String} courseName A name to identify the course
	 * @param {int[]} students An array of all students (IDs) in the course
	 */
	this.Course = function Course(courseName, students) {
		return {
			courseName: courseName,
			students: students
		};
	};

	/**
	 * Creates a new room object
	 * @param {String} roomName The room's name
	 * @param {int} width The room's seats per row
	 * @param {int} height The room's rows
	 */
	this.Room = function Room(roomName, width, height) {
		return {
			roomName: roomName,
			width: width,
			height: height
		};
	};

	/**
	 * Creates a new lecture object
	 * @param {String} lectureName the lecture's display name
	 * @param {int} room the room's ID
	 * @param {int} course the course's ID
	 */
	this.Lecture = function Lecture(lectureName, room, course) {
		return {
			lectureName: lectureName,
			room: room,
			course: course
		};
	};

	/**
	 * Creates a current lecture object containing the real data (not only IDs)
	 * @param {int} lectureId the lecture's ID
	 * @returns {{
	 * 				lectureName: {String},
	 * 				room: {
	 * 					roomName: {String},
	 * 					width: {int},
	 * 					height: {int}
	 * 				},
	 * 				course: {
	 * 					courseName: {String},
	 * 					students: {Array({
	 * 						studentName: {String},
	 * 						group: {String}
	 *	 				})}
	 *	 			}
	 *	 		}}
	 */
	this.createCurrentLecture = function createCurrentLecture(lectureId) {
		var lectureObject = window.EduMon.Prefs.lectures[lectureId];
		var roomObject = window.EduMon.Prefs.rooms[lectureObject.room];
		var courseObject = window.EduMon.Prefs.courses[lectureObject.course];

		var students = [];

		courseObject.students.forEach(function(studentId) {
			students.push(window.EduMon.Prefs.students[studentId]);
		});

		courseObject.students = students;

		return {
			lectureName: lectureObject.lectureName,
			room: roomObject,
			course: courseObject
		};
	};
};
