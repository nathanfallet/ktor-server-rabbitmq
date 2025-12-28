package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicNackBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BasicNackBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters set`() = runTest {
        // Arrange
        val builder = BasicNackBuilder(mockChannel)
        builder.deliveryTag = 123L
        builder.multiple = true
        builder.requeue = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicNack(123L, true, true) }
    }

    @Test
    fun `should build with default values`() = runTest {
        // Arrange
        val builder = BasicNackBuilder(mockChannel)
        builder.deliveryTag = 456L

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicNack(456L, false, false) }
    }

    @Test
    fun `should throw error when deliveryTag not set`() = runTest {
        // Arrange
        val builder = BasicNackBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}