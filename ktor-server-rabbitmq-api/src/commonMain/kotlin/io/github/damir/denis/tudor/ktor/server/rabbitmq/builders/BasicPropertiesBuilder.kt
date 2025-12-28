package io.github.damir.denis.tudor.ktor.server.rabbitmq.builders

import io.github.damir.denis.tudor.ktor.server.rabbitmq.delegator.Delegator

import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.RabbitDslMarker
import io.github.damir.denis.tudor.ktor.server.rabbitmq.model.dto.Properties

@RabbitDslMarker
class BasicPropertiesBuilder {
    private val contentTypeDelegate = Delegator<String>()
    var contentType: String by contentTypeDelegate

    private val contentEncodingDelegate = Delegator<String>()
    var contentEncoding: String by contentEncodingDelegate

    private val headersDelegate = Delegator<Map<String, Any>>()
    var headers: Map<String, Any> by headersDelegate

    private val deliveryModeDelegate = Delegator<Int>()
    var deliveryMode: Int by deliveryModeDelegate

    private val priorityDelegate = Delegator<Int>()
    var priority: Int by priorityDelegate

    private val correlationIdDelegate = Delegator<String>()
    var correlationId: String by correlationIdDelegate

    private val replyToDelegate = Delegator<String>()
    var replyTo: String by replyToDelegate

    private val expirationDelegate = Delegator<String>()
    var expiration: String by expirationDelegate

    private val messageIdDelegate = Delegator<String>()
    var messageId: String by messageIdDelegate

    private val timestampDelegate = Delegator<Long>()
    var timestamp: Long by timestampDelegate

    private val typeDelegate = Delegator<String>()
    var type: String by typeDelegate

    private val userIdDelegate = Delegator<String>()
    var userId: String by userIdDelegate

    private val appIdDelegate = Delegator<String>()
    var appId: String by appIdDelegate

    private val clusterIdDelegate = Delegator<String>()
    var clusterId: String by clusterIdDelegate

    fun build(): Properties = Properties(
        if (Delegator.verify(contentTypeDelegate)) contentType else null,
        if (Delegator.verify(contentEncodingDelegate)) contentEncoding else null,
        if (Delegator.verify(headersDelegate)) headers else null,
        if (Delegator.verify(deliveryModeDelegate)) deliveryMode else null,
        if (Delegator.verify(priorityDelegate)) priority else null,
        if (Delegator.verify(correlationIdDelegate)) correlationId else null,
        if (Delegator.verify(replyToDelegate)) replyTo else null,
        if (Delegator.verify(expirationDelegate)) expiration else null,
        if (Delegator.verify(messageIdDelegate)) messageId else null,
        if (Delegator.verify(timestampDelegate)) timestamp else null,
        if (Delegator.verify(typeDelegate)) type else null,
        if (Delegator.verify(userIdDelegate)) userId else null,
        if (Delegator.verify(appIdDelegate)) appId else null,
        if (Delegator.verify(clusterIdDelegate)) clusterId else null,
    )
}
