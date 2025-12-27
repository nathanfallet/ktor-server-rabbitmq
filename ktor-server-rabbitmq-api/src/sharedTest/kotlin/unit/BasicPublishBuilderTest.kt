package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicPublishBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Channel
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Properties
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class BasicPublishBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters - path 1`() = runTest {
        // Arrange
        val builder = BasicPublishBuilder(mockChannel)
        builder.exchange = "test-exchange"
        builder.routingKey = "test-key"
        builder.message = "test".toByteArray()
        builder.mandatory = true
        builder.immediate = true
        builder.properties = Properties()

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicPublish("test-exchange", "test-key", true, true, any(), any()) }
    }

    @Test
    fun `should build with immediate only - path 2`() = runTest {
        // Arrange
        val builder = BasicPublishBuilder(mockChannel)
        builder.exchange = "test-exchange"
        builder.message = "test".toByteArray()
        builder.immediate = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicPublish("test-exchange", "", immediate = true, properties = any(), message = any()) }
    }

    @Test
    fun `should build with mandatory only - path 3`() = runTest {
        // Arrange
        val builder = BasicPublishBuilder(mockChannel)
        builder.exchange = "test-exchange"
        builder.message = "test".toByteArray()
        builder.mandatory = true

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicPublish("test-exchange", "", true, properties = any(), message = any()) }
    }

    @Test
    fun `should build with minimal parameters - path 4`() = runTest {
        // Arrange
        val builder = BasicPublishBuilder(mockChannel)
        builder.exchange = "test-exchange"
        builder.message = "test".toByteArray()

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.basicPublish("test-exchange", "", properties = any(), message = any()) }
    }

    @Test
    fun `should throw error when exchange not set`() = runTest {
        // Arrange
        val builder = BasicPublishBuilder(mockChannel)
        builder.message = "test".toByteArray()

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun `should throw error when message not set`() = runTest {
        // Arrange
        val builder = BasicPublishBuilder(mockChannel)
        builder.exchange = "test-exchange"

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}