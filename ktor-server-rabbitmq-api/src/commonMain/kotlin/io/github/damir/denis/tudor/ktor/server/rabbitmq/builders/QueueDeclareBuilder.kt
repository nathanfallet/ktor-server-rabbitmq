package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.QueueDeclareOk

@RabbitDslMarker
class QueueDeclareBuilder(private val channel: Channel) {
    var queue: String by Delegator(on = this)
    var durable: Boolean by Delegator(on = this)
    var exclusive: Boolean by Delegator(on = this)
    var autoDelete: Boolean by Delegator(on = this)
    var arguments: Map<String, Any> by Delegator(on = this)

    init {
        durable = true
        exclusive = false
        autoDelete = false
        arguments = emptyMap()
    }

    suspend fun build(): QueueDeclareOk = when {
        verify(on = this@QueueDeclareBuilder, ::queue, ::durable, ::exclusive, ::autoDelete, ::arguments) -> {
            channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments)
        }

        else -> {
            error(logStateTrace(on = this@QueueDeclareBuilder))
        }
    }
}
