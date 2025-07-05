package io.noties.markwon.html.jsoup.nodes

import io.noties.markwon.html.jsoup.helper.Validate.notEmpty
import io.noties.markwon.html.jsoup.helper.Validate.notNull

/**
 * A single key + value attribute. (Only used for presentation.)
 */
class Attribute(key: String, `val`: String?, parent: Attributes?) :
    MutableMap.MutableEntry<String?, String?>, Cloneable {
    override var key: String?

    /**
     * Get the attribute value.
     *
     * @return the attribute value
     */
    override var value: String?
        private set

    @JvmField
    var parent: Attributes? // used to update the holding Attributes when the key / value is changed via this interface

    /**
     * Create a new attribute from unencoded (raw) key and value.
     *
     * @param key   attribute key; case is preserved.
     * @param value attribute value
     */
    constructor(key: String, value: String?) : this(key, value, null)

    /**
     * Create a new attribute from unencoded (raw) key and value.
     *
     * @param key    attribute key; case is preserved.
     * @param val    attribute value
     * @param parent the containing Attributes (this Attribute is not automatically added to said Attributes)
     */
    init {
        notNull(key)
        this.key = key.trim { it <= ' ' }
        notEmpty(key) // trimming could potentially make empty, so validate here
        this.value = `val`
        this.parent = parent
    }

    /**
     * Set the attribute key; case is preserved.
     *
     * @param key the new key; must not be null
     */
    fun setKey(key: String) {
        var key = key
        notNull(key)
        key = key.trim { it <= ' ' }
        notEmpty(key) // trimming could potentially make empty, so validate here
        if (parent != null) {
            val i = parent!!.indexOfKey(this.key!!)
            if (i != Attributes.NotFound) parent!!.keys[i] = key
        }
        this.key = key
    }

    /**
     * Set the attribute value.
     *
     * @param newValue the new attribute value; must not be null
     */
    override fun setValue(newValue: String?): String? {
        val oldVal = parent!!.get(key!!)
        if (parent != null) {
            val i = parent!!.indexOfKey(this.key!!)
            if (i != Attributes.NotFound) parent!!.vals[i] = newValue
        }
        this.value = newValue
        return oldVal
    }

    override fun equals(other: Any?): Boolean { // note parent not considered
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val attribute = other as Attribute
        if (if (key != null) (key != attribute.key) else attribute.key != null) return false
        return if (this.value != null) (this.value == attribute.value) else attribute.value == null
    }

    override fun hashCode(): Int { // note parent not considered
        var result = if (key != null) key.hashCode() else 0
        result = 31 * result + (if (this.value != null) value.hashCode() else 0)
        return result
    }

    public override fun clone(): Attribute {
        try {
            return super.clone() as Attribute
        } catch (e: CloneNotSupportedException) {
            throw RuntimeException(e)
        }
    }
}
