package io.github.damir.denis.tudor.ktor.server.rabbitmq

import io.github.damir.denis.tudor.ktor.server.rabbitmq.connection.ConnectionConfig
import io.github.damir.denis.tudor.ktor.server.rabbitmq.connection.ConnectionManager
import io.ktor.server.application.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

val ConnectionManagersKey = AttributeKey<MutableMap<String, ConnectionManager>>(
    name = "RABBITMQ_CONNECTION_MANAGERS"
)

fun createRabbitMQPlugin(
    instanceName: String = "default",
    createConnectionManager: (application: Application, pluginConfig: ConnectionConfig) -> ConnectionManager,
) = createApplicationPlugin(
    name = "RabbitMQ-$instanceName",
    configurationPath = "ktor.rabbitmq",
    createConfiguration = ::ConnectionConfig
) {
    pluginConfig.verify()

    val managers: MutableMap<String, ConnectionManager> =
        application.attributes.getOrNull(ConnectionManagersKey) ?: run {
            val newMap = mutableMapOf<String, ConnectionManager>()
            application.attributes.put(ConnectionManagersKey, newMap)
            newMap
        }

    val manager: ConnectionManager = createConnectionManager(application, pluginConfig)
    RabbitMQDispatcherHolder.dispatcher = manager.dispatcher

    managers[instanceName] = manager

    application.monitor.subscribe(ApplicationStopping) {
        runBlocking { manager.close() }
    }
}

private object RabbitMQDispatcherHolder {
    lateinit var dispatcher: CoroutineDispatcher
}

val Dispatchers.rabbitMQ: CoroutineDispatcher
    get() = RabbitMQDispatcherHolder.dispatcher
