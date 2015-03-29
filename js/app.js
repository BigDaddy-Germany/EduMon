/**
 * Class to represent a student
 * @param {String} studentName the student's name
 * @param {String} group the group, the student is in
 */
function Student(studentName, group) {
	this.studentName = studentName;
	this.group = group;
}

/**
 * Class to represent a course
 * @param {String} courseName A name to identify the course
 * @param {Student[]} students An array of all students inside the course
 */
function Course(courseName, students) {
	this.courseName = courseName;
	this.students = students;
}

/**
 * Parses a given CSV String and returns it as an Array
 * @param {String} csvString The String, which should be parsed
 * @param {String} [separator=,] The CSV field separator
 * @param {String} [delimiter=;] The CSV field delimiter
 * @returns {Array} A nested array containing all rows and fields
 */
function parseCsv(csvString, separator, delimiter) {
	var csvArray = [];				// will be returned later
	var row = 0, col = 0;			// Where am I?
	var inQuotes = false;			// Am I inside quotes?

	separator = separator || ',';	// field separator, default ','
	delimiter = delimiter || '"';	// field separator, default '"'

	// go over all chars
	for (var charNum = 0; charNum < csvString.length; ++charNum) {
		// we need the current char every time
		var char = csvString[charNum];
		// sometimes, we also need the next char to make a decision (windows line break, escape chars)
		var nextChar = csvString[charNum + 1];

		csvArray[row] = csvArray[row] || [];			// create new row, if needed
		csvArray[row][col] = csvArray[row][col] || '';	// create new col, if needed

		if (inQuotes) {
			// we are inside a quoted field

			if (char == nextChar && char == delimiter) {
				// we got an escaped delimiter char (e.g. double quotes)
				csvArray[row][col] += char;
				++charNum;	// ignore next char
				continue;
			}

			if (char == delimiter) {
				// we are not inside quotes, anymore
				inQuotes = false;
				continue;
			}

			// By default, add next char to field
			csvArray[row][col] += char;

		} else {
			// we are not inside a quoted field

			if (char == separator) {
				// got a separator? next col
				++col;
				continue;
			}

			if (char == '\r' && nextChar == '\n') {
				// windows uses \r\n -> two chars -> skip next char
				++charNum;
			}
			if (char == '\r' || char == '\n') {
				// linebreak indicates new row, starting at col 0
				++row;
				col = 0;
				continue;
			}

			if (char == delimiter) {
				// got a delimiter char? start new quoted field
				inQuotes = true;
				continue;
			}

			if (char != ' ' && char != '\t') {
				// everything, which is no blank or tab can be added to the field
				csvArray[row][col] += char;
			}
		}
	}

	return csvArray;
}

/**
 * Creates a course from a given CSV String
 * @param {String} courseName the name, the new created course should get
 * @param {String} csvString A String containing the CSV to import
 * @param {String} [separator] The field separator of the CSV
 * @param {String} [delimiter] The field delimiter of the CSV
 * @param {Boolean} [headerLine=false] Does the CSV contain a header line?
 * @param {int} [positionName=0] on which position is the name field?
 * @param {int} [positionTeam=1] on which position is the team field?
 * @returns {Course} The generated course
 */
function createCourseFromCsv(courseName, csvString, separator, delimiter, headerLine, positionName, positionTeam) {
	headerLine = headerLine || false;
	positionName = positionName || 0;
	positionTeam = positionTeam || 1;

	var students = [];
	var parsedCsv = parseCsv(csvString, separator, delimiter);

	parsedCsv.forEach(function(csvLine) {
		// skip first line, if headerLine is true
		if (headerLine) {
			headerLine = false;
		} else {
			students.push(new Student(csvLine[positionName], csvLine[positionTeam]));
		}
	});

	return new Course(courseName, students);
}



(function() {

	/* Packet handling - Messager class connects with message worker */
	function Messenger(eventCallback){
		var that = this;

		////////////////////private vars
		var _worker;

		////////////////////public vars

		////////////////////private methods

		////////////////////public methods

		/* Send command to message worker */
		this.sendEvent = function(event){
			_worker.postMessage(event);
		};


		/* Destroy message worker */
		this.kill = function(){
			_worker.terminate();
		};


		////////////////////Constructor
		_worker = new Worker('js/app.messageworker.js');
		_worker.callbackWrapper = eventCallback;
		_worker.onmessage = function(e) {
			this.callbackWrapper(e.data);
		};

	}



	/* Main app class */
	function EduMon() {
		var that = this;

		////////////////////private vars
		var session_id = ""; //session id assinged by message server //TODO: necessary?

		////////////////////public vars
		this.show_debug = true; //show debug messages in javascript console

		////////////////////private methods

		////////////////////public methods

		/* Debug output to JS console */
		this.debug = function(msg){
			if (that.show_debug){
				console.log(msg);
			}
		};


		/* EduMon startup */
		this.init = function(){
			that.debug("*** All Glory to the EduMon! ***");
			that.debug("EduMon awakening...");
			that.Messenger = new Messenger(this.handleIncomingData);
		};


		/* Handle incoming data */
		this.handleIncomingData = function(event){
			//Process packets received by message server
			if ("inbox" in event && event.inbox.length > 0){
				for (var i = 0; i < event.inbox.length; ++i) {
					that.processPacket(event.inbox[i]);
				}
			}

			//Handle any errors
			if ("errorMessages" in event && event.errorMessages.length > 0){
				for (i = 0; i < event.errorMessages.length; ++i) {
					that.debug(event.errorMessages[i]);
				}
			}

			//Save session id assigned by message server
			if ("clientId" in event){
				that.session_id = event.clientId;
			}
		};


		/* Process packet */
		this.processPacket = function(packet){
			//TODO to be implemented
			that.debug("Received packet:");
			that.debug(packet);
		};


		/* [DEV] Send demo packet */
		this.sendDemo = function(typenumber){
			jQuery.getJSON("js/demoDataOut.json",function(data){
				that.sendPacket(data["demo_type_"+typenumber]);
			});
		};


		/* Queue packet for sending */
		this.sendPacket = function(packet){
			that.cmdConnection(packet);
		};


		/* Command message worker */
		this.cmdConnection = function(cmd){
			that.Messenger.sendEvent(cmd);
		};


		////////////////////Constructor
		//moved to init to ensure dom-ready start
	}



	window.EduMon = new EduMon();

})();
