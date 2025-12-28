package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class BasicRejectBuilder(private val channel: Channel) {
    private val deliveryTagDelegate = Delegator<Long>()
    var deliveryTag: Long by deliveryTagDelegate

    private val requeueDelegate = Delegator<Boolean>()
    var requeue: Boolean by requeueDelegate

    init {
        requeue = false
    }

    suspend fun build() = when {
        Delegator.verify(
            deliveryTagDelegate,
            requeueDelegate
        ) -> {
            channel.basicReject(deliveryTag, requeue)
        }

        else -> {
            error(
                Delegator.logStateTrace(deliveryTagDelegate, requeueDelegate)
            )
        }
    }
}
