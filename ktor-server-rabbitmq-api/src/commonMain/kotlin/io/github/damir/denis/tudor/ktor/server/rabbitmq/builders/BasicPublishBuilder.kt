package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Properties
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

@RabbitDslMarker
class BasicPublishBuilder(
    private val channel: Channel,
) {
    var exchange: String by Delegator(on = this)
    var routingKey: String by Delegator(on = this)
    var message: ByteArray by Delegator(on = this)
    var mandatory: Boolean by Delegator(on = this)
    var immediate: Boolean by Delegator(on = this)
    var properties: Properties by Delegator(on = this)

    init {
        routingKey = ""
        properties = Properties()
    }

    @RabbitDslMarker
    inline fun <reified T> message(block: () -> T) {
        message = Json.encodeToString(block()).toByteArray(Charsets.UTF_8)
    }

    @RabbitDslMarker
    inline fun <reified T> message(block: T) {
        message = Json.encodeToString(block).toByteArray(Charsets.UTF_8)
    }

    suspend fun build() = when {
        verify(
            on = this@BasicPublishBuilder,
            ::exchange,
            ::routingKey,
            ::message,
            ::mandatory,
            ::immediate,
            ::properties
        ) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                mandatory,
                immediate,
                properties,
                message
            )
        }

        verify(on = this@BasicPublishBuilder, ::exchange, ::routingKey, ::message, ::immediate, ::properties) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                immediate = immediate,
                properties = properties,
                message = message
            )
        }

        verify(on = this@BasicPublishBuilder, ::exchange, ::routingKey, ::message, ::mandatory, ::properties) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                mandatory,
                properties = properties,
                message = message
            )
        }

        verify(on = this@BasicPublishBuilder, ::exchange, ::routingKey, ::message, ::properties) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                properties = properties,
                message = message
            )
        }

        else -> {
            error(logStateTrace(on = this@BasicPublishBuilder))
        }
    }
}

