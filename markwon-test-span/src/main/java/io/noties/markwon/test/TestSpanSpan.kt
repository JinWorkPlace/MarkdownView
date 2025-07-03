package io.noties.markwon.test

internal class TestSpanSpan(
    private val name: String,
    private val children: MutableList<TestSpan>,
    private val arguments: MutableMap<String?, Any?>
) : TestSpan.Span() {
    override fun name(): String {
        return name
    }

    override fun arguments(): MutableMap<String?, Any?> {
        return arguments
    }

    override fun children(): MutableList<TestSpan> {
        return children
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as TestSpanSpan

        if (name != that.name) return false
        return arguments == that.arguments
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + arguments.hashCode()
        return result
    }
}
