package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class ConsumerCountBuilder(private val channel: Channel) {
    var queue: String by Delegator(on = this)

    suspend fun build(): Long = when {
        verify(on = this@ConsumerCountBuilder, ::queue) -> {
            channel.consumerCount(queue)
        }

        else -> {
            error(logStateTrace(on = this@ConsumerCountBuilder))
        }
    }
}
