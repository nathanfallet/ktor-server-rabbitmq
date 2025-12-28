package io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto

data class Envelope(
    val deliveryTag: Long,
    val isRedeliver: Boolean,
    val exchange: String,
    val routingKey: String,
)
