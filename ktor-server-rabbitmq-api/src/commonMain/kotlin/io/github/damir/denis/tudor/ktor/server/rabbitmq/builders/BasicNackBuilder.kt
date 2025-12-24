package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class BasicNackBuilder(private val channel: Channel) {
    var deliveryTag: Long by Delegator(on = this)
    var multiple: Boolean by Delegator(on = this)
    var requeue: Boolean by Delegator(on = this)

    init {
        multiple = false
        requeue = false
    }

    suspend fun build() = when {
        verify(on = this@BasicNackBuilder, ::deliveryTag, ::multiple, ::requeue) -> {
            channel.basicNack(deliveryTag, multiple, requeue)
        }

        else -> {
            error(logStateTrace(on = this@BasicNackBuilder))
        }
    }
}
