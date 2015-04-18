EduMon = window.EduMon || {};
/**
 * This class hols the logic and state for the wheel of fortune which can be used to select any of its segments.
 *
 * The implementation (especially the drawing code) is based on http://bramp.net/javascript/lunchwheel.html
 * heavily modified though
 *
 * @param {HTMLCanvasElement} canvas the canvas to draw into
 * @param {Array} segments the initial segments (as pairs of (name,weight))
 * @constructor
 */
EduMon.Wheel = function(canvas, segments) {

	var that = this;
	var context = canvas.getContext('2d');

	var PI = Math.PI;
	var TAU = 2 * PI;

	var timer = -1;
	var targetFPS = 60;

	var RPS = TAU / targetFPS;
	var targetVelocity = 3.5 * RPS;
	var speedUp  =  .1 * RPS;
	var slowDown = -.08 * RPS;

	var currentAngle = 0;
	var velocity = 0;
	var acceleration = speedUp;


	var colors = ['#000000', '#ffff00', '#ffc700', '#ff9100', '#ff6301', '#ff0000', '#c6037e', '#713697', '#444ea1', '#2772b2', '#0297ba', '#008e5b', '#8ac819'];
	var segmentColors = [];

	var size = 290;
	var centerX = 300;
	var centerY = 300;

	var weights = 0;
	var unit = 1;
	var activeSegment = null;

	this.onFinish = null;

	setAndShuffledSegments(segments);
	updateState(true);

	/**
	 * This function updates the internal rendering state
	 *
	 * @param {boolean} [resetAngle=true] true to reset the rotation of the wheel and start at the first segment
	 */
	function updateState(resetAngle) {
		activeSegment = null;
		updateUnit();
		if (!!resetAngle) {
			currentAngle = TAU - 0.5 * unit * segments[0][1] - (TAU/segments.length);
		}

		// Generate a color cache (so we have consistent coloring)
		var newSegmentColors = [];
		for (var i = 0; i < segments.length; i++) {
			var color = colors[modulo(hashString(segments[i][0]), colors.length)];
			newSegmentColors.push([color, readableForegroundFor(color)]);
		}
		segmentColors = newSegmentColors;

		drawWheel();
	}

	/**
	 * This functions set a copy of the given segment array and shuffles it
	 *
	 * @param {Array} newSegments the new segments to set
	 */
	function setAndShuffledSegments(newSegments) {
		if (!newSegments || newSegments.length < 1) {
			throw "No segments given!";
		}
		segments = newSegments.slice();
		shuffle(segments);
	}

	/**
	 * Sets the new segments, updates the internal state and optionally resets the rotation of the wheel.
	 *
	 * @param newSegments the list of new segments
	 * @param {boolean} [reset=true] true to reset the rotation of the wheel and start at the first segment
	 */
	this.setSegments = function(newSegments, reset) {
		setAndShuffledSegments(newSegments);
		updateState(!!reset);
	};

	/**
	 * This function removes a segment either by name or by itself.
	 *
	 * @param {Array|string} segment either a segment or the name of a segment
	 */
	this.removeSegment = function(segment) {
		var i = segments.indexOf(segment);
		if (i < 0) {
			for (var n = 0; n < segments.length; ++n) {
				if (segments[n][0] == segment) {
					i = n;
				}
			}
		}
		if (i >= 0) {
			segments.splice(i, 1);
		}
	};


	/**
	 * This function calculates a rotation unit based on the segment weights
	 */
	function updateUnit() {
		if (segments.length == 0) {
			throw "No segments supplied!";
		}
		if (segments.length === 1) {
			weights = segments[0][1];
		} else {
			weights = EduMon.Math.sumOver(0, segments.length, function (i) {
				return segments[i][1]
			});
		}
		unit = TAU / weights;
	}

	/**
	 * This function calculates a readable foreground color for a given background color.
	 * Internally the HSV color model is used to calculate a color based on the saturation and lightness.
	 *
	 * @param {string} color a standard 6-digit hex color code prefixed by a hash (#NNNNNN)
	 * @returns {string} a standard 6-digit hex color code prefixed by a hash (#NNNNNN)
	 */
	function readableForegroundFor(color) {
		var red = (parseInt(color.substring(1, 3), 16));
		var green = (parseInt(color.substring(3, 5), 16));
		var blue = (parseInt(color.substring(5), 16));

		var hsv = EduMon.Math.rgbToHsv(red, green, blue);
		var rgb = EduMon.Math.hsvToRgb(modulo((hsv[0] + 180), 360), hsv[1], hsv[1] < .5 ? 1 : 0);

		return stringifyRGB.apply(stringifyRGB, rgb);
	}

	/**
	 * This function converts the 3 separate components of RGB colors to a standard 6-digit hex color code prefixed by
	 * a hash (#NNNNNN).
	 *
	 * @param {int} red
	 * @param {int} green
	 * @param {int} blue
	 * @returns {string} a standard 6-digit hex color code prefixed by a hash (#NNNNNN)
	 */
	function stringifyRGB(red, green, blue) {
		var redString = EduMon.Util.padLeft(red.toString(16), '0', 2);
		var greenString = EduMon.Util.padLeft(green.toString(16), '0', 2);
		var blueString = EduMon.Util.padLeft(blue.toString(16), '0', 2);

		return "#" + redString + greenString + blueString;
	}

	/**
	 * Ths function generates a list of colors by going around the HSV hue circle
	 *
	 * @param {number} step the step size
	 * @returns {Array} the list of colors
	 */
	function generateColorsHSV(step) {
		step = step || 30;

		var colors = [];
		for (var hue = 0; hue < 360; hue += step) {
			var c = stringifyRGB.apply(stringifyRGB, (EduMon.Math.hsvToRgb(hue, 1, 1)));
			if (colors.indexOf(c) < 0) {
				colors.push(c);
			}
		}
		return colors;
	}

	/**
	 * Ths function generates a list of colors by iterating all RGB color components
	 *
	 * @param {number} step the step size
	 * @returns {Array} the list of colors
	 */
	function generateColorsRGB(step) {
		step = step || 128;
		var base = [];
		var i = 0;
		do {
			base.push(Math.min(i, 255));
			i += step;
		} while (i <= 256);

		var colors = [];
		for (var r = 0; r < base.length; ++r) {
			for (var g = 0; g < base.length; ++g) {
				for (var b = 0; b < base.length; ++b) {
					colors.push(stringifyRGB(base[r], base[g], base[b]))
				}
			}
		}
		return colors;
	}

	/**
	 * A fixed modulo function.
	 * Based on: http://javascript.about.com/od/problemsolving/a/modulobug.htm (not implemented as a polyfill)
	 *
	 * @param {number} a the left modulo operand
	 * @param {int} n the right modulo operand
	 * @returns {number} the modulo result
	 */
	function modulo(a, n) {
		return ((a % n) + n) % n;
	}

	/**
	 * This function shuffles a given array and returns the shuffled version.
	 *
	 * @param {Array} array the array to shuffle
	 * @returns {Array} the shuffled array
	 */
	function shuffle(array) {
		var counter = array.length, temp, index;

		// While there are elements in the array
		while (counter > 0) {
			// Pick a random index
			index = Math.floor(Math.random() * counter);

			// Decrease counter by 1
			counter--;

			// And swap the last element with it
			temp = array[counter];
			array[counter] = array[index];
			array[index] = temp;
		}

		return array;
	}

	/**
	 * Calculates the hash code
	 *
	 * @param {String} a the input string
	 * @returns {number} the string hash code
	 */
	function hashString(a) {
		// See http://www.cse.yorku.ca/~oz/hash.html
		var hash = 5381;
		for (var i = 0; i < a.length; i++) {
			var char = a.charCodeAt(i);
			hash = ((hash << 5) + hash) + char;
			hash = hash & hash; // Convert to 32bit integer
		}
		return hash;
	}

	/**
	 * This function starts the spinning of the wheel
	 */
	this.beginSpinning = function () {

		// Start the wheel only if it's not already spinning
		if (timer == -1) {

			updateState();

			if (segments.length === 1) {
				currentAngle = PI;
				drawWheel();
				return;
			}

			velocity = 0;
			acceleration = speedUp;

			timer = setInterval(onTick, Math.round(1000 / targetFPS));
		}
	};

	/**
	 * This function ends the spinning of the wheel by letting it slow down
	 */
	this.endSpinning = function() {
		acceleration = slowDown;
	};

	/**
	 * This function instantly stops the wheel
	 */
	this.stopSpinning = function() {
		if (timer != -1) {
			clearInterval(timer);
			acceleration = 0;
			velocity = 0;

			timer = -1;

			if (typeof that.onFinish == 'function') {
				that.onFinish(activeSegment);
			}
		}
	};


	/**
	 * This is the internal tick function (read as "main-loop")
	 */
	function onTick() {
		drawWheel();

		velocity += acceleration;
		if (velocity > targetVelocity) {
			velocity = targetVelocity;
			acceleration = 0;
		}

		if (velocity < 0) {
			that.stopSpinning();
			return;
		}

		currentAngle += velocity;
		currentAngle = modulo(currentAngle, 360);
	}

	/**
	 * This function draws the entire wheel
	 */
	function drawWheel() {
		context.clearRect(0, 0, canvas.width, canvas.height);

		context.lineWidth = 1;
		context.strokeStyle = '#000000';
		context.textBaseline = "middle";
		context.textAlign = "center";
		context.font = "1.4em Arial";

		var lastAngle = currentAngle;
		var len = segments.length;

		for (var i = 0; i < len; i++) {
			lastAngle = drawSegment(segments[i], segmentColors[i], lastAngle);
		}

		// Draw a center circle
		context.beginPath();
		context.arc(centerX, centerY, 20, 0, TAU, false);
		context.closePath();

		context.fillStyle = '#ffffff';
		context.strokeStyle = '#000000';
		context.fill();
		context.stroke();

		// Draw outer circle
		context.beginPath();
		context.arc(centerX, centerY, size, 0, TAU, false);
		context.closePath();

		context.lineWidth = 10;
		context.strokeStyle = '#000000';
		context.stroke();

		// draw the needle
		context.lineWidth = 1;
		context.strokeStyle = '#000000';
		context.fillStyle = '#ffffff';

		context.beginPath();

		context.moveTo(centerX + size - 40, centerY);
		context.lineTo(centerX + size + 20, centerY - 10);
		context.lineTo(centerX + size + 20, centerY + 10);
		context.closePath();

		context.stroke();
		context.fill();
	}

	/**
	 * This function draws a single segment and if it is the selected segment it will also draw the active segment's
	 * name.
	 *
	 * @param {Array} segment the segment to draw
	 * @param {Array} color the colors (a pair: (background,foreground))
	 * @param {float} startAngle the angle (in radians) to draw the segment at
	 * @returns {float} the end angle of the segment.
	 */
	function drawSegment(segment, color, startAngle) {
		var text = segment[0];
		var weight = segment[1];

		var endAngle = startAngle + weight * unit;

		context.save();
		context.beginPath();

		// Start in the centre
		context.moveTo(centerX, centerY);
		context.arc(centerX, centerY, size, startAngle, endAngle, false); // Draw a arc around the edge
		context.lineTo(centerX, centerY); // Now draw a line back to the centre

		// Clip anything that follows to this area
		//ctx.clip(); // It would be best to clip, but we can double performance without it
		context.closePath();

		var background = color[0];
		var foreground = color[1];

		context.fillStyle = background;
		context.fill();
		context.stroke();

		// Now draw the text
		context.save(); // The save ensures this works on Android devices
		context.translate(centerX, centerY);
		context.rotate((startAngle + endAngle) / 2);

		context.fillStyle = foreground;
		context.fillText(text.substr(0, 20), size / 2 + 20, 0);
		context.restore();

		if (modulo(startAngle, TAU) >= modulo(endAngle, TAU)) {
			drawActive(text, startAngle, endAngle);
			activeSegment = segment;
		}

		context.restore();

		return endAngle;
	}

	/**
	 * This function draws the active segment's name beside the needle
	 * @param text
	 */
	function drawActive(text) {
		// Now draw the winning name
		context.textAlign = "left";
		context.textBaseline = "middle";
		context.fillStyle = '#000000';
		context.font = "2em Arial";
		context.fillText(text, centerX + size + 25, centerY);
	}
};
