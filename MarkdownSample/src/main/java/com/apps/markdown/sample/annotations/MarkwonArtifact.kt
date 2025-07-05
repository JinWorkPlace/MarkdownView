package com.apps.markdown.sample.annotations

enum class MarkwonArtifact {
    CORE,
    EDITOR,
    EXT_LATEX,
    EXT_STRIKETHROUGH,
    EXT_TABLES,
    EXT_TASKLIST,
    HTML,
    IMAGE,
    IMAGE_COIL,
    IMAGE_GLIDE,
    IMAGE_PICASSO,
    INLINE_PARSER,
    LINKIFY,
    RECYCLER,
    RECYCLER_TABLE,
    SIMPLE_EXT,
    SYNTAX_HIGHLIGHT;

    fun artifactName(): String {
        return name.lowercase().replace('_', '-')
    }
}
