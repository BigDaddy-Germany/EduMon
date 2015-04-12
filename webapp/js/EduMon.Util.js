EduMon = window.EduMon || {};
EduMon.Util = new function() {

    var that = this;

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

    // todo comment me
    var genPadding = function(str, length) {
        var pre = "";
        while (pre.length < length) {
            pre += str;
        }
        return pre;
    };

    // todo comment me
    this.padLeft = function(base, add, length) {
        return (genPadding(add, length) + base).slice(-length)
    };

    // todo comment me
    this.padRight = function(base, add, length) {
        return (base + genPadding(add, length)).slice(length)
    };

    this.openWindow = function(url, options, name) {
        var splitter = '';
        var features = '';
        that.forEachField(options, function(k, v) {
            features += splitter + k + '=' + v;
            splitter = ',';
        });

        return window.open(url, '_' + name, features);
    };
};





/**
 * Checks, whether a String end with a given suffix
 * @param {String} suffix the given suffix
 * @return {boolean} True, if the String ends with the given suffix
 */
String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};


/**
 * Inserts the given element into the first free slot (undefined)
 * @param {Object} element the element to insert
 * @return {int} The element's new index
 */
Array.prototype.insertIntoFirstFreeSlot = function(element) {
    var newIndex;

    for (var i = 0; i < this.length + 1; ++i) {
        // Because we go to length + 1, there is at least this "free slot"
        if (this[i] === undefined) {
            this[i] = element;
            newIndex = i;
            break;
        }
    }

    return newIndex;
};

/**
 * Calculates the first used index
 * @return {number} the calculated index
 */
Array.prototype.firstUsedElement = function() {
    for (var i = 0; i < this.length; ++i) {
        if (this[i]) {
            return i;
        }
    }
};


/**
 * Sorts option tags by their values
 */
$.fn.sortSelectBox = function() {
    // copy elements into array
    var elements = [];
    this.children().each(function() {
        var el = $(this);
        elements.push({ el: el, sort: el.html() });
    });

    // sort them using the sort key
    elements.sort(function(a, b) {
        var sortA = a.sort.toLowerCase();
        var sortB = b.sort.toLowerCase();
        if (sortA > sortB) {
            return 1;
        }
        if (sortA < sortB) {
            return -1;
        }
        return 0;
    });

    // remove old html
    this.empty();

    // add all new elements
    var that = this;
    elements.forEach(function(el) {
        el.el.appendTo(that);
    });
};