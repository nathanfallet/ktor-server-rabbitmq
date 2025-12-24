package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.MessageCountBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.*
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MessageCountBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with queue set`() = runTest {
        // Arrange
        val builder = MessageCountBuilder(mockChannel)
        builder.queue = "test-queue"

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.messageCount("test-queue") }
    }

    @Test
    fun `should throw error when queue not set`() = runTest {
        // Arrange
        val builder = MessageCountBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}