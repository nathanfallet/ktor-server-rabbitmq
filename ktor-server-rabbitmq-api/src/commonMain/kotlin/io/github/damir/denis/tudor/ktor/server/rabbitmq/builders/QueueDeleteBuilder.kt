package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.QueueDeleteOk

@RabbitDslMarker
class QueueDeleteBuilder(private val channel: Channel) {
    private val queueDelegate = Delegator<String>()
    var queue: String by queueDelegate

    private val ifUnusedDelegate = Delegator<Boolean>()
    var ifUnused: Boolean by ifUnusedDelegate

    private val ifEmptyDelegate = Delegator<Boolean>()
    var ifEmpty: Boolean by ifEmptyDelegate

    suspend fun build(): QueueDeleteOk = when {
        Delegator.verify(
            queueDelegate,
            ifUnusedDelegate,
            ifEmptyDelegate
        ) -> {
            channel.queueDelete(queue, ifUnused, ifEmpty)
        }

        Delegator.verify(
            queueDelegate
        ) -> {
            channel.queueDelete(queue)
        }

        else -> {
            error(
                Delegator.logStateTrace(queueDelegate)
            )
        }
    }
}
