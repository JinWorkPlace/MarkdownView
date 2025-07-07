package io.noties.markwon

/**
 * Class to hold data in [RenderProps]. Represents a certain *property*.
 *
 * @param <T> represents the type that this instance holds
 * @see .of
 * @see .of
 * @since 3.0.0
</T> */
class Prop<T> internal constructor(private val name: String) {
    fun name(): String {
        return name
    }

    fun get(props: RenderProps): T {
        return props.get(this)
    }

    fun get(props: RenderProps, defValue: T): T {
        return props.get(this, defValue!!)
    }

    fun require(props: RenderProps): T {
        val t = get(props)
        if (t == null) {
            throw NullPointerException(name)
        }
        return t
    }

    fun set(props: RenderProps, value: T?) {
        props.set(this, value)
    }

    fun clear(props: RenderProps) {
        props.clear(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this == other) return true
        if (other == null || javaClass != other.javaClass) return false

        val prop = other as Prop<*>

        return name == prop.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "Prop{name='$name'}"
    }

    companion object {
        @Suppress("unused")
        fun <T> of(type: Class<T?>, name: String): Prop<T?> {
            return Prop(name)
        }

        @JvmStatic
        fun <T> of(name: String): Prop<T> {
            return Prop(name)
        }
    }
}
