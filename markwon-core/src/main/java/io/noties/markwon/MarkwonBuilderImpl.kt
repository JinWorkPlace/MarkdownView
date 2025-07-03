package io.noties.markwon

import android.content.Context
import android.widget.TextView.BufferType
import io.noties.markwon.Markwon.TextSetter
import io.noties.markwon.core.MarkwonTheme
import org.commonmark.parser.Parser
import java.util.Collections

/**
 * @since 3.0.0
 */
internal class MarkwonBuilderImpl(private val context: Context) : Markwon.Builder {
    private val plugins: MutableList<MarkwonPlugin> = ArrayList(3)

    private var bufferType = BufferType.SPANNABLE

    private var textSetter: TextSetter? = null

    // @since 4.4.0
    private var fallbackToRawInputWhenEmpty = true

    override fun bufferType(bufferType: BufferType): Markwon.Builder {
        this.bufferType = bufferType
        return this
    }

    override fun textSetter(textSetter: TextSetter): Markwon.Builder {
        this.textSetter = textSetter
        return this
    }

    override fun usePlugin(plugin: MarkwonPlugin): Markwon.Builder {
        plugins.add(plugin)
        return this
    }

    override fun usePlugins(plugins: Iterable<MarkwonPlugin>): Markwon.Builder {
        val iterator: Iterator<MarkwonPlugin> = plugins.iterator()

        var plugin: MarkwonPlugin

        while (iterator.hasNext()) {
            plugin = iterator.next()
            this.plugins.add(plugin)
        }

        return this
    }

    override fun fallbackToRawInputWhenEmpty(fallbackToRawInputWhenEmpty: Boolean): Markwon.Builder {
        this.fallbackToRawInputWhenEmpty = fallbackToRawInputWhenEmpty
        return this
    }

    override fun build(): Markwon {
        check(!plugins.isEmpty()) { "No plugins were added to this builder. Use #usePlugin " + "method to add them" }

        // please note that this method must not modify supplied collection
        // if nothing should be done -> the same collection can be returned
        val plugins: MutableList<MarkwonPlugin> = preparePlugins(this.plugins)

        val parserBuilder = Parser.Builder()
        val themeBuilder = MarkwonTheme.builderWithDefaults(context)
        val configurationBuilder = MarkwonConfiguration.Builder()
        val visitorBuilder: MarkwonVisitor.Builder = MarkwonVisitorImpl.BuilderImpl()
        val spanFactoryBuilder: MarkwonSpansFactory.Builder = MarkwonSpansFactoryImpl.BuilderImpl()

        for (plugin in plugins) {
            plugin.configureParser(parserBuilder)
            plugin.configureTheme(themeBuilder)
            plugin.configureConfiguration(configurationBuilder)
            plugin.configureVisitor(visitorBuilder)
            plugin.configureSpansFactory(spanFactoryBuilder)
        }

        val configuration =
            configurationBuilder.build(themeBuilder.build(), spanFactoryBuilder.build())

        // @since 4.1.1
        // @since 4.1.2 - do not reuse render-props (each render call should have own render-props)
        val visitorFactory = MarkwonVisitorFactory.create(visitorBuilder, configuration)

        return MarkwonImpl(
            bufferType,
            textSetter,
            parserBuilder.build(),
            visitorFactory,
            configuration,
            Collections.unmodifiableList(plugins),
            fallbackToRawInputWhenEmpty
        )
    }

    companion object {
        private fun preparePlugins(plugins: MutableList<MarkwonPlugin>): MutableList<MarkwonPlugin> {
            return RegistryImpl(plugins).process()
        }
    }
}
