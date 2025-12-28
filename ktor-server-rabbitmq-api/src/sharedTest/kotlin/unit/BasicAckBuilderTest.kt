package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicAckBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BasicAckBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters set`() = runTest {
        // Arrange
        val builder = BasicAckBuilder(mockChannel)
        builder.deliveryTag = 123L
        builder.multiple = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicAck(123L, true) }
    }

    @Test
    fun `should build with default multiple value`() = runTest {
        // Arrange
        val builder = BasicAckBuilder(mockChannel)
        builder.deliveryTag = 456L

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicAck(456L, false) }
    }

    @Test
    fun `should throw error when deliveryTag not set`() = runTest {
        // Arrange
        val builder = BasicAckBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}