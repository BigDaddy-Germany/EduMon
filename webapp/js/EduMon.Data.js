window.EduMon.Data = new function Data() {
	/**
	 * Class to represent a student
	 * @param {String} studentName the student's name
	 * @param {String} group the group, the student is in
	 */
	this.Student = function Student(studentName, group) {
		this.studentName = studentName;
		this.group = group;
	};

	/**
	 * Class to represent a course
	 * @param {String} courseName A name to identify the course
	 * @param {Student[]} students An array of all students inside the course
	 */
	this.Course = function Course(courseName, students) {
		this.courseName = courseName;
		this.students = students;
	};
};
