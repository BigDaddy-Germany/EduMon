/*
	method createCourseFromCsv depends on EduMon.Data
 */
/**
 * @author Marco
 */
EduMon.CSV = new function() {
	/**
	 * Parses a given CSV String and returns it as an Array
	 * @param {string} csvString The String, which should be parsed
	 * @param {string} [separator=,] The CSV field separator
	 * @param {string} [delimiter=;] The CSV field delimiter
	 * @returns {Array} A nested array containing all rows and fields
	 */
	this.parse = function(csvString, separator, delimiter) {
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
					csvArray[row][col] = csvArray[row][col].trim();
					++col;
					continue;
				}

				if (char == '\r' && nextChar == '\n') {
					// windows uses \r\n -> two chars -> skip next char
					++charNum;
				}
				if (char == '\r' || char == '\n') {
					// linebreak indicates new row, starting at col 0
					csvArray[row][col] = csvArray[row][col].trim();
					++row;
					col = 0;
					continue;
				}

				if (char == delimiter) {
					// got a delimiter char? start new quoted field
					inQuotes = true;
					continue;
				}

				// everything else can be added to the field
				csvArray[row][col] += char;
			}
		}

		if (csvArray[row] && csvArray[row][col]) {
			csvArray[row][col] = csvArray[row][col].trim();
		}

		return csvArray;
	};
};
