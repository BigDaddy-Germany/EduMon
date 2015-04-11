EduMon.Math = new function() {
    var that = this;

    /**
     * Interpolates a polynomial with Lagrange
     * @param {... Array} - The points to interpolate with Lagrange
     * @return {Function}
     */
    this.interpolatePolynomialByLagrange = function() {
        function createLagrangePolynomial(k, points) {
            return function (x) {
                return that.productOver(0, n, function(i) {
                    if (i != k) {
                        return (x - points[i][0]) / (points[k][0] - points[i][0]);
                    }
                    return 1;
                });
            };
        }

        var points = arguments;
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

        return sum/values.length;
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

        return 1/(values.length - 1) * sum;
    };


    /**
     * Calculates the Gini Index of given values
     * @param {Array} values the given values
     * @return {number} the calculated gini index
     */
    this.giniIndex = function(values) {
        var n = values.length;

        var upperSum = 2 * that.sumOver(0, n, function(k) { return (k+1) * values[k]; });
        var lowerSum = n * that.sumOver(0, n, function(k) { return values[k]; });

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
     * @return {*|T} The extreme value
     */
    this.extremeValue = function(values, compare) {
        values = values.slice(0);
        values.sort(compare);

        return values[values.length-1];
    };


    /**
     * Calculates the mathematical sum
     * @param {int} from the start value
     * @param {int} to the end value (exclusive)
     * @param {Function} functor the function to apply in each step
     * @return {number} the calculated sum
     */
    this.sumOver = function(from, to, functor) {
        return that.applyOver(from, to, function(a, b) { return a + b; }, functor);
    };


    /**
     * Calculates the mathematical product
     * @param {int} from the start value
     * @param {int} to the end value (exclusive)
     * @param {Function} functor the function to apply in each step
     * @return {number} the calculated product
     */
    this.productOver = function(from, to, functor) {
        return that.applyOver(from, to, function(a, b) { return a * b; }, functor);
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
        if (from == to) {
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
    }
};
