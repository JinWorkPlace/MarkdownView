package com.apps.markdown.sample.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class MarkwonSampleInfo(
    /**
     * Actual format is not important, but this key must be set in order to persist sample.
     * This key should not change during lifetime of sample
     *
     *
     * `id` date in `YYYYMMDDHHmmss` format (UTC),
     * a simple live template can be used:
     * `groovyScript("new Date().format('YYYYMMDDHHmmss', TimeZone.getTimeZone('UTC'))")
    ` *
     */
    val id: String,
    val title: String,
    val description: String = "",
    val artifacts: Array<MarkwonArtifact>,
    val tags: Array<Tag>
)
