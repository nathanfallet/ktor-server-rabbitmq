package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicQosBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.interfaces.Channel
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BasicQosBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters - path 1`() = runTest {
        // Arrange
        val builder = BasicQosBuilder(mockChannel)
        builder.prefetchSize = 100
        builder.prefetchCount = 10
        builder.global = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicQos(100, 10, true) }
    }

    @Test
    fun `should build with prefetchCount and global - path 2`() = runTest {
        // Arrange
        val builder = BasicQosBuilder(mockChannel)
        builder.prefetchCount = 5
        builder.global = false
        // Note: prefetchSize not set, so this should hit path 2

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicQos(prefetchCount = 5, global = false) }
    }

    @Test
    fun `should build with prefetchCount only - path 3`() = runTest {
        // Arrange
        val builder = BasicQosBuilder(mockChannel)
        builder.prefetchCount = 15

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicQos(prefetchCount = 15) }
    }

    @Test
    fun `should throw error when prefetchCount not set`() = runTest {
        // Arrange
        val builder = BasicQosBuilder(mockChannel)

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}