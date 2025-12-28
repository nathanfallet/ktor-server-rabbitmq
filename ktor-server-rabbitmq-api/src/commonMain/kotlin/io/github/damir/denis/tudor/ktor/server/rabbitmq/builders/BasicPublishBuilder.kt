package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.Properties
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

@RabbitDslMarker
class BasicPublishBuilder(
    private val channel: Channel,
) {
    private val exchangeDelegate = Delegator<String>()
    var exchange: String by exchangeDelegate

    private val routingKeyDelegate = Delegator<String>()
    var routingKey: String by routingKeyDelegate

    private val messageDelegate = Delegator<ByteArray>()
    var message: ByteArray by messageDelegate

    private val mandatoryDelegate = Delegator<Boolean>()
    var mandatory: Boolean by mandatoryDelegate

    private val immediateDelegate = Delegator<Boolean>()
    var immediate: Boolean by immediateDelegate

    private val propertiesDelegate = Delegator<Properties>()
    var properties: Properties by propertiesDelegate

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
        Delegator.verify(
            exchangeDelegate,
            routingKeyDelegate,
            messageDelegate,
            mandatoryDelegate,
            immediateDelegate,
            propertiesDelegate
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

        Delegator.verify(
            exchangeDelegate,
            routingKeyDelegate,
            messageDelegate,
            immediateDelegate,
            propertiesDelegate
        ) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                immediate = immediate,
                properties = properties,
                message = message
            )
        }

        Delegator.verify(
            exchangeDelegate,
            routingKeyDelegate,
            messageDelegate,
            mandatoryDelegate,
            propertiesDelegate
        ) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                mandatory,
                properties = properties,
                message = message
            )
        }

        Delegator.verify(
            exchangeDelegate,
            routingKeyDelegate,
            messageDelegate,
            propertiesDelegate
        ) -> {
            channel.basicPublish(
                exchange,
                routingKey,
                properties = properties,
                message = message
            )
        }

        else -> {
            error(
                Delegator.logStateTrace(
                    exchangeDelegate,
                    routingKeyDelegate,
                    messageDelegate,
                    propertiesDelegate
                )
            )
        }
    }
}

