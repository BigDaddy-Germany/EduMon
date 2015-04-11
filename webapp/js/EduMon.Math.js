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
                var returnValue = 1;
                for (var i = 0; i < n; i++) {
                    if (i != k) {
                        returnValue *= (x - points[i][0]) / (points[k][0] - points[i][0]);
                    }
                }
                return returnValue;
            };
        }

        var points = arguments;
        var n = points.length;
        var lagrangePolynomials = [];

        for (var k = 0; k < n; k++) {
            lagrangePolynomials.push(createLagrangePolynomial(k, points));
        }

        return function(x) {
            var returnValue = 0;
            for (var i = 0; i < n; i++) {
                returnValue += points[i][1] * lagrangePolynomials[i](x);
            }
            return returnValue;
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
        var sum = 0;
        values.forEach(function(value) {
            sum += value;
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

        var insideSum = 0;
        values.forEach(function(value) {
            insideSum += Math.pow(value - expectancyValue, 2);
        });

        return 1/(values.length - 1) * insideSum;
    };
};
