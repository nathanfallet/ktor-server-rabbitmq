package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.GetResponse

@RabbitDslMarker
class BasicGetBuilder(private val channel: Channel) {
    private val queueDelegate = Delegator<String>()
    var queue: String by queueDelegate

    private val autoAckDelegate = Delegator<Boolean>()
    var autoAck: Boolean by autoAckDelegate

    suspend fun build(): GetResponse = when {
        Delegator.verify(
            queueDelegate,
            autoAckDelegate
        ) -> {
            channel.basicGet(queue, autoAck)
        }

        else -> {
            error(
                Delegator.logStateTrace(queueDelegate, autoAckDelegate)
            )
        }
    }
}
