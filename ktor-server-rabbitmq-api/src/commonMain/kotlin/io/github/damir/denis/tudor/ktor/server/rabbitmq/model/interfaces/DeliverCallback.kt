package io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces

import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.Delivery

fun interface DeliverCallback {

    fun handle(consumerTag: String, message: Delivery)

}
