package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class BasicAckBuilder(private val channel: Channel) {
    var deliveryTag: Long by Delegator(on = this)
    var multiple: Boolean by Delegator(on = this)

    init {
        multiple = false
    }

    suspend fun build() = when {
        verify(on = this@BasicAckBuilder, ::deliveryTag, ::multiple) -> {
            channel.basicAck(deliveryTag, multiple)
        }

        else -> {
            error(logStateTrace(on = this@BasicAckBuilder, ::deliveryTag, ::multiple))
        }
    }
}