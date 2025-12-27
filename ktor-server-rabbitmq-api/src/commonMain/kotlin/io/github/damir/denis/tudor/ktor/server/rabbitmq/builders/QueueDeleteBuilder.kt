package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.QueueDeleteOk

@RabbitDslMarker
class QueueDeleteBuilder(private val channel: Channel) {
    var queue: String by Delegator(on = this)
    var ifUnused: Boolean by Delegator(on = this)
    var ifEmpty: Boolean by Delegator(on = this)

    suspend fun build(): QueueDeleteOk = when {
        verify(on = this@QueueDeleteBuilder, ::queue, ::ifUnused, ::ifEmpty) -> {
            channel.queueDelete(queue, ifUnused, ifEmpty)
        }

        verify(on = this@QueueDeleteBuilder, ::queue) -> {
            channel.queueDelete(queue)
        }

        else -> {
            error(logStateTrace(on = this@QueueDeleteBuilder))
        }
    }
}
