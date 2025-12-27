package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicRejectBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.*
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BasicRejectBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters set`() = runTest {
        // Arrange
        val builder = BasicRejectBuilder(mockChannel)
        builder.deliveryTag = 123L
        builder.requeue = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicReject(123L, true) }
    }

    @Test
    fun `should build with default requeue value`() = runTest {
        // Arrange
        val builder = BasicRejectBuilder(mockChannel)
        builder.deliveryTag = 456L

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicReject(456L, false) }
    }

    @Test
    fun `should throw error when deliveryTag not set`() = runTest {
        // Arrange
        val builder = BasicRejectBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}