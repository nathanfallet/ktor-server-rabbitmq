package io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces

fun interface CancelCallback {

    fun handle(consumerTag: String)

}
