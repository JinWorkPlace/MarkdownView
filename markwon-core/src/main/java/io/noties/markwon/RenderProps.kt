package io.noties.markwon

/**
 * @since 3.0.0
 */
interface RenderProps {
    fun <T> get(prop: Prop<T>): T

    fun <T> get(prop: Prop<T>, defValue: T): T

    fun <T> set(prop: Prop<T>, value: T?)

    fun <T> clear(prop: Prop<T>)

    fun clearAll()
}
