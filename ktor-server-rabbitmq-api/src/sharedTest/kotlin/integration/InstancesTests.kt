package integration

import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class InstancesTests {

    enum class Instances {
        INSTANCE1, INSTANCE2
    }

    @Serializable
    data class Message(
        var content: String,
    )

    companion object {
        private val rabbitMQContainer: RabbitMQContainer = RabbitMQContainer(
            DockerImageName.parse("rabbitmq:management")
        )

        private val rabbitMQContainer1: RabbitMQContainer = RabbitMQContainer(
            DockerImageName.parse("rabbitmq:management")
        )

        @BeforeAll
        @JvmStatic
        fun setUp() {
            rabbitMQContainer.start()
            println("RabbitMQ is running at ${rabbitMQContainer.amqpUrl}")

            rabbitMQContainer1.start()
            println("RabbitMQ is running at ${rabbitMQContainer1.amqpUrl}")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            rabbitMQContainer.stop()
            println("RabbitMQ is stopped")

            rabbitMQContainer1.stop()
            println("RabbitMQ is stopped")
        }
    }

    @Test
    fun `performing actions on two different instances test`() = testApplication {
        application {
            install(RabbitMQ(instanceName = Instances.INSTANCE1.name)) {
                connectionAttempts = 3
                attemptDelay = 10
                uri = rabbitMQContainer.amqpUrl
            }
            install(RabbitMQ(instanceName = Instances.INSTANCE2.name)) {
                connectionAttempts = 3
                attemptDelay = 10
                uri = rabbitMQContainer1.amqpUrl
            }
        }

        application {
            rabbitmqTest(instanceName = Instances.INSTANCE1.name) {
                queueBind {
                    queue = "test-queue1"
                    exchange = "test-exchange1"
                    queueDeclare {
                        queue = "test-queue1"
                    }
                    exchangeDeclare {
                        exchange = "test-exchange1"
                        type = "fanout"
                    }
                }
            }

            rabbitmqTest(instanceName = Instances.INSTANCE2.name) {
                queueBind {
                    queue = "test-queue1"
                    exchange = "test-exchange1"
                    queueDeclare {
                        queue = "test-queue1"
                    }
                    exchangeDeclare {
                        exchange = "test-exchange1"
                        type = "fanout"
                    }
                }
            }

            rabbitmqTest(instanceName = Instances.INSTANCE2.name) {
                basicConsume {
                    queue = "test-queue1"
                    autoAck = true
                    deliverCallback<String> { message ->
                        println("${Instances.INSTANCE2}: Received message: $message.")
                    }
                }
            }

            rabbitmqTest(instanceName = Instances.INSTANCE1.name) {
                basicConsume {
                    queue = "test-queue1"
                    autoAck = true
                    deliverCallback<String> { message ->
                        println("${Instances.INSTANCE1}: Received message: $message.")

                        rabbitmqTest(instanceName = Instances.INSTANCE2.name) {
                            basicPublish {
                                println("${Instances.INSTANCE2}: Published the message.")
                                exchange = "test-exchange1"
                                message { message.body }
                            }
                        }
                    }
                }
            }

            rabbitmqTest(instanceName = Instances.INSTANCE1.name) {
                basicPublish {
                    exchange = "test-exchange1"
                    message { "Hello World!" }
                }
            }
        }
    }

    @Test
    fun `cross-instance communication in delivery callback`() = testApplication {
        application {
            install(RabbitMQ(instanceName = "production")) {
                connectionAttempts = 3
                attemptDelay = 10
                uri = rabbitMQContainer.amqpUrl
            }
            install(RabbitMQ(instanceName = "analytics")) {
                connectionAttempts = 3
                attemptDelay = 10
                uri = rabbitMQContainer1.amqpUrl
            }
        }

        application {
            // Setup production queues
            rabbitmqTest(instanceName = "production") {
                queueBind {
                    queue = "orders"
                    exchange = "orders-exchange"
                    routingKey = "order.created"
                    queueDeclare {
                        queue = "orders"
                        durable = true
                    }
                    exchangeDeclare {
                        exchange = "orders-exchange"
                        type = "direct"
                    }
                }
            }

            // Setup analytics queues
            rabbitmqTest(instanceName = "analytics") {
                queueBind {
                    queue = "events"
                    exchange = "analytics-exchange"
                    routingKey = "user.action"
                    queueDeclare {
                        queue = "events"
                        durable = false
                    }
                    exchangeDeclare {
                        exchange = "analytics-exchange"
                        type = "topic"
                    }
                }
            }

            val analyticsMessages = AtomicInteger(0)

            // Analytics consumer
            rabbitmqTest(instanceName = "analytics") {
                basicConsume {
                    queue = "events"
                    autoAck = true
                    deliverCallback<String> { message ->
                        analyticsMessages.incrementAndGet()
                        println("Analytics: ${message.body}")
                    }
                }
            }

            // Production consumer that sends analytics events
            rabbitmqTest(instanceName = "production") {
                basicConsume {
                    queue = "orders"
                    autoAck = false
                    deliverCallback<String> { message ->
                        // Process order
                        println("Processing order: ${message.body}")
                        
                        basicAck {
                            deliveryTag = message.envelope.deliveryTag
                        }
                        
                        // Send analytics event to different instance
                        rabbitmqTest(instanceName = "analytics") {
                            basicPublish {
                                exchange = "analytics-exchange"
                                routingKey = "user.action"
                                message { "Order processed: ${message.body}" }
                            }
                        }
                    }
                }
            }

            // Publish order to production
            rabbitmqTest(instanceName = "production") {
                basicPublish {
                    exchange = "orders-exchange"
                    routingKey = "order.created"
                    message { "Order #12345" }
                }
            }

            sleep(2000)
            assertEquals(1, analyticsMessages.get())
        }
    }

    @Test
    fun `trying to access an instance that is not existent test`() = testApplication {
        application {
            install(RabbitMQ(instanceName = Instances.INSTANCE1.name)) {
                connectionAttempts = 3
                attemptDelay = 10
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            assertThrows<IllegalStateException> {
                rabbitmqTest(instanceName = "nonexistent") {
                    basicPublish {
                        exchange = "test"
                        message { "test" }
                    }
                }
            }
        }
    }
}
