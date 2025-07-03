package io.noties.markwon.html

class CssProperty internal constructor() {
    private var key: String? = null
    private var value: String? = null

    fun set(key: String, value: String) {
        this.key = key
        this.value = value
    }

    fun key(): String {
        return key!!
    }

    fun value(): String {
        return value!!
    }

    fun mutate(): CssProperty {
        val cssProperty = CssProperty()
        cssProperty.set(this.key!!, this.value!!)
        return cssProperty
    }

    override fun toString(): String {
        return "CssProperty{key='$key', value='$value'}"
    }
}
