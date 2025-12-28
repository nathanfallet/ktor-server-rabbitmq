package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class MessageCountBuilder(private val channel: Channel) {
    private val queueDelegate = Delegator<String>()
    var queue: String by queueDelegate

    suspend fun build(): Long = when {
        Delegator.verify(
            queueDelegate
        ) -> {
            channel.messageCount(queue)
        }

        else -> {
            error(
                Delegator.logStateTrace(queueDelegate)
            )
        }
    }
}
