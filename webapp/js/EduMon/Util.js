/**
 * Utility Class - A mix of useful functions for reuse (contains third party code)
 *
 * @author Marco (jquery plugins, BitSet, object utils)
 * @author Phillip (keymap, window, string padding
 */
EduMon = (typeof window !== "undefined" && window.EduMon) ? window.EduMon : {};
EduMon.Util = new function() {

	var that = this;

	/**
	 * the keys used in various keyboard controls
	 * @type {{SHIFT: number, SPACE: number, A: number, B: number, D: number, G: number, M: number, P: number, Q:
	 *     number, R: number, Z: number}}
	 */
	this.keymap = {
		SHIFT: 16,
		SPACE: 32,
		A: 65,
		B: 66,
		D: 68,
		G: 71,
		M: 77,
		P: 80,
		Q: 81,
		R: 82,
		Z: 90
	};

	/**
	 * Iterates over a field and calls the given functor
	 * @param {object} object the object ot iterate over
	 * @param {Function} functor the functor to call (first argument is the field's name, the second one the content)
	 */
	this.forEachField = function(object, functor) {
		for (var propertyName in object) {
			if (object.hasOwnProperty(propertyName)) {
				functor(propertyName, object[propertyName]);
			}
		}
	};

	/**
	 * Counts the number of a given objects own properties
	 * @author Niko
	 * @param {object} object with attributes to count
	 */
	this.countFields = function(object) {
		var count = 0;
		this.forEachField(object, function() {
			count++;
		});
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

	/**
	 * Generates string to be used for padding.
	 *
	 * @param {string} str
	 * @param {int} length
	 * @returns {string} a string consisting of str that is at least length characters long
	 */
	var genPadding = function(str, length) {
		var pre = "";
		while (pre.length < length) {
			pre += str;
		}
		return pre;
	};

	/**
	 * Pads a string on the left side with a given string to the given length
	 *
	 * @param {string} base the string to pad
	 * @param {string} add the string to use as padding
	 * @param {int} length
	 * @returns {string} the padding string
	 */
	this.padLeft = function(base, add, length) {
		return (genPadding(add, length) + base).slice(-length)
	};

	/**
	 * Pads a string on the right side with a given string to the given length
	 *
	 * @param {string} base the string to pad
	 * @param {string} add the string to use as padding
	 * @param {int} length
	 * @returns {string} the padding string
	 */
	this.padRight = function(base, add, length) {
		return (base + genPadding(add, length)).slice(length)
	};

	/**
	 * This is a wrapper around window.open which eases the specification of the feature spec
	 *
	 * @param {string} url the url for the window
	 * @param {Object} options the window feature specification as an object
	 * @param {string} [name=blank] the name of the window ("blank", "parent", ...)
	 * @returns {Window} the newly opened window
	 */
	this.openWindow = function(url, options, name) {
		name = name || "blank";
		var splitter = '';
		var features = '';
		that.forEachField(options, function(k, v) {
			if (v === true) {
				v = 'yes';
			}
			if (v === false) {
				v = 'no';
			}
			features += splitter + k + '=' + v;
			splitter = ',';
		});

		return window.open(url, '_' + name, features);
	};

	/**
	 * Get current unix timestamp (according to browser)
	 *
	 * @author Niko
	 * @returns {int} timestamp in seconds
	 */
	this.timestampNow = function() {
		return Math.floor(Date.now() / 1000);
	};

	/**
	 * JS Implementation of MurmurHash3 (r136) (as of May 20, 2011)
	 *
	 * @author <a href="mailto:gary.court@gmail.com">Gary Court</a>
	 * @see http://github.com/garycourt/murmurhash-js
	 * @author <a href="mailto:aappleby@gmail.com">Austin Appleby</a>
	 * @see http://sites.google.com/site/murmurhash/
	 *
	 * @param {string} key ASCII only
	 * @param {number} seed Positive integer only
	 * @return {number} 32-bit positive integer hash
	 */
	this.hashString = function(key, seed) {
		var remainder, bytes, h1, h1b, c1, c1b, c2, c2b, k1, i;

		remainder = key.length & 3; // key.length % 4
		bytes = key.length - remainder;
		h1 = seed;
		c1 = 0xcc9e2d51;
		c2 = 0x1b873593;
		i = 0;

		while (i < bytes) {
			k1 =
				((key.charCodeAt(i) & 0xff)) |
				((key.charCodeAt(++i) & 0xff) << 8) |
				((key.charCodeAt(++i) & 0xff) << 16) |
				((key.charCodeAt(++i) & 0xff) << 24);
			++i;

			k1 = ((((k1 & 0xffff) * c1) + ((((k1 >>> 16) * c1) & 0xffff) << 16))) & 0xffffffff;
			k1 = (k1 << 15) | (k1 >>> 17);
			k1 = ((((k1 & 0xffff) * c2) + ((((k1 >>> 16) * c2) & 0xffff) << 16))) & 0xffffffff;

			h1 ^= k1;
			h1 = (h1 << 13) | (h1 >>> 19);
			h1b = ((((h1 & 0xffff) * 5) + ((((h1 >>> 16) * 5) & 0xffff) << 16))) & 0xffffffff;
			h1 = (((h1b & 0xffff) + 0x6b64) + ((((h1b >>> 16) + 0xe654) & 0xffff) << 16));
		}

		k1 = 0;

		switch (remainder) {
			case 3:
				k1 ^= (key.charCodeAt(i + 2) & 0xff) << 16;
			case 2:
				k1 ^= (key.charCodeAt(i + 1) & 0xff) << 8;
			case 1:
				k1 ^= (key.charCodeAt(i) & 0xff);

				k1 = (((k1 & 0xffff) * c1) + ((((k1 >>> 16) * c1) & 0xffff) << 16)) & 0xffffffff;
				k1 = (k1 << 15) | (k1 >>> 17);
				k1 = (((k1 & 0xffff) * c2) + ((((k1 >>> 16) * c2) & 0xffff) << 16)) & 0xffffffff;
				h1 ^= k1;
		}

		h1 ^= key.length;

		h1 ^= h1 >>> 16;
		h1 = (((h1 & 0xffff) * 0x85ebca6b) + ((((h1 >>> 16) * 0x85ebca6b) & 0xffff) << 16)) & 0xffffffff;
		h1 ^= h1 >>> 13;
		h1 = ((((h1 & 0xffff) * 0xc2b2ae35) + ((((h1 >>> 16) * 0xc2b2ae35) & 0xffff) << 16))) & 0xffffffff;
		h1 ^= h1 >>> 16;

		return h1 >>> 0;
	};

	this.randomIntFromInterval = function(min, max) {
		return Math.floor(Math.random() * (max - min + 1) + min);
	}
};

/**
 * Checks, whether a String end with a given suffix
 * @author Niko
 * @param {string} suffix the given suffix
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


// If jQuery is available, add our plugins
if (typeof $ !== "undefined") {

	/**
	 * Sorts option tags by their values
	 */
	$.fn.sortSelectBox = function() {
		// copy elements into array
		var elements = [];
		this.children().each(function() {
			var el = $(this);
			elements.push({el: el, sort: el.html()});
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

	/**
	 * Clones the jQuery object and sets all select values explicitly before (To make sure, they're selected later)
	 * @param {boolean} dataAndEvents like jQuery parameter
	 * @param {boolean} deep like jQuery parameter
	 * @return {*} the cloned jQuery object
	 */
	$.fn.cloneWithSelectState = function(dataAndEvents, deep) {
		var initialClone = this.clone(dataAndEvents, deep);
		var selects = $('select', this);
		var selectsInClone = $('select', initialClone);
		selects.each(function(i) {
			selectsInClone.eq(i).val($(this).val());
		});
		return initialClone;
	}

}
