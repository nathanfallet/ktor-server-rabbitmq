package io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto

data class QueueDeclareOk(
    val queue: String,
    val messageCount: Int,
    val consumerCount: Int,
)
