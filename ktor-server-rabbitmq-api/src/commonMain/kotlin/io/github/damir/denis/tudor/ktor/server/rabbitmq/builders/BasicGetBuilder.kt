package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.GetResponse

@RabbitDslMarker
class BasicGetBuilder(private val channel: Channel) {
    var queue: String by Delegator(on = this)
    var autoAck: Boolean by Delegator(on = this)

    suspend fun build(): GetResponse = when {
        verify(on = this@BasicGetBuilder, ::queue, ::autoAck) -> {
            channel.basicGet(queue, autoAck)
        }

        else -> {
            error(logStateTrace(on = this@BasicGetBuilder, ::queue, ::autoAck))
        }
    }
}
