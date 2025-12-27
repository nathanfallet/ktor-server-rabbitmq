package io.github.damir.denis.tudor.ktor.server.rabbitmq

import io.github.damir.denis.tudor.ktor.server.rabbitmq.connection.JavaConnectionManager

val RabbitMQ = createRabbitMQPlugin { application, pluginConfig ->
    JavaConnectionManager(application, pluginConfig)
}

fun RabbitMQ(instanceName: String) = createRabbitMQPlugin(instanceName = instanceName) { application, pluginConfig ->
    JavaConnectionManager(application, pluginConfig, instanceName)
}