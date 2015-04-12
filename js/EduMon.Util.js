EduMon.Util = new function() {

    /**
     * Iterates over a field and calls the given functor
     * @param {object} object the object ot iterate over
     * @param {Function} functor the functor to call (first argument is the field's name, the second one the content)
     */
    this.forEachField = function(object, functor) {
        for (propertyName in object) {
            if (object.hasOwnProperty(propertyName)) {
                functor(propertyName, object[propertyName]);
            }
        }
    };


    /**
     * Counts the number of a given objects own properties
     * @param {object} object with attributes to count
     */
    this.countFields = function(object) {
		var count = 0;
		this.forEachField(object,function(){count++;});
		return count;
    };


    /**
     * BitSet class to use bitwise operators without thinking about them
     * @param {int} [startValue=0] the start value
     */
    this.BitSet = function(startValue) {
        var that = this;
        this.bits = startValue || 0;

        /**
         * Sets given bits
         * @param {int} b the given bits
         */
        this.set = function(b) {
            that.bits |= b;
            return this;
        };

        /**
         * Clears given bits
         * @param {int} b the given bits
         */
        this.clear = function(b) {
            that.bits &= ~b;
            return this;
        };

        /**
         * Checks, whether given bits are set
         * @param {int} b the given bits
         * @return {boolean} true, if all given bits are set
         */
        this.contain = function(b) {
            return (that.bits & b) == b;
        };


        /**
         * Calculates, whether all given bits are equal to the saved bits
         * @param {int} b the given bits
         * @return {boolean}
         */
        this.equals = function(b) {
            return b == that.bits;
        };

    };

    var genPadding = function(str, length) {
        var pre = "";
        while (pre.length < length) {
            pre += str;
        }
        return pre;
    };

    this.padLeft = function(base, add, length) {
        return (genPadding(add, length) + base).slice(-length)
    }

    this.padRight = function(base, add, length) {
        return (base + genPadding(add, length)).slice(length)
    }
};
