package io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator

import kotlin.concurrent.Volatile
import kotlin.reflect.KProperty


/**
 * A delegate class to manage the state of a single property,
 * ensuring it is initialized before use.
 *
 * @param T the type of the property being delegated.
 */
internal class Delegator<T : Any> {

    companion object {
        fun verify(vararg delegates: Delegator<*>): Boolean = delegates.all { it.state is State.Initialized }

        fun logStateTrace(vararg delegates: Delegator<*>): String {
            val uninitialized = delegates.filter { it.state !is State.Initialized }
            if (uninitialized.isNotEmpty()) {
                return "Uninitialized properties: $uninitialized"
            }
            return "All properties are initialized"
        }
    }

    @Volatile
    private var state: State<T> = State.Uninitialized

    /**
     * Gets the value of the delegated property, throwing an exception if the property is not initialized.
     */
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return when (val current = state) {
            is State.Initialized -> current.value
            else -> throw UninitializedPropertyException(
                "Property <${property.name}> must be initialized before accessing."
            )
        }
    }

    /**
     * Sets the value of the delegated property and updates its state to `Initialized`.
     */
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        state = State.Initialized(value)
    }
}
