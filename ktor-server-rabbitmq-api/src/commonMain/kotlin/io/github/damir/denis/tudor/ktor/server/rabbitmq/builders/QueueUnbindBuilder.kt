package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.QueueUnbindOk

@RabbitDslMarker
class QueueUnbindBuilder(private val channel: Channel) {
    private val queueDelegate = Delegator<String>()
    var queue: String by queueDelegate

    private val exchangeDelegate = Delegator<String>()
    var exchange: String by exchangeDelegate

    private val routingKeyDelegate = Delegator<String>()
    var routingKey: String by routingKeyDelegate

    private val argumentsDelegate = Delegator<Map<String, Any>>()
    var arguments: Map<String, Any> by argumentsDelegate

    suspend fun build(): QueueUnbindOk = when {
        Delegator.verify(
            queueDelegate,
            exchangeDelegate,
            routingKeyDelegate,
            argumentsDelegate
        ) -> {
            channel.queueUnbind(queue, exchange, routingKey, arguments)
        }

        Delegator.verify(
            queueDelegate,
            exchangeDelegate,
            routingKeyDelegate
        ) -> {
            channel.queueUnbind(queue, exchange, routingKey)
        }

        else -> {
            error(
                Delegator.logStateTrace(
                    queueDelegate,
                    exchangeDelegate,
                    routingKeyDelegate
                )
            )
        }
    }
}
