package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.StateRegistry.verify
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.Properties

@RabbitDslMarker
class BasicPropertiesBuilder {
    var contentType: String by Delegator(on = this)
    var contentEncoding: String by Delegator(on = this)
    var headers: Map<String, Any> by Delegator(on = this)
    var deliveryMode: Int by Delegator(on = this)
    var priority: Int by Delegator(on = this)
    var correlationId: String by Delegator(on = this)
    var replyTo: String by Delegator(on = this)
    var expiration: String by Delegator(on = this)
    var messageId: String by Delegator(on = this)
    var timestamp: Long by Delegator(on = this)
    var type: String by Delegator(on = this)
    var userId: String by Delegator(on = this)
    var appId: String by Delegator(on = this)
    var clusterId: String by Delegator(on = this)

    fun build(): Properties = Properties(
        if (verify(on = this@BasicPropertiesBuilder, ::contentType)) contentType else null,
        if (verify(on = this@BasicPropertiesBuilder, ::contentEncoding)) contentEncoding else null,
        if (verify(on = this@BasicPropertiesBuilder, ::headers)) headers else null,
        if (verify(on = this@BasicPropertiesBuilder, ::deliveryMode)) deliveryMode else null,
        if (verify(on = this@BasicPropertiesBuilder, ::priority)) priority else null,
        if (verify(on = this@BasicPropertiesBuilder, ::correlationId)) correlationId else null,
        if (verify(on = this@BasicPropertiesBuilder, ::replyTo)) replyTo else null,
        if (verify(on = this@BasicPropertiesBuilder, ::expiration)) expiration else null,
        if (verify(on = this@BasicPropertiesBuilder, ::messageId)) messageId else null,
        if (verify(on = this@BasicPropertiesBuilder, ::timestamp)) timestamp else null,
        if (verify(on = this@BasicPropertiesBuilder, ::type)) type else null,
        if (verify(on = this@BasicPropertiesBuilder, ::userId)) userId else null,
        if (verify(on = this@BasicPropertiesBuilder, ::appId)) appId else null,
        if (verify(on = this@BasicPropertiesBuilder, ::clusterId)) clusterId else null,
    )
}
