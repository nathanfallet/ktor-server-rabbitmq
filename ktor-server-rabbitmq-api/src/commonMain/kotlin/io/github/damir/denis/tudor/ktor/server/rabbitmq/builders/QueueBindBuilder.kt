package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.QueueBindOk

@RabbitDslMarker
class QueueBindBuilder(private val channel: Channel) {
    var queue: String by Delegator(on = this)
    var exchange: String by Delegator(on = this)
    var routingKey: String by Delegator(on = this)
    var arguments: Map<String, Any> by Delegator(on = this)

    init {
        routingKey = ""
    }

    suspend fun build(): QueueBindOk = when {
        verify(on = this@QueueBindBuilder, ::queue, ::exchange, ::routingKey, ::arguments) -> {
            channel.queueBind(queue, exchange, routingKey, arguments)
        }

        verify(on = this@QueueBindBuilder, ::queue, ::exchange, ::routingKey) -> {
            channel.queueBind(queue, exchange, routingKey)
        }

        else -> {
            error(logStateTrace(on = this@QueueBindBuilder))
        }
    }
}
