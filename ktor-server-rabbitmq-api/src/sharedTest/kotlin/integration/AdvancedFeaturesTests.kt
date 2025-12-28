package integration

import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AdvancedFeaturesTests {

    @Serializable
    data class TestMessage(val id: String, val content: String)

    companion object {
        private val rabbitMQContainer: RabbitMQContainer = RabbitMQContainer(
            DockerImageName.parse("rabbitmq:management")
        )

        @BeforeAll
        @JvmStatic
        fun setUp() {
            rabbitMQContainer.start()
            println("RabbitMQ is running at ${rabbitMQContainer.amqpUrl}")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            rabbitMQContainer.stop()
            println("RabbitMQ is stopped")
        }
    }

    @Test
    fun `basic properties with headers and metadata`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "props-queue"
                    exchange = "props-exchange"
                    routingKey = "props"
                    queueDeclare {
                        queue = "props-queue"
                    }
                    exchangeDeclare {
                        exchange = "props-exchange"
                        type = "direct"
                    }
                }
            }

            // Publish with custom properties
            rabbitmqTest {
                basicPublish {
                    exchange = "props-exchange"
                    routingKey = "props"
                    properties = basicProperties {
                        correlationId = "test-correlation-123"
                        type = "important"
                        headers = mapOf(
                            "source" to "integration-test",
                            "priority" to "high",
                            "version" to "1.0"
                        )
                        deliveryMode = 2 // Persistent
                        contentType = "application/json"
                    }
                    message { TestMessage("msg-1", "Test content") }
                }
            }

            // Consume and verify properties
            val receivedMessage = AtomicInteger(0)
            rabbitmqTest {
                basicConsume {
                    queue = "props-queue"
                    autoAck = true
                    deliverCallback<TestMessage> { message ->
                        receivedMessage.incrementAndGet()
                        
                        // Verify properties
                        assertEquals("test-correlation-123", message.properties.correlationId)
                        assertEquals("important", message.properties.type)
                        assertEquals("application/json", message.properties.contentType)
                        assertEquals(2, message.properties.deliveryMode)
                        
                        // Verify headers
                        val headers = message.properties.headers
                        assertNotNull(headers)
                        assertEquals("integration-test", headers["source"])
                        assertEquals("high", headers["priority"])
                        assertEquals("1.0", headers["version"])
                        
                        // Verify message content
                        assertEquals("msg-1", message.body.id)
                        assertEquals("Test content", message.body.content)
                    }
                }
            }

            sleep(1000)
            assertEquals(1, receivedMessage.get())
        }
    }

    @Test
    fun `quality of service (QoS) configuration`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "qos-queue"
                    exchange = "qos-exchange"
                    queueDeclare {
                        queue = "qos-queue"
                    }
                    exchangeDeclare {
                        exchange = "qos-exchange"
                        type = "fanout"
                    }
                }
            }

            // Publish multiple messages
            rabbitmqTest {
                repeat(10) {
                    basicPublish {
                        exchange = "qos-exchange"
                        message { "QoS Message $it" }
                    }
                }
            }

            sleep(500)

            // Configure QoS and consume
            val processedMessages = AtomicInteger(0)
            rabbitmqTest {
                connectionTest(id = "qos-consumer") {
                    // Set QoS to prefetch only 3 messages
                    basicQos {
                        prefetchCount = 3
                        global = false
                    }

                    basicConsume {
                        queue = "qos-queue"
                        autoAck = false
                        dispatcher = Dispatchers.IO
                        deliverCallback<String> { message ->
                            processedMessages.incrementAndGet()
                            
                            // Simulate processing time
                            Thread.sleep(100)
                            
                            basicAck {
                                deliveryTag = message.envelope.deliveryTag
                            }
                        }
                    }
                }
            }

            sleep(2000)
            assertEquals(10, processedMessages.get())
        }
    }

    @Test
    fun `message rejection and requeue`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "reject-queue"
                    exchange = "reject-exchange"
                    queueDeclare {
                        queue = "reject-queue"
                    }
                    exchangeDeclare {
                        exchange = "reject-exchange"
                        type = "fanout"
                    }
                }
            }

            // Publish test message
            rabbitmqTest {
                basicPublish {
                    exchange = "reject-exchange"
                    message { "Reject test message" }
                }
            }

            sleep(500)

            val attemptCount = AtomicInteger(0)
            
            // Consumer that rejects first attempt, accepts second
            rabbitmqTest {
                basicConsume {
                    queue = "reject-queue"
                    autoAck = false
                    deliverCallback<String> { message ->
                        val attempts = attemptCount.incrementAndGet()
                        
                        if (attempts == 1) {
                            // Reject and requeue first attempt
                            basicReject {
                                deliveryTag = message.envelope.deliveryTag
                                requeue = true
                            }
                        } else {
                            // Accept second attempt
                            basicAck {
                                deliveryTag = message.envelope.deliveryTag
                            }
                        }
                    }
                }
            }

            sleep(2000)
            assertEquals(2, attemptCount.get())
            
            // Verify queue is empty after processing
            rabbitmqTest {
                assertEquals(0, messageCount { queue = "reject-queue" })
            }
        }
    }

    @Test
    fun `negative acknowledgment with multiple messages`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "nack-queue"
                    exchange = "nack-exchange"
                    queueDeclare {
                        queue = "nack-queue"
                    }
                    exchangeDeclare {
                        exchange = "nack-exchange"
                        type = "fanout"
                    }
                }
            }

            // Publish multiple messages
            rabbitmqTest {
                repeat(5) {
                    basicPublish {
                        exchange = "nack-exchange"
                        message { "NACK Message $it" }
                    }
                }
            }

            sleep(500)

            val processedMessages = AtomicInteger(0)
            
            // Consumer that nacks every other message
            rabbitmqTest {
                basicConsume {
                    queue = "nack-queue"
                    autoAck = false
                    deliverCallback<String> { message ->
                        val count = processedMessages.incrementAndGet()
                        
                        if (count % 2 == 0) {
                            // NACK even messages without requeue
                            basicNack {
                                deliveryTag = message.envelope.deliveryTag
                                multiple = false
                                requeue = false
                            }
                        } else {
                            // ACK odd messages
                            basicAck {
                                deliveryTag = message.envelope.deliveryTag
                            }
                        }
                    }
                }
            }

            sleep(2000)
            assertEquals(5, processedMessages.get())
        }
    }

    @Test
    fun `consumer and queue statistics`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "stats-queue"
                    exchange = "stats-exchange"
                    queueDeclare {
                        queue = "stats-queue"
                    }
                    exchangeDeclare {
                        exchange = "stats-exchange"
                        type = "fanout"
                    }
                }
            }

            // Initially no consumers
            rabbitmqTest {
                assertEquals(0, consumerCount { queue = "stats-queue" })
            }

            // Publish messages
            rabbitmqTest {
                repeat(3) {
                    basicPublish {
                        exchange = "stats-exchange"
                        message { "Stats message $it" }
                    }
                }
            }

            sleep(500)

            // Check message count
            rabbitmqTest {
                assertEquals(3, messageCount { queue = "stats-queue" })
            }

            // Start consumer
            val consumedMessages = AtomicInteger(0)
            rabbitmqTest {
                basicConsume {
                    queue = "stats-queue"
                    autoAck = true
                    deliverCallback<String> { message ->
                        consumedMessages.incrementAndGet()
                    }
                }
            }

            sleep(1000)

            // Verify consumption
            assertEquals(3, consumedMessages.get())
            
            rabbitmqTest {
                assertEquals(0, messageCount { queue = "stats-queue" })
            }
        }
    }
}