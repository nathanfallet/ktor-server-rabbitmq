package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel

@RabbitDslMarker
class BasicNackBuilder(private val channel: Channel) {
    private val deliveryTagDelegate = Delegator<Long>()
    var deliveryTag: Long by deliveryTagDelegate

    private val multipleDelegate = Delegator<Boolean>()
    var multiple: Boolean by multipleDelegate

    private val requeueDelegate = Delegator<Boolean>()
    var requeue: Boolean by requeueDelegate

    init {
        multiple = false
        requeue = false
    }

    suspend fun build() = when {
        Delegator.verify(
            deliveryTagDelegate,
            multipleDelegate,
            requeueDelegate
        ) -> {
            channel.basicNack(deliveryTag, multiple, requeue)
        }

        else -> {
            error(
                Delegator.logStateTrace(
                    deliveryTagDelegate,
                    multipleDelegate,
                    requeueDelegate
                )
            )
        }
    }
}
