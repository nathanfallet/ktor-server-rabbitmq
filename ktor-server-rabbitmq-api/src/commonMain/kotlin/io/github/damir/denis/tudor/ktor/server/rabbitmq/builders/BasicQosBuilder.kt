package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel

@RabbitDslMarker
class BasicQosBuilder(private val channel: Channel) {
    var prefetchSize: Int by Delegator(on = this)
    var prefetchCount: Int by Delegator(on = this)
    var global: Boolean by Delegator(on = this)

    suspend fun build() = when {
        verify(on = this@BasicQosBuilder, ::prefetchSize, ::prefetchCount, ::global) -> {
            channel.basicQos(prefetchSize, prefetchCount, global)
        }

        verify(on = this@BasicQosBuilder, ::prefetchCount, ::global) -> {
            channel.basicQos(
                prefetchCount = prefetchCount,
                global = global
            )
        }

        verify(on = this@BasicQosBuilder, ::prefetchCount) -> {
            channel.basicQos(
                prefetchCount = prefetchCount
            )
        }

        else -> {
            error(   logStateTrace(on = this@BasicQosBuilder))
        }
    }
}
