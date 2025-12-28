package integration

import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class ExchangeOperationsTests {

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
    fun `exchange declare and delete operations`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            // Declare exchange
            rabbitmqTest {
                exchangeDeclare {
                    exchange = "test-exchange-ops"
                    type = "direct"
                    durable = true
                    autoDelete = false
                    internal = false
                    arguments = mapOf("x-message-ttl" to 60000)
                }
            }

            // Use the exchange
            rabbitmqTest {
                queueBind {
                    queue = "test-queue-ops"
                    exchange = "test-exchange-ops"
                    routingKey = "test"
                    queueDeclare {
                        queue = "test-queue-ops"
                    }
                }
            }

            // Publish to exchange
            rabbitmqTest {
                basicPublish {
                    exchange = "test-exchange-ops"
                    routingKey = "test"
                    message { "Exchange test message" }
                }
            }

            sleep(500)

            // Verify message reached queue
            rabbitmqTest {
                assertEquals(1, messageCount { queue = "test-queue-ops" })
            }

            // Delete exchange
            rabbitmqTest {
                exchangeDelete {
                    exchange = "test-exchange-ops"
                    ifUnused = false
                }
            }
        }
    }

    @Test
    fun `queue unbind operations`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            // Setup
            rabbitmqTest {
                queueBind {
                    queue = "unbind-queue"
                    exchange = "unbind-exchange"
                    routingKey = "unbind-key"
                    queueDeclare {
                        queue = "unbind-queue"
                    }
                    exchangeDeclare {
                        exchange = "unbind-exchange"
                        type = "direct"
                    }
                }
            }

            // Publish message (should reach queue)
            rabbitmqTest {
                basicPublish {
                    exchange = "unbind-exchange"
                    routingKey = "unbind-key"
                    message { "Before unbind" }
                }
            }

            sleep(500)

            rabbitmqTest {
                assertEquals(1, messageCount { queue = "unbind-queue" })
            }

            // Unbind queue from exchange
            rabbitmqTest {
                queueUnbind {
                    queue = "unbind-queue"
                    exchange = "unbind-exchange"
                    routingKey = "unbind-key"
                }
            }

            // Publish another message (should not reach queue)
            rabbitmqTest {
                basicPublish {
                    exchange = "unbind-exchange"
                    routingKey = "unbind-key"
                    message { "After unbind" }
                }
            }

            sleep(500)

            // Should still have only 1 message
            rabbitmqTest {
                assertEquals(1, messageCount { queue = "unbind-queue" })
            }
        }
    }

    @Test
    fun `queue delete operations`() = testApplication {
        application {
            install(RabbitMQ) {
                uri = rabbitMQContainer.amqpUrl
            }
        }

        application {
            // Create and populate queue
            rabbitmqTest {
                queueBind {
                    queue = "delete-queue"
                    exchange = "delete-exchange"
                    queueDeclare {
                        queue = "delete-queue"
                    }
                    exchangeDeclare {
                        exchange = "delete-exchange"
                        type = "fanout"
                    }
                }
            }

            rabbitmqTest {
                basicPublish {
                    exchange = "delete-exchange"
                    message { "Message in queue" }
                }
            }

            sleep(500)

            rabbitmqTest {
                assertEquals(1, messageCount { queue = "delete-queue" })
            }

            // Delete queue
            rabbitmqTest {
                queueDelete {
                    queue = "delete-queue"
                    ifUnused = false
                    ifEmpty = false
                }
            }
        }
    }
}