package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.connection.ConnectionManager
import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator
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

    private val noLocalDelegate = Delegator<Boolean>()
    var noLocal: Boolean by noLocalDelegate

    private val exclusiveDelegate = Delegator<Boolean>()
    var exclusive: Boolean by exclusiveDelegate

    private val argumentsDelegate = Delegator<Map<String, Any>>()
    var arguments: Map<String, Any> by argumentsDelegate

    private val autoAckDelegate = Delegator<Boolean>()
    var autoAck: Boolean by autoAckDelegate

    private val queueDelegate = Delegator<String>()
    var queue: String by queueDelegate

    private val consumerTagDelegate = Delegator<String>()
    var consumerTag: String by consumerTagDelegate

    private val deliverCallbackDelegate = Delegator<DeliverCallback>()
    private var deliverCallback: DeliverCallback by deliverCallbackDelegate

    private val cancelCallbackDelegate = Delegator<CancelCallback>()
    private var cancelCallback: CancelCallback by cancelCallbackDelegate

    private val shutdownSignalCallbackDelegate = Delegator<ConsumerShutdownSignalCallback>()
    private var shutdownSignalCallback: ConsumerShutdownSignalCallback by shutdownSignalCallbackDelegate

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
        autoAck = false
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
                        val body = when (T::class) {
                            String::class -> delivery.body.decodeToString() as T
                            ByteArray::class -> delivery.body as T

                            else -> Json.decodeFromString<T>(delivery.body.decodeToString())
                        }

                        callback(
                            Message(
                                body = body,
                                consumerTag = consumerTag,
                                envelope = delivery.envelope,
                                properties = delivery.properties
                            )
                        )
                    }.onFailure { error ->
                        defaultLogger.error("Exception in deliverCallback", error)
                        if (failureCallbackDefined) {
                            receiverFailChannel.trySendBlocking(consumerTag to delivery)
                        }
                    }
                }
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
        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            consumerTagDelegate,
            noLocalDelegate,
            exclusiveDelegate,
            argumentsDelegate,
            deliverCallbackDelegate,
            cancelCallbackDelegate,
            shutdownSignalCallbackDelegate
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

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            argumentsDelegate,
            deliverCallbackDelegate,
            cancelCallbackDelegate,
            shutdownSignalCallbackDelegate
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

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            consumerTagDelegate,
            deliverCallbackDelegate,
            cancelCallbackDelegate
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                consumerTag,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            consumerTagDelegate,
            deliverCallbackDelegate,
            shutdownSignalCallbackDelegate
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                consumerTag,
                deliverCallback = deliverCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            argumentsDelegate,
            deliverCallbackDelegate,
            shutdownSignalCallbackDelegate
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                arguments = arguments,
                deliverCallback = deliverCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            argumentsDelegate,
            deliverCallbackDelegate,
            cancelCallbackDelegate
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                arguments = arguments,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            deliverCallbackDelegate,
            shutdownSignalCallbackDelegate
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                deliverCallback = deliverCallback,
                shutdownSignalCallback = shutdownSignalCallback
            )
        }

        Delegator.verify(
            queueDelegate,
            autoAckDelegate,
            deliverCallbackDelegate,
            cancelCallbackDelegate
        ) -> {
            channel.basicConsume(
                queue,
                autoAck,
                deliverCallback = deliverCallback,
                cancelCallback = cancelCallback
            )
        }

        else -> {
            error(
                Delegator.logStateTrace(
                    queueDelegate,
                    autoAckDelegate,
                    deliverCallbackDelegate
                )
            )
        }
    }
}