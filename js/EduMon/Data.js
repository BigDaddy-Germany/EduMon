/*
 method createCurrentLecture depends on EduMon.Prefs
 */
EduMon.Data = new function() {
	/**
	 * Creates a new student object
	 *
	 * @param {string} name the student's name
	 * @param {string} group the group, the student is in
	 * @return {Object}
	 */
	this.Student = function Student(name, group) {
		return {
			name: name,
			group: group
		};
	};

	/**
	 * Creates a new course object
	 *
	 * @param {string} name A name to identify the course
	 * @param {Student[]} students
	 * @return {Object}
	 */
	this.Course = function Course(name, students) {
		return {
			name: name,
			students: students
		};
	};

	/**
	 * Creates a new room object
	 *
	 * @param {string} roomName The room's name
	 * @param {int} width The room's seats per row
	 * @param {int} height The room's rows
	 * @return {Object}
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
	 *
	 * @param {string} lectureName the lecture's display name
	 * @param {int} room the room's ID
	 * @param {int} course the course's ID
	 * @return {Object}
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
	 *
	 * @author Niko
	 * @return {Object}
	 */
	this.Timeline = function Timeline() {
		return {
			status: "stop", // "stop" | "play" | "pause"
				totalSeconds: 1, //start value 1 is intended
				start: "",
				started: false, //lecture started yet
				ended: false, //lecture over
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
	 *                lectureName: {string},
	 *                room: {
	 * 					roomName: {string},
	 * 					width: {int},
	 * 					height: {int}
	 * 				},
	 *                course: {
	 * 					name: {string},
	 * 					students: Array({Student})
	 *  			},
	 *            activeStudents: {}
	 *        }}
	 *
	 * @author Team
	 * @param {int} [lectureId] the lecture's ID
	 * @return
	 */
	this.createCurrentLecture = function(lectureId) {
		var lectureObject = undefined;
		var roomObject = undefined;
		var courseObject = undefined;
		var lectureName = undefined;

		if (lectureId !== undefined && EduMon.Prefs.lectures[lectureId]) {
			lectureObject = EduMon.Prefs.lectures[lectureId];
			roomObject = EduMon.Prefs.rooms[lectureObject.room];
			courseObject = EduMon.Prefs.courses[lectureObject.course];
			lectureName = lectureObject.lectureName;
			/*
			 we only need IDs inside the course, because it's only to start the broadcast once
			 Everything, which is needed to calculate or something similar should be copied
			 (like analytics or timeline)
			 */
		}

		return {
			lectureName: lectureName,
			room: roomObject,
			course: courseObject,
			activeStudents: {/*
				'44aa488f082b42f5fdc0090878f8ef3f': {
					name: 'Max Mustermann',
					group: 'Muster Group',
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
					],
					sentBreakRequest: false
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

				currentFeedbackId: 0,
				nextFeedbackId: 1,
				studentFeedback: {/*
					feedbackId: {
						type: "thumb"
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

			gui: {
				actionTime: -1,
				pultup: ""
			}
		};
	};

	/**
	 * Creates an empty packet (from "MODERATOR") with the common headers set
	 *
	 * @author Niko
	 * @param {int} type Packet type number
	 * @param {string} to Recipient
	 * @param {Object} body Packet body
	 * @return {Object}
	 */
	this.createBasePacket = function(type, to, body) {
		return {
			"type": +type,
			"id": ++EduMon.Prefs.messaging.outgoingPackageId,
			"time": EduMon.Util.timestampNow(),
			"from": "MODERATOR",
			"to": to,
			"room": EduMon.Prefs.currentLecture.room.roomName,
			"body": body
		};
	};

	/**
	 * Checks, whether the deletion of a resource is allowed
	 *
	 * @param {string} resourceType the resource's type
	 * @param {int} resourceId the resource's ID
	 * @param {string|boolean} allowedUser
	 * @return {int}
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
