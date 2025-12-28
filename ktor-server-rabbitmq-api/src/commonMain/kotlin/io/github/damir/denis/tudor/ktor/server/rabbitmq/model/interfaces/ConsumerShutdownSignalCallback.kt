package io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces

import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.exceptions.ShutdownSignalException

fun interface ConsumerShutdownSignalCallback {

    fun handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException)

}
