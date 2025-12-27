package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.connection.ConnectionManager
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.logStateTrace
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.*
import io.github.damir.denis.tudor.ktor.server.rabbitmq.rabbitMQ
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
@RabbitDslMarker
class BasicConsumeBuilder(
    val connectionManager: ConnectionManager,
    private val channel: Channel,
) {
    val defaultLogger = KtorSimpleLogger(name = this::class.qualifiedName!!)

    var noLocal: Boolean by Delegator(on = this)
    var exclusive: Boolean by Delegator(on = this)
    var arguments: Map<String, Any> by Delegator(on = this)

    var autoAck: Boolean by Delegator(on = this)
    var queue: String by Delegator(on = this)
    var consumerTag: String by Delegator(on = this)

    private var deliverCallback: DeliverCallback by Delegator(on = this)
    private var cancelCallback: CancelCallback by Delegator(on = this)
    private var shutdownSignalCallback: ConsumerShutdownSignalCallback by Delegator(on = this)

    var dispatcher: CoroutineDispatcher = Dispatchers.rabbitMQ
    var coroutinePollSize: Int = 1

    @InternalAPI
    var receiverChannel = kotlinx.coroutines.channels.Channel<Pair<String, Delivery>>(
        connectionManager.configuration.consumerChannelCoroutineSize
    )

    @InternalAPI
    var failureCallbackDefined = false

    @InternalAPI
    var receiverFailChannel = kotlinx.coroutines.channels.Channel<Pair<String, Delivery>>(
        connectionManager.configuration.consumerChannelCoroutineSize
    )

    init {
        noLocal = false
        exclusive = false
        arguments = emptyMap()
        deliverCallback = DeliverCallback { consumerTag, delivery ->
            receiverChannel.trySendBlocking(consumerTag to delivery)
        }
        cancelCallback = CancelCallback { }
        shutdownSignalCallback = ConsumerShutdownSignalCallback { _, error -> }
    }

    @RabbitDslMarker
    inline fun <reified T> deliverCallback(crossinline callback: suspend (message: Message<T>) -> Unit) {
        repeat(coroutinePollSize) {
            connectionManager.coroutineScope.launch(dispatcher) {
                receiverChannel.consumeAsFlow().collect { (consumerTag, delivery) ->
                    runCatching {
                        when (T::class) {
                            String::class -> delivery.body.decodeToString() as T
                            ByteArray::class -> delivery.body as T

                            else -> Json.decodeFromString<T>(delivery.body.decodeToString())
                        }
                    }.onFailure { error ->
                        defaultLogger.error(error)
                        if (failureCallbackDefined) {
                            receiverFailChannel.trySendBlocking(consumerTag to delivery)
                        }
                    }.getOrNull()?.let { message ->
                        callback(
                            Message(
                                body = message,
                                consumerTag = consumerTag,
                                envelope = delivery.envelope,
                                properties = delivery.properties
                            )
                        )
                    }
                }
            }
        }
    }

    @RabbitDslMarker
    @Deprecated(
        message = "Use deliverFailureCallback with Message<ByteArray> parameter for full access to properties and envelope.",
        level = DeprecationLevel.WARNING
    )
    fun deliverFailureCallback(callback: suspend (tag: Long, message: ByteArray) -> Unit) {
        failureCallbackDefined = true
        connectionManager.coroutineScope.launch(dispatcher) {
            receiverFailChannel.consumeAsFlow().collect { (_, delivery) ->
                callback(delivery.envelope.deliveryTag, delivery.body)
            }
        }
    }

    @RabbitDslMarker
    fun deliverFailureCallback(callback: suspend (message: Message<ByteArray>) -> Unit) {
        failureCallbackDefined = true
        connectionManager.coroutineScope.launch(dispatcher) {
            receiverFailChannel.consumeAsFlow().collect { (consumerTag, delivery) ->
                callback(
                    Message(
                        body = delivery.body,
                        consumerTag = consumerTag,
                        envelope = delivery.envelope,
                        properties = delivery.properties
                    )
                )
            }
        }
    }

    @RabbitDslMarker
    fun cancelCallback(callback: (tag: String) -> Unit) {
        cancelCallback = CancelCallback { consumerTag ->
            callback(consumerTag)
        }
    }

    @RabbitDslMarker
    fun shutdownSignalCallback(callback: (tag: String, sig: ShutdownSignalException) -> Unit) {
        shutdownSignalCallback = ConsumerShutdownSignalCallback { consumerTag, sig ->
            callback(consumerTag, sig)
        }
    }

    suspend fun build(): String = when {
        verify(
            on = this@BasicConsumeBuilder,
            ::queue,
            ::autoAck,
            ::consumerTag,
            ::noLocal,
            ::exclusive,
            ::arguments,
            ::deliverCallback,
            ::cancelCallback,
            ::shutdownSignalCallback
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                consumerTag,
                noLocal,
                exclusive,
                arguments,
                deliverCallback,
                cancelCallback,
                shutdownSignalCallback
            )
        }

        verify(
            on = this@BasicConsumeBuilder,
            ::queue,
            ::autoAck,
            ::arguments,
            ::deliverCallback,
            ::cancelCallback,
            ::shutdownSignalCallback
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                arguments = arguments,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        verify(
            on = this@BasicConsumeBuilder,
            ::queue,
            ::autoAck,
            ::consumerTag,
            ::deliverCallback,
            ::cancelCallback
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                consumerTag,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }


        verify(
            on = this@BasicConsumeBuilder,
            ::queue,
            ::autoAck,
            ::consumerTag,
            ::deliverCallback,
            ::shutdownSignalCallback
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                consumerTag,
                deliverCallback = deliverCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        verify(
            on = this@BasicConsumeBuilder,
            ::queue,
            ::autoAck,
            ::arguments,
            ::deliverCallback,
            ::shutdownSignalCallback
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                arguments = arguments,
                deliverCallback = deliverCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        verify(on = this@BasicConsumeBuilder, ::queue, ::autoAck, ::arguments, ::deliverCallback, ::cancelCallback) -> {
            channel.basicConsume(
                queue,
                autoAck,
                arguments = arguments,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }

        verify(on = this@BasicConsumeBuilder, ::queue, ::autoAck, ::deliverCallback, ::shutdownSignalCallback) -> {
            channel.basicConsume(
                queue,
                autoAck,
                deliverCallback = deliverCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        verify(on = this@BasicConsumeBuilder, ::queue, ::autoAck, ::deliverCallback, ::cancelCallback) -> {
            channel.basicConsume(
                queue,
                autoAck,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }

        verify(on = this@BasicConsumeBuilder, ::queue, ::autoAck, ::deliverCallback, ::cancelCallback) -> {
            channel.basicConsume(
                queue,
                autoAck,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }

        else -> {
            error(logStateTrace(on = this@BasicConsumeBuilder))
        }
    }
}
