package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.QueueDeleteBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class QueueDeleteBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters - path 1`() = runTest {
        // Arrange
        val builder = QueueDeleteBuilder(mockChannel)
        builder.queue = "test-queue"
        builder.ifUnused = true
        builder.ifEmpty = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.queueDelete("test-queue", true, true) }
    }

    @Test
    fun `should build with queue only - path 2`() = runTest {
        // Arrange
        val builder = QueueDeleteBuilder(mockChannel)
        builder.queue = "test-queue"

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.queueDelete("test-queue") }
    }

    @Test
    fun `should throw error when queue not set`() = runTest {
        // Arrange
        val builder = QueueDeleteBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}