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
    fun notNull(obj: Any?) {
        if (obj == null) {
            throw IllegalArgumentException("Object must not be null")
        }
    }

    /**
     * Validates that the object is not null
     * @param obj object to test
     * @param msg message to output if validation fails
     */
    @JvmStatic
    fun notNull(obj: Any?, msg: String) {
        if (obj == null) {
            throw IllegalArgumentException(msg)
        }
    }

    /**
     * Validates that the value is true
     * @param value object to test
     */
    @JvmStatic
    fun isTrue(value: Boolean) {
        if (!value) {
            throw IllegalArgumentException("Must be true")
        }
    }

    /**
     * Validates that the value is true
     * @param value object to test
     * @param msg message to output if validation fails
     */
    @JvmStatic
    fun isTrue(value: Boolean, msg: String) {
        if (!value) {
            throw IllegalArgumentException(msg)
        }
    }

    /**
     * Validates that the value is false
     * @param value object to test
     */
    @JvmStatic
    fun isFalse(value: Boolean) {
        if (value) {
            throw IllegalArgumentException("Must be false")
        }
    }

    /**
     * Validates that the value is false
     * @param value object to test
     * @param msg message to output if validation fails
     */
    @JvmStatic
    fun isFalse(value: Boolean, msg: String) {
        if (value) {
            throw IllegalArgumentException(msg)
        }
    }

    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     */
    @JvmStatic
    fun noNullElements(objects: Array<Any?>) {
        noNullElements(objects, "Array must not contain any null objects")
    }

    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     * @param msg message to output if validation fails
     */
    @JvmStatic
    fun noNullElements(objects: Array<Any?>, msg: String) {
        for (obj in objects) {
            if (obj == null) {
                throw IllegalArgumentException(msg)
            }
        }
    }

    /**
     * Validates that the string is not empty
     * @param string the string to test
     */
    @JvmStatic
    fun notEmpty(string: String?) {
        if (string == null || string.isEmpty()) {
            throw IllegalArgumentException("String must not be empty")
        }
    }

    /**
     * Validates that the string is not empty
     * @param string the string to test
     * @param msg message to output if validation fails
     */
    @JvmStatic
    fun notEmpty(string: String?, msg: String) {
        if (string == null || string.isEmpty()) {
            throw IllegalArgumentException(msg)
        }
    }

    /**
     * Cause a failure.
     * @param msg message to output.
     */
    @JvmStatic
    fun fail(msg: String) {
        throw IllegalArgumentException(msg)
    }
}