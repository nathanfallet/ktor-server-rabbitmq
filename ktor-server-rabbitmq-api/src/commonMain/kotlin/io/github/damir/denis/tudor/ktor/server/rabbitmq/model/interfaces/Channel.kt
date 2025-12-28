package io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces

import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.ExchangeDeclareOk
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.ExchangeDeleteOk
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.GetResponse
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.Properties
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.QueueBindOk
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.QueueDeclareOk
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.QueueDeleteOk
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.QueueUnbindOk

interface Channel {

    val isOpen: Boolean
    val closeReason: String?

    suspend fun basicAck(deliveryTag: Long, multiple: Boolean)

    suspend fun basicNack(deliveryTag: Long, multiple: Boolean, requeue: Boolean)

    suspend fun basicReject(deliveryTag: Long, requeue: Boolean)

    suspend fun basicGet(queue: String, autoAck: Boolean): GetResponse

    suspend fun basicPublish(
        exchange: String,
        routingKey: String,
        mandatory: Boolean = false,
        immediate: Boolean = false,
        properties: Properties = Properties(),
        message: ByteArray,
    )

    suspend fun basicConsume(
        queue: String,
        autoAck: Boolean,
        consumerTag: String = "",
        noLocal: Boolean = false,
        exclusive: Boolean = false,
        arguments: Map<String, Any> = emptyMap(),
        deliverCallback: DeliverCallback,
        cancelCallback: CancelCallback = CancelCallback { },
        shutdownSignalCallback: ConsumerShutdownSignalCallback = ConsumerShutdownSignalCallback { _, _ -> },
    ): String

    suspend fun basicQos(prefetchSize: Int = 0, prefetchCount: Int, global: Boolean = false)

    suspend fun exchangeDeclare(
        exchange: String,
        type: String,
        durable: Boolean,
        autoDelete: Boolean,
        internal: Boolean,
        arguments: Map<String, Any>,
    ): ExchangeDeclareOk

    suspend fun exchangeDelete(exchange: String, ifUnused: Boolean): ExchangeDeleteOk

    suspend fun messageCount(queue: String): Long

    suspend fun consumerCount(queue: String): Long

    suspend fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Map<String, Any> = emptyMap(),
    ): QueueBindOk

    suspend fun queueUnbind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Map<String, Any> = emptyMap(),
    ): QueueUnbindOk

    suspend fun queueDeclare(
        queue: String,
        durable: Boolean,
        exclusive: Boolean,
        autoDelete: Boolean,
        arguments: Map<String, Any> = emptyMap(),
    ): QueueDeclareOk

    suspend fun queueDelete(queue: String, ifUnused: Boolean = false, ifEmpty: Boolean = false): QueueDeleteOk

    suspend fun close()

}
