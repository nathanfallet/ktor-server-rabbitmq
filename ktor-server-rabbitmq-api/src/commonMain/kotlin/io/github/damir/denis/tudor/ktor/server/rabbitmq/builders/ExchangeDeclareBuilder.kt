package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.ExchangeDeclareOk

@RabbitDslMarker
class ExchangeDeclareBuilder(private val channel: Channel) {
    private val exchangeDelegate = Delegator<String>()
    var exchange: String by exchangeDelegate

    private val typeDelegate = Delegator<String>()
    var type: String by typeDelegate

    private val durableDelegate = Delegator<Boolean>()
    var durable: Boolean by durableDelegate

    private val autoDeleteDelegate = Delegator<Boolean>()
    var autoDelete: Boolean by autoDeleteDelegate

    private val internalDelegate = Delegator<Boolean>()
    var internal: Boolean by internalDelegate

    private val argumentsDelegate = Delegator<Map<String, Any>>()
    var arguments: Map<String, Any> by argumentsDelegate

    init {
        durable = false
        autoDelete = false
        internal = false
        arguments = emptyMap()
    }

    suspend fun build(): ExchangeDeclareOk = when {
        Delegator.verify(
            exchangeDelegate,
            typeDelegate,
            durableDelegate,
            autoDeleteDelegate,
            internalDelegate,
            argumentsDelegate
        ) -> {
            channel.exchangeDeclare(
                exchange,
                type,
                durable,
                autoDelete,
                internal,
                arguments
            )
        }

        else -> {
            error(
                Delegator.logStateTrace(exchangeDelegate, typeDelegate)
            )
        }
    }
}
