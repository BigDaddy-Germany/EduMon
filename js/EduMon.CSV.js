window.EduMon.CSV = new function CSV() {
	var that = this;

	/**
	 * Parses a given CSV String and returns it as an Array
	 * @param {String} csvString The String, which should be parsed
	 * @param {String} [separator=,] The CSV field separator
	 * @param {String} [delimiter=;] The CSV field delimiter
	 * @returns {Array} A nested array containing all rows and fields
	 */
	this.parseCsv = function parseCsv(csvString, separator, delimiter) {
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
	};


	/**
	 * Creates a course from a given CSV String
	 * @param {String} courseName the name, the new created course should get
	 * @param {String} csvString A String containing the CSV to import
	 * @param {String} [separator] The field separator of the CSV
	 * @param {String} [delimiter] The field delimiter of the CSV
	 * @param {Boolean} [headerLine=false] Does the CSV contain a header line?
	 * @param {int} [positionName=0] on which position is the name field?
	 * @param {int} [positionTeam=1] on which position is the team field?
	 * @returns {window.EduMon.Data.Course} The generated course
	 */
	this.createCourseFromCsv = function createCourseFromCsv(courseName, csvString, separator, delimiter, headerLine, positionName, positionTeam) {
		headerLine = headerLine || false;
		positionName = positionName || 0;
		positionTeam = positionTeam || 1;

		var students = [];
		var parsedCsv = that.parseCsv(csvString, separator, delimiter);

		parsedCsv.forEach(function (csvLine) {
			// skip first line, if headerLine is true
			if (headerLine) {
				headerLine = false;
			} else {
				students.push(new EduMon.Data.Student(csvLine[positionName], csvLine[positionTeam]));
			}
		});

		return new EduMon.Data.Course(courseName, students);
	};
};
