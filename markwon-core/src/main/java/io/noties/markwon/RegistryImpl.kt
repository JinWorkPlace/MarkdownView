package io.noties.markwon

import io.noties.markwon.core.CorePlugin

// @since 4.0.0
internal class RegistryImpl(
    private val origin: MutableList<MarkwonPlugin>
) : MarkwonPlugin.Registry {
    private val plugins: MutableList<MarkwonPlugin> = ArrayList(origin.size)
    private val pending: MutableSet<MarkwonPlugin?> = HashSet(3)

    override fun <P : MarkwonPlugin> require(plugin: Class<P>): P {
        return get(plugin)
    }

    override fun <P : MarkwonPlugin> require(
        plugin: Class<P>, action: MarkwonPlugin.Action<in P>
    ) {
        action.apply(get(plugin))
    }

    fun process(): MutableList<MarkwonPlugin> {
        for (plugin in origin) {
            configure(plugin)
        }
        return plugins
    }

    private fun configure(plugin: MarkwonPlugin) {
        // important -> check if it's in plugins
        //  if it is -> no need to configure (already configured)

        if (!plugins.contains(plugin)) {
            check(!pending.contains(plugin)) { "Cyclic dependency chain found: $pending" }

            // start tracking plugins that are pending for configuration
            pending.add(plugin)

            plugin.configure(this)

            // stop pending tracking
            pending.remove(plugin)

            // check again if it's included (a child might've configured it already)
            // add to out-collection if not already present
            // this is a bit different from `find` method as it does check for exact instance
            // and not a sub-type
            if (!plugins.contains(plugin)) {
                // core-plugin must always be the first one (if it's present)
                if (CorePlugin::class.java.isAssignableFrom(plugin.javaClass)) {
                    plugins.add(0, plugin)
                } else {
                    plugins.add(plugin)
                }
            }
        }
    }

    private fun <P : MarkwonPlugin> get(type: Class<P>): P {
        // check if present already in plugins
        // find in origin, if not found -> throw, else add to out-plugins

        var plugin: P? = find(plugins, type)

        if (plugin == null) {
            plugin = find(origin, type)

            checkNotNull(plugin) {
                "Requested plugin is not added: " + "" + type.name + ", plugins: " + origin
            }

            configure(plugin)
        }

        return plugin
    }

    companion object {
        private fun <P : MarkwonPlugin> find(
            plugins: MutableList<MarkwonPlugin>, type: Class<P>
        ): P? {
            for (plugin in plugins) {
                if (type.isAssignableFrom(plugin.javaClass)) {
                    return plugin as P
                }
            }
            return null
        }
    }
}
