package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.ExchangeDeclareBuilder
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.*
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ExchangeDeclareBuilderTest {

    private val mockChannel = mockk<Channel>(relaxed = true)

    @Test
    fun `should build with all parameters set`() = runTest {
        // Arrange
        val builder = ExchangeDeclareBuilder(mockChannel)
        builder.exchange = "test-exchange"
        builder.type = "direct"
        builder.durable = true
        builder.autoDelete = false
        builder.internal = false
        builder.arguments = mapOf("key" to "value")

        // Act
        builder.build()

        // Assert
        coVerify { mockChannel.exchangeDeclare("test-exchange", "direct", true, false, false, mapOf("key" to "value")) }
    }

    @Test
    fun `should throw error when exchange not set`() = runTest {
        // Arrange
        val builder = ExchangeDeclareBuilder(mockChannel)
        builder.type = "direct"

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun `should throw error when type not set`() = runTest {
        // Arrange
        val builder = ExchangeDeclareBuilder(mockChannel)
        builder.exchange = "test-exchange"

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }
}