package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.ExchangeDeclareOk

@RabbitDslMarker
class ExchangeDeclareBuilder(private val channel: Channel) {
    var exchange: String by Delegator(on = this)
    var type: String by Delegator(on = this)
    var durable: Boolean by Delegator(on = this)
    var autoDelete: Boolean by Delegator(on = this)
    var internal: Boolean by Delegator(on = this)
    var arguments: Map<String, Any> by Delegator(on = this)

    init {
        durable = false
        autoDelete = false
        internal = false
        arguments = emptyMap()
    }

    suspend fun build(): ExchangeDeclareOk = when {
        verify(
            on = this@ExchangeDeclareBuilder,
            ::exchange,
            ::type,
            ::durable,
            ::autoDelete,
            ::internal,
            ::arguments
        ) -> {
            channel.exchangeDeclare(
                exchange,
                type,
                durable,
                autoDelete,
                internal,
                arguments
            )
        }

        else -> {
            error(logStateTrace(on = this@ExchangeDeclareBuilder))
        }
    }
}
