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
    }
};