package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel

@RabbitDslMarker
class BasicQosBuilder(private val channel: Channel) {
    private val prefetchSizeDelegate = Delegator<Int>()
    var prefetchSize: Int by prefetchSizeDelegate

    private val prefetchCountDelegate = Delegator<Int>()
    var prefetchCount: Int by prefetchCountDelegate

    private val globalDelegate = Delegator<Boolean>()
    var global: Boolean by globalDelegate

    suspend fun build() = when {
        Delegator.verify(
            prefetchSizeDelegate,
            prefetchCountDelegate,
            globalDelegate
        ) -> {
            channel.basicQos(prefetchSize, prefetchCount, global)
        }

        Delegator.verify(
            prefetchCountDelegate,
            globalDelegate
        ) -> {
            channel.basicQos(
                prefetchCount = prefetchCount,
                global = global
            )
        }

        Delegator.verify(
            prefetchCountDelegate
        ) -> {
            channel.basicQos(
                prefetchCount = prefetchCount
            )
        }

        else -> {
            error(
                Delegator.logStateTrace(prefetchCountDelegate, globalDelegate)
            )
        }
    }
}
