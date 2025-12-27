package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicGetBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.*
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BasicGetBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters set`() = runTest {
        // Arrange
        val builder = BasicGetBuilder(mockChannel)
        builder.queue = "test-queue"
        builder.autoAck = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicGet("test-queue", true) }
    }

    @Test
    fun `should throw error when queue not set`() = runTest {
        // Arrange
        val builder = BasicGetBuilder(mockChannel)
        builder.autoAck = false

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun `should throw error when autoAck not set`() = runTest {
        // Arrange
        val builder = BasicGetBuilder(mockChannel)
        builder.queue = "test-queue"

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}