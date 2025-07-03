package io.noties.markwon.test

internal class TestSpanText(private val literal: String) : TestSpan.Text() {
    override fun literal(): String {
        return literal
    }

    override fun length(): Int {
        return literal.length
    }

    override fun children(): MutableList<TestSpan> {
        return mutableListOf()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as TestSpanText

        return literal == that.literal
    }

    override fun hashCode(): Int {
        return literal.hashCode()
    }
}
