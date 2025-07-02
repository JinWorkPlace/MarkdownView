package io.noties.markwon.html.jsoup.parser

/**
 * A Parse Error records an error in the input HTML that occurs in either the tokenisation or the tree building phase.
 */
class ParseError {
    /**
     * Retrieves the offset of the error.
     * @return error offset within input
     */
    val position: Int

    /**
     * Retrieve the error message.
     * @return the error message.
     */
    val errorMessage: String?

    internal constructor(pos: Int, errorMsg: String?) {
        this.position = pos
        this.errorMessage = errorMsg
    }

    internal constructor(pos: Int, errorFormat: String, vararg args: Any?) {
        this.errorMessage = String.format(errorFormat, *args)
        this.position = pos
    }

    override fun toString(): String {
        return position.toString() + ": " + this.errorMessage
    }
}

