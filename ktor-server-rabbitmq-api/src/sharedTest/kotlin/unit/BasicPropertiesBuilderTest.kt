package unit

import io.github.damir.denis.tudor.ktor.server.rabbitmq.builders.BasicPropertiesBuilder
import kotlin.test.*

class BasicPropertiesBuilderTest {

    @Test
    fun `should build with all properties set`() {
        // Arrange
        val builder = BasicPropertiesBuilder()
        builder.contentType = "application/json"
        builder.contentEncoding = "utf-8"
        builder.headers = mapOf("key" to "value")
        builder.deliveryMode = 2
        builder.priority = 1
        builder.correlationId = "corr-123"
        builder.replyTo = "reply-queue"
        builder.expiration = "60000"
        builder.messageId = "msg-123"
        builder.timestamp = 1234567890L
        builder.type = "test-type"
        builder.userId = "user-123"
        builder.appId = "app-123"
        builder.clusterId = "cluster-123"

        // Act
        val properties = builder.build()

        // Assert
        assertEquals("application/json", properties.contentType)
        assertEquals("utf-8", properties.contentEncoding)
        assertEquals(mapOf("key" to "value"), properties.headers)
        assertEquals(2, properties.deliveryMode)
        assertEquals(1, properties.priority)
        assertEquals("corr-123", properties.correlationId)
        assertEquals("reply-queue", properties.replyTo)
        assertEquals("60000", properties.expiration)
        assertEquals("msg-123", properties.messageId)
        assertEquals(1234567890L, properties.timestamp)
        assertEquals("test-type", properties.type)
        assertEquals("user-123", properties.userId)
        assertEquals("app-123", properties.appId)
        assertEquals("cluster-123", properties.clusterId)
    }

    @Test
    fun `should build with no properties set`() {
        // Arrange
        val builder = BasicPropertiesBuilder()

        // Act
        val properties = builder.build()

        // Assert
        assertNull(properties.contentType)
        assertNull(properties.contentEncoding)
        assertNull(properties.headers)
        assertNull(properties.deliveryMode)
        assertNull(properties.priority)
        assertNull(properties.correlationId)
        assertNull(properties.replyTo)
        assertNull(properties.expiration)
        assertNull(properties.messageId)
        assertNull(properties.timestamp)
        assertNull(properties.type)
        assertNull(properties.userId)
        assertNull(properties.appId)
        assertNull(properties.clusterId)
    }

    @Test
    fun `should build with partial properties set`() {
        // Arrange
        val builder = BasicPropertiesBuilder()
        builder.contentType = "text/plain"
        builder.deliveryMode = 1

        // Act
        val properties = builder.build()

        // Assert
        assertEquals("text/plain", properties.contentType)
        assertEquals(1, properties.deliveryMode)
        assertNull(properties.contentEncoding)
        assertNull(properties.headers)
    }
}