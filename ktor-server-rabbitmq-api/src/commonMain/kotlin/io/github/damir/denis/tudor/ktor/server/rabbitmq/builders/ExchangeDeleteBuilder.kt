package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.ExchangeDeleteOk

@RabbitDslMarker
class ExchangeDeleteBuilder(private val channel: Channel) {
    private val exchangeDelegate = Delegator<String>()
    var exchange: String by exchangeDelegate

    private val ifUnusedDelegate = Delegator<Boolean>()
    var ifUnused: Boolean by ifUnusedDelegate

    init {
        ifUnused = false
    }

    suspend fun build(): ExchangeDeleteOk = when {
        Delegator.verify(
            exchangeDelegate,
            ifUnusedDelegate
        ) -> {
            channel.exchangeDelete(exchange, ifUnused)
        }

        else -> {
            error(
                Delegator.logStateTrace(exchangeDelegate, ifUnusedDelegate)
            )
        }
    }
}
