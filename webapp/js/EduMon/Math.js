EduMon = window.EduMon || {};

/**
 * A collections of advanced mathematical functions implemented in a functional style
 *
 * @author Marco
 * @author Phillip
 */
EduMon.Math = new function() {
	var that = this;

	/**
	 * Interpolates a polynomial with Lagrange
	 * @param {... Array} [points] The points to interpolate with Lagrange
	 * @return {Function}
	 */
	this.interpolatePolynomialByLagrange = function(points) {
		function createLagrangePolynomial(k, points) {
			return function(x) {
				return that.productOver(0, n, function(i) {
					if (i != k) {
						return (x - points[i][0]) / (points[k][0] - points[i][0]);
					}
					return 1;
				});
			};
		}

		points = Array.prototype.slice.call(arguments);
		var n = points.length;
		var lagrangePolynomials = [];

		for (var k = 0; k < n; k++) {
			lagrangePolynomials.push(createLagrangePolynomial(k, points));
		}

		return function(x) {
			return that.sumOver(0, n, function(i) {
				return points[i][1] * lagrangePolynomials[i](x);
			});
		};
	};

	/**
	 * Create a function, which is linear over intervals using the given points
	 * @param {... Array} [points] the given points
	 * @return {Function} the calculated function
	 */
	this.linearIntervalFunction = function(points) {
		var values = Array.prototype.slice.call(arguments);
		values.sort(function(a, b) {
			if (a[0] > b[0]) {
				return 1;
			}
			if (a[0] < b[0]) {
				return -1;
			}
			return 0;
		});

		// generate the linear functions
		var functions = [];
		for (var i = 0; i < values.length - 1; ++i) {
			var p1 = values[i];
			var p2 = values[i + 1];

			functions.push([p1[0], p2[0], that.linearFunction(p1, p2)]);
		}

		return function(x) {
			// for each x: choose the right function and calculate the value
			for (var i = 0; i < functions.length; ++i) {
				if (x >= functions[i][0] && x <= functions[i][1]) {
					return functions[i][2](x);
				}
			}
			throw 'Given x is not in defined range.';
		}
	};

	/**
	 * Returns a linear function going through p1 and p2
	 * @param {Array} p1 the point p1
	 * @param {Array} p2 the point p2
	 */
	this.linearFunction = function(p1, p2) {
		var m = (p2[1] - p1[1]) / (p2[0] - p1[0]);
		var b = p1[1] - m * p1[0];

		return function(x) {
			return m * x + b;
		};
	};

	/**
	 * Calculates the average value of a given Array of numbers
	 * @param {Array} values The values to calculate the average
	 * @returns {number} The calculated average
	 */
	this.arithmeticAverage = function(values) {
		if (values.length == 0) {
			return 0;
		}
		var sum = that.sumOver(0, values.length, function(i) {
			return values[i];
		});

		return sum / values.length;
	};

	/**
	 * Calculates the standard deviation of a given array of values
	 * @param {Array} values the values
	 * @returns {number} the given value's standard deviation
	 */
	this.standardDeviation = function(values) {
		return Math.sqrt(that.variance(values));
	};

	/**
	 * Calculates the variance of a given array of values
	 * @param {Array} values the values
	 * @returns {number} the given value's variance
	 */
	this.variance = function(values) {
		var expectancyValue = that.arithmeticAverage(values);

		var sum = that.sumOver(0, values.length, function(i) {
			return Math.pow(values[i] - expectancyValue, 2);
		});

		return 1 / (values.length - 1) * sum;
	};

	/**
	 * Calculates the Gini Index of given values
	 * @param {Array} values the given values
	 * @return {number} the calculated gini index
	 */
	this.giniIndex = function(values) {
		var n = values.length;

		var upperSum = 2 * that.sumOver(0, n, function(k) {
				return (k + 1) * values[k];
			});
		var lowerSum = n * that.sumOver(0, n, function(k) {
				return values[k];
			});

		return upperSum / lowerSum - (n + 1) / n;
	};

	/**
	 * Calculates the maximum of an array using the native Math.max
	 * @param {Array} values the given values
	 * @return {number} the calculated maximum
	 */
	this.max = function(values) {
		return that.extremeValue(values, function(a, b) {
			if (a > b) {
				return 1;
			}
			if (b > a) {
				return -1;
			}
			return 0;
		});
	};

	/**
	 * Calculates the minimum of an array using the native Math.max
	 * @param {Array} values the given values
	 * @return {number} the calculated minimum
	 */
	this.min = function(values) {
		return that.extremeValue(values, function(a, b) {
			if (a > b) {
				return -1;
			}
			if (b > a) {
				return 1;
			}
			return 0;
		});
	};

	/**
	 * Calculates the extreme value ("biggest" in case of the compare function) of a given array of values
	 * @param {Array} values the values
	 * @param {Function} compare the compare function
	 * @return {*} The extreme value
	 */
	this.extremeValue = function(values, compare) {
		values = values.slice(0);
		values.sort(compare);

		return values[values.length - 1];
	};

	/**
	 * Calculates the mathematical sum
	 * @param {int} from the start value
	 * @param {int} to the end value (exclusive)
	 * @param {Function} functor the function to apply in each step
	 * @return {number} the calculated sum
	 */
	this.sumOver = function(from, to, functor) {
		return that.applyOver(from, to, function(a, b) {
			return a + b;
		}, functor);
	};

	/**
	 * Calculates the mathematical product
	 * @param {int} from the start value
	 * @param {int} to the end value (exclusive)
	 * @param {Function} functor the function to apply in each step
	 * @return {number} the calculated product
	 */
	this.productOver = function(from, to, functor) {
		return that.applyOver(from, to, function(a, b) {
			return a * b;
		}, functor);
	};

	/**
	 * Calculates the mathematical sum
	 * @param {int} from the start value
	 * @param {int} to the end value (exclusive)
	 * @param {Function} predicate the predicate to apply to each interim result
	 * @param {Function} functor the function to apply in each step
	 * @return {number} the result
	 */
	this.applyOver = function(from, to, predicate, functor) {
		if (from + 1 == to) {
			return functor(from);
		}
		var currentValue = predicate(functor(from++), functor(from++));

		for (; from < to; from++) {
			currentValue = predicate(currentValue, functor(from));
		}

		return currentValue;
	};

	/**
	 * Calculates the log[basis](x)
	 * @param {number} x The number
	 * @param {number} [basis=10] The basis
	 * @return {number}
	 */
	this.log = function(x, basis) {
		basis = basis || 10;
		return Math.log(x) / Math.log(basis);
	};

	/**
	 * Generates an random integer
	 * @param {int} lowerLimit the lowest number
	 * @param {int} upperLimit the highest number
	 * @return {int} the generated random number
	 */
	this.randomInteger = function(lowerLimit, upperLimit) {
		return Math.floor(Math.random() * (upperLimit - lowerLimit)) + lowerLimit;
	};

	/**
	 * Converts a RGB color to the HSV format
	 *
	 * @param {int} red the red value
	 * @param {int} green the green value
	 * @param {int} blue the blue value
	 * @returns {*[]} an array with the 3 HSV components
	 */
	this.rgbToHsv = function(red, green, blue) {
		red /= 255;
		green /= 255;
		blue /= 255;

		var max = EduMon.Math.max([red, green, blue]);
		var min = EduMon.Math.min([red, green, blue]);

		var delta = max - min;

		var hue = 0;
		var saturation = 0;
		var value = max;

		if (min === max) {
			hue = 0;
		} else if (max === red) {
			hue = 60 * (((green - blue) % 6) / delta);
		} else if (max === green) {
			hue = 60 * ((blue - red) / delta + 2);
		} else if (max === blue) {
			hue = 60 * ((red - green) / delta + 4);
		} else {
			throw "Hue calculation failed!"
		}

		if (max > 0) {
			saturation = delta / max;
		}

		if (hue < 0) {
			hue += 360;
		}

		return [hue, saturation, value]
	};

	/**
	 * Converts a HSV formatted color to the RGB format.
	 *
	 * @param hue the hoe component
	 * @param saturation the saturation component
	 * @param value the value components
	 * @returns {*[]} an array with the 3 RGB components
	 */
	this.hsvToRgb = function(hue, saturation, value) {

		hue = Math.floor(hue / 60);

		var c = value * saturation;
		var x = c * (1 - Math.abs((hue % 2) - 1));
		var m = value - c;

		var r = 0;
		var g = 0;
		var b = 0;

		switch (Math.floor(hue)) {
			case 0:
				r = c;
				g = x;
				b = 0;
				break;
			case 1:
				r = x;
				g = c;
				b = 0;
				break;
			case 2:
				r = 0;
				g = c;
				b = x;
				break;
			case 3:
				r = 0;
				g = x;
				b = c;
				break;
			case 4:
				r = x;
				g = 0;
				b = c;
				break;
			case 5:
				r = c;
				g = 0;
				b = x;
				break;
			default:
				throw "HSV to RGB conversion failed!";
		}

		return [Math.round((r + m) * 255), Math.round((g + m) * 255), Math.round((b + m) * 255)];
	};
};
