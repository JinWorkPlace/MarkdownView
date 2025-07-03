package io.noties.markwon.test

internal class TestSpanDocument(
    private val children: MutableList<TestSpan>
) : TestSpan.Document() {
    override fun children(): MutableList<TestSpan> {
        return children
    }

    override fun wholeText(): String {
        val builder = StringBuilder()

        for (child in children) {
            fillWholeText(builder, child)
        }

        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as TestSpanDocument

        return children == that.children
    }

    override fun hashCode(): Int {
        return children.hashCode()
    }

    companion object {
        private fun fillWholeText(builder: StringBuilder, span: TestSpan) {
            when (span) {
                is Text -> {
                    builder.append(span.literal())
                }

                is Span -> {
                    for (child in span.children()) {
                        fillWholeText(builder, child)
                    }
                }

                else -> {
                    throw IllegalStateException(
                        "Unexpected state. Found unexpected TestSpan " + "object of type `" + span.javaClass.name + "`"
                    )
                }
            }
        }
    }
}
