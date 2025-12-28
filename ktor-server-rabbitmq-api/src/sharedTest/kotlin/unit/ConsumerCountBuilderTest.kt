package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.ConsumerCountBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ConsumerCountBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with queue set`() = runTest {
        // Arrange
        val builder = ConsumerCountBuilder(mockChannel)
        builder.queue = "test-queue"

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.consumerCount("test-queue") }
    }

    @Test
    fun `should throw error when queue not set`() = runTest {
        // Arrange
        val builder = ConsumerCountBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}