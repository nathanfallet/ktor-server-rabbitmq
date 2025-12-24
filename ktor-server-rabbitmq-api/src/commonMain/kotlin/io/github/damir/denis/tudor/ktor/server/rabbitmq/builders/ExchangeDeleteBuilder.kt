package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.ExchangeDeleteOk

@RabbitDslMarker
class ExchangeDeleteBuilder(private val channel: Channel) {
    var exchange: String by Delegator(on = this)
    var ifUnused: Boolean by Delegator(on = this)

    init {
        ifUnused = false
    }

    suspend fun build(): ExchangeDeleteOk = when {
        verify(on = this@ExchangeDeleteBuilder, ::exchange, ::ifUnused) -> {
            channel.exchangeDelete(exchange, ifUnused)
        }

        else -> {
            error(logStateTrace(on = this@ExchangeDeleteBuilder))
        }
    }
}
