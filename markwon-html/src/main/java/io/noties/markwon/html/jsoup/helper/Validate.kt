package io.noties.markwon.html.jsoup.helper

/**
 * Simple validation methods. Designed for jsoup internal use
 */
object Validate {
    /**
     * Validates that the object is not null
     * @param obj object to test
     */
    @JvmStatic
    fun notNull(obj: Any) {
        requireNotNull(obj) { "Object must not be null" }
    }

    /**
     * Validates that the object is not null
     * @param obj object to test
     * @param msg message to output if validation fails
     */
    fun notNull(obj: Any, msg: String?) {
        requireNotNull(obj) { msg!! }
    }

    /**
     * Validates that the value is true
     * @param val object to test
     */
    @JvmStatic
    fun isTrue(`val`: Boolean) {
        require(`val`) { "Must be true" }
    }

    /**
     * Validates that the value is true
     * @param val object to test
     * @param msg message to output if validation fails
     */
    fun isTrue(`val`: Boolean, msg: String?) {
        require(`val`) { msg!! }
    }

    /**
     * Validates that the value is false
     * @param val object to test
     */
    @JvmStatic
    fun isFalse(`val`: Boolean) {
        require(!`val`) { "Must be false" }
    }

    /**
     * Validates that the value is false
     * @param val object to test
     * @param msg message to output if validation fails
     */
    fun isFalse(`val`: Boolean, msg: String?) {
        require(!`val`) { msg!! }
    }

    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     * @param msg message to output if validation fails
     */
    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     */
    @JvmOverloads
    fun noNullElements(
        objects: Array<Any>,
        msg: String? = "Array must not contain any null objects"
    ) {
        for (obj in objects) requireNotNull(obj) { msg!! }
    }

    /**
     * Validates that the string is not empty
     * @param string the string to test
     */
    @JvmStatic
    fun notEmpty(string: String) {
        require(!(string.isEmpty())) { "String must not be empty" }
    }

    /**
     * Validates that the string is not empty
     * @param string the string to test
     * @param msg message to output if validation fails
     */
    fun notEmpty(string: String, msg: String?) {
        require(!(string.isEmpty())) { msg!! }
    }

    /**
     * Cause a failure.
     * @param msg message to output.
     */
    fun fail(msg: String?) {
        throw IllegalArgumentException(msg)
    }
}
