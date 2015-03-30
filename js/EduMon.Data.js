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
		}
	};

	/**
	 * Creates a new course object
	 * @param {String} courseName A name to identify the course
	 * @param {int[]} students An array of all students (idx) in the course
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
};
