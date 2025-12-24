package io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator

import io.ktor.util.logging.*
import kotlin.reflect.KProperty

/**
 * StateRegistry is a utility object to manage and trace the initialization
 * states of properties in an object. It tracks states per object using a
 * global registry map, so Delegator.setValue can be non-suspending.
 *
 * @version 1.4.0
 */
object StateRegistry {

    /**
     * Global registry map to store ScopedStateRegistry instances per object.
     */
    private val registries = mutableMapOf<Any, ScopedStateRegistry>()

    /**
     * ScopedStateRegistry holds a reference to an object and its logger,
     * along with a map of property states for that object.
     */
    private class ScopedStateRegistry(
        val ref: Any,
        val logger: Logger
    ) {
        val states = mutableMapOf<Pair<String, String>, State<Any>>()
    }

    /**
     * Retrieves or creates the registry for a given object.
     *
     * @param on the object to get or create a registry for.
     * @return the ScopedStateRegistry for the object.
     */
    private fun getRegistry(on: Any): ScopedStateRegistry {
        return registries.getOrPut(on) {
            ScopedStateRegistry(
                ref = on,
                logger = KtorSimpleLogger(on::class.qualifiedName!!)
            )
        }
    }

    /**
     * Adds or updates the state of a property for a given object and property name.
     *
     * @param on the object instance containing the property.
     * @param propertyOf the object whose class name is used as the property owner.
     * @param property the property to track.
     * @param state the current state of the property.
     */
    fun addState(
        on: Any,
        propertyOf: Any,
        property: KProperty<*>,
        state: State<Any>
    ) {
        val registry = getRegistry(on)
        val owner = propertyOf::class.qualifiedName!!
        registry.states[owner to property.name] = state
    }

    /**
     * Verifies that all specified properties have been initialized.
     *
     * @param on the object instance to check properties for.
     * @param properties the properties to check.
     * @return true if all properties are initialized, false otherwise.
     */
    fun verify(on: Any, vararg properties: KProperty<*>): Boolean {
        val registry = getRegistry(on)
        val owner = registry.ref::class.qualifiedName!!

        return properties.all { property ->
            registry.states.getOrPut(owner to property.name) { State.Uninitialized } !is State.Uninitialized
        }
    }

    /**
     * Logs the state trace for specified properties.
     * Shows which properties are initialized and their values.
     *
     * @param on the object instance to log properties for.
     * @param properties the properties to log; if empty, logs a message indicating no properties to trace.
     */
    fun logStateTrace(on: Any, vararg properties: KProperty<*>): String {
        val registry = getRegistry(on)
        val owner = registry.ref::class.qualifiedName!!

        registry.logger.trace("<${registry.ref::class.simpleName}> State trace")

        for (property in properties) {
            val state = registry.states[owner to property.name]
            val value = (state as? State.Initialized)?.value ?: "Uninitialized"

            registry.logger.error(
                "<${registry.ref::class.simpleName}> <${property.name}> value=<$value>"
            )
        }

        if (properties.isEmpty()) {
            registry.logger.error("<${registry.ref::class.simpleName}> No properties to trace")
        }

        return "Unexpected combination of parameters"
    }
}
