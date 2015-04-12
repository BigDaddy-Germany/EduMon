/*
	method createCurrentLecture depends on EduMon.Prefs
 */
EduMon.Data = new function() {
	/**
	 * Creates a new student object
	 * @param {String} name the student's name
	 * @param {String} group the group, the student is in
	 */
	this.Student = function Student(name, group) {
		return {
			name: name,
			group: group
		};
	};

	/**
	 * Creates a new course object
	 * @param {String} name A name to identify the course
	 * @param {Student[]} students An array of all students in the course
	 */
	this.Course = function Course(name, students) {
		return {
			name: name,
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
	 * Creates a new timeline object
	 */
	this.Timeline = function Timeline() {
		return {
			status: "stop", // "stop" | "play" | "pause"
				totalSeconds: 1, //start value 1 is intended
				start: "",
				slices: [/*
							Elements:
							seconds: 1337,
							type: "lecture" | "break",
							end: "12:30"
							*/]
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
	 * 					name: {String},
	 * 					students: Array({Student})
	 *	 			},
	 *	 			activeStudents: {}
	 *	 		}}
	 */
	this.createCurrentLecture = function(lectureId) {
		if (EduMon.Prefs.lectures[lectureId]) {
			var lectureObject = EduMon.Prefs.lectures[lectureId];
			var roomObject = EduMon.Prefs.rooms[lectureObject.room];
			var courseObject = EduMon.Prefs.courses[lectureObject.course];
			/*
				 we only need IDs inside the course, because it's only to start the broadcast once
				 Everything, which is needed to calculate or something similar should be copied
				 (like analytics or timeline)
			 */

			return {
				lectureName: lectureObject.lectureName,
				room: roomObject,
				course: courseObject,
				activeStudents: {/*
					'44aa488f082b42f5fdc0090878f8ef3f': {
						name: 'Steyer',
						group: 'ShitGroup',
						seat: {x: 3, y: 2},
						disturbance: 0,
						history: [
							{
								time: 1234566,
								microphone: 12,
								keyboard: 13,
								mouseDistance: 145,
								mouseClicks: 13
							}
						],
						micHistory: [
							{
								time: 123456,
								value: 123
							}
						]
					},

					SESSID: { fancy stuff like above }
				*/},

				seatingPlan: [/*
					// access via seatingPlan[x][y]
					5: [
						3: sessionId
					]
				*/],

				timeline: new EduMon.Data.Timeline(),

				analytics: {
					globalReferenceValues: {/*
						sender: {
							microphone: 12,
							keyboard: 32,
							mouseDistance: 312,
							mouseClicks: 123
						}
					*/},

					studentFeedback: {/*
						feedbackId: {
							time: 123456,
							currentAverage: 0.175
							studentVoting: {
								sender1: 0.12,
								sender2: 0.23
							}
						}
					*/},

					breakRequests: 0
				},

				messaging: {
					outgoingPackageId: 1,
					serverUrl: "http://vps2.code-infection.de/edumon/mailbox.php",
					moderatorPassphrase: "alohomora"
				}
			};
		}
	};

	// todo comment me
	this.createBasePacket = function(type, to, body){
		return {
			"type":+type,
				"id":++EduMon.Prefs.currentLecture.messaging.outgoingPackageId,
				"time":Math.floor(Date.now()/1000),
				"from":"MODERATOR",
				"to":to,
				"room":EduMon.Prefs.currentLecture.room.roomName,
				"body":body
		};
	};

	/**
	 * Checks, whether the deletion of a resource is allowed
	 * @param {String} resourceType the resource's type
	 * @param {int} resourceId the resource's ID
	 * @param {int|boolean} [allowedUser=false] this user is allowed and causes no deletion error
	 * @return {int} the calculated error code (1: last one, 2: still used)
	 */
	this.checkDeletionErrors = function(resourceType, resourceId, allowedUser) {
		allowedUser = allowedUser || false;

		var identifyingKey;
		var potentialUser;
		var storageLocation;

		switch (resourceType) {
			case 'room':
				identifyingKey = 'room';
				potentialUser = EduMon.Prefs.lectures;
				storageLocation = EduMon.Prefs.rooms;
				break;

			case 'course':
				identifyingKey = 'course';
				potentialUser = EduMon.Prefs.lectures;
				storageLocation = EduMon.Prefs.courses;
				break;

			case 'lecture':
				identifyingKey = '';
				potentialUser = [];
				storageLocation = EduMon.Prefs.lectures;
				break;

			default:
				throw 'Unknown resource type';
				break;
		}

		if (storageLocation.length < 2) {
			return 1;
		}

		for (var i = 0; i < potentialUser.length; ++i) {
			if (potentialUser[i][identifyingKey] == resourceId && i != allowedUser) {
				return 2;
			}
		}

		return 0;
	};

};
