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
    fun `trying to access an instance that is not existent test`() = testApplication {
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
            assertThrows<IllegalStateException> {
                rabbitmqTest(instanceName = "test") {
                    connectionTest(id = "test") {
                        queueBind {
                            queue = "dlq"
                            exchange = "dlx"
                            routingKey = "dlq-dlx"
                            queueDeclare {
                                queue = "dlq"
                                durable = true
                            }
                            exchangeDeclare {
                                exchange = "dlx"
                                type = "direct"
                            }
                        }
                    }
                }
            }
        }
    }
}
