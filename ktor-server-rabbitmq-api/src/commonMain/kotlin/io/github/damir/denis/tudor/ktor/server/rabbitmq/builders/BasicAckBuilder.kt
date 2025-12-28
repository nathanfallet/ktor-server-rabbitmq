package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class BasicAckBuilder(private val channel: Channel) {
    private val deliveryTagDelegate = Delegator<Long>()
    var deliveryTag: Long by deliveryTagDelegate

    private val multipleDelegate = Delegator<Boolean>()
    var multiple: Boolean by multipleDelegate

    init { multiple = false }

    suspend fun build() = when {
        Delegator.verify(
            deliveryTagDelegate,
            multipleDelegate
        ) -> {
            channel.basicAck(deliveryTag, multiple)
        }

        else -> {
            error(
                Delegator.logStateTrace(deliveryTagDelegate, multipleDelegate)
            )
        }
    }
}