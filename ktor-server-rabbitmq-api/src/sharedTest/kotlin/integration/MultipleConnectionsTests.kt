package integration

import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class MultipleConnectionsTests {

    @Serializable
    data class Order(val id: String, val amount: Double)

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
    fun `multiple connections with producer and consumer separation`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
                defaultConnectionName = "default"
            }
        }

        application {
            // Setup queues and exchanges
            rabbitmqTest {
                queueBind {
                    queue = "orders-queue"
                    exchange = "orders-exchange"
                    routingKey = "order.created"
                    queueDeclare {
                        queue = "orders-queue"
                        durable = true
                    }
                    exchangeDeclare {
                        exchange = "orders-exchange"
                        type = "direct"
                    }
                }
            }

            val processedOrders = AtomicInteger(0)

            // Consumer connection with high throughput
            rabbitmqTest {
                connectionTest(id = "consumer") {
                    basicConsume {
                        queue = "orders-queue"
                        autoAck = false
                        dispatcher = Dispatchers.IO
                        coroutinePollSize = 10
                        deliverCallback<Order> { message ->
                            delay(10) // Simulate processing time
                            processedOrders.incrementAndGet()
                            
                            basicAck {
                                deliveryTag = message.envelope.deliveryTag
                            }
                        }
                    }
                }
            }

            // Producer connection
            rabbitmqTest {
                connectionTest(id = "producer") {
                    repeat(50) {
                        basicPublish {
                            exchange = "orders-exchange"
                            routingKey = "order.created"
                            message { Order("order-$it", 100.0 + it) }
                        }
                    }
                }
            }

            sleep(2000)
            assertEquals(50, processedOrders.get())
        }
    }

    @Test
    fun `multiple connections with channel management`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
                defaultConnectionName = "default"
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "test-queue"
                    exchange = "test-exchange"
                    routingKey = "test"
                    queueDeclare {
                        queue = "test-queue"
                    }
                    exchangeDeclare {
                        exchange = "test-exchange"
                        type = "direct"
                    }
                }
            }

            val messageCount = AtomicInteger(0)

            // Connection with multiple channels
            rabbitmqTest {
                connectionTest(id = "multi-channel") {
                    // Channel 1 for consuming
                    channelTest(id = 1) {
                        basicConsume {
                            queue = "test-queue"
                            autoAck = true
                            deliverCallback<String> { message ->
                                messageCount.incrementAndGet()
                            }
                        }
                    }

                    // Channel 2 for publishing
                    channelTest(id = 2) {
                        repeat(20) {
                            basicPublish {
                                exchange = "test-exchange"
                                routingKey = "test"
                                message { "Message $it" }
                            }
                        }
                    }
                }
            }

            sleep(1000)
            assertEquals(20, messageCount.get())
        }
    }

    @Test
    fun `connection auto-close functionality`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            rabbitmqTest {
                queueBind {
                    queue = "auto-close-queue"
                    exchange = "auto-close-exchange"
                    queueDeclare {
                        queue = "auto-close-queue"
                    }
                    exchangeDeclare {
                        exchange = "auto-close-exchange"
                        type = "fanout"
                    }
                }
            }

            // Test auto-close connection
            rabbitmqTest {
                connectionTest(id = "auto-close-test", autoClose = true) {
                    basicPublish {
                        exchange = "auto-close-exchange"
                        message { "Test message" }
                    }
                }
            }

            // Verify message was published
            rabbitmqTest {
                assertEquals(1, messageCount { queue = "auto-close-queue" })
            }
        }
    }
}