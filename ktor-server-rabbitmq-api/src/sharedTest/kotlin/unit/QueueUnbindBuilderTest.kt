package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.QueueUnbindBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.*
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class QueueUnbindBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters - path 1`() = runTest {
        // Arrange
        val builder = QueueUnbindBuilder(mockChannel)
        builder.queue = "test-queue"
        builder.exchange = "test-exchange"
        builder.routingKey = "test-key"
        builder.arguments = mapOf("key" to "value")

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.queueUnbind("test-queue", "test-exchange", "test-key", mapOf("key" to "value")) }
    }

    @Test
    fun `should build without arguments - path 2`() = runTest {
        // Arrange
        val builder = QueueUnbindBuilder(mockChannel)
        builder.queue = "test-queue"
        builder.exchange = "test-exchange"
        builder.routingKey = "test-key"

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.queueUnbind("test-queue", "test-exchange", "test-key") }
    }

    @Test
    fun `should throw error when queue not set`() = runTest {
        // Arrange
        val builder = QueueUnbindBuilder(mockChannel)
        builder.exchange = "test-exchange"
        builder.routingKey = "test-key"

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun `should throw error when exchange not set`() = runTest {
        // Arrange
        val builder = QueueUnbindBuilder(mockChannel)
        builder.queue = "test-queue"
        builder.routingKey = "test-key"

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}