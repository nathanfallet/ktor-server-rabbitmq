package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.QueueDeclareOk

@RabbitDslMarker
class QueueDeclareBuilder(private val channel: Channel) {
    private val queueDelegate = Delegator<String>()
    var queue: String by queueDelegate

    private val durableDelegate = Delegator<Boolean>()
    var durable: Boolean by durableDelegate

    private val exclusiveDelegate = Delegator<Boolean>()
    var exclusive: Boolean by exclusiveDelegate

    private val autoDeleteDelegate = Delegator<Boolean>()
    var autoDelete: Boolean by autoDeleteDelegate

    private val argumentsDelegate = Delegator<Map<String, Any>>()
    var arguments: Map<String, Any> by argumentsDelegate

    init {
        durable = true
        exclusive = false
        autoDelete = false
        arguments = emptyMap()
    }

    suspend fun build(): QueueDeclareOk = when {
        Delegator.verify(
            queueDelegate,
            durableDelegate,
            exclusiveDelegate,
            autoDeleteDelegate,
            argumentsDelegate
        ) -> {
            channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments)
        }

        else -> {
            error(
                Delegator.logStateTrace(queueDelegate)
            )
        }
    }
}
