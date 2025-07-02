package io.noties.markwon

/**
 * @since 3.0.0
 */
internal class RenderPropsImpl : RenderProps {
    private val values: MutableMap<Prop<*>, Any> = HashMap(3)

    override fun <T> get(prop: Prop<T>): T {
        return values[prop] as T
    }

    override fun <T> get(prop: Prop<T>, defValue: T): T {
        val value = values[prop]
        if (value != null) {
            return value as T
        }
        return defValue
    }

    override fun <T> set(prop: Prop<T>, value: T?) {
        if (value == null) {
            values.remove(prop)
        } else {
            values.put(prop, value)
        }
    }

    override fun <T> clear(prop: Prop<T>) {
        values.remove(prop)
    }

    override fun clearAll() {
        values.clear()
    }
}
