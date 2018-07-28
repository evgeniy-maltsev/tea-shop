package de.maltsev.lavita.teashop.socket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.maltsev.lavita.teashop.domain.User
import de.maltsev.lavita.teashop.repository.UserRepository
import org.reactivestreams.Publisher
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import kotlin.reflect.KClass

enum class ReactiveEventType(val requestClass: KClass<out ServiceRequest>) {
    GET_USER_BY_ID(GetUserByIdRequest::class)
}

data class GetUserByIdRequest(
        val id: Long
) : ServiceRequest

interface ServiceRequest

data class ReactiveRequest(
        val eventId: String,
        val eventType: ReactiveEventType,
        val payload: String
)

data class ReactiveReply(
        val eventId: String,
        val eventType: ReactiveEventType,
        val payload: String
)

interface ReactiveHandler<T : ServiceRequest, R : Any> {
    fun handle(request: T): Publisher<R>
}

@Component("GET_USER_BY_ID")
class UserHandlers(val userRepository: UserRepository) : ReactiveHandler<GetUserByIdRequest, User> {
    override fun handle(request: GetUserByIdRequest): Mono<User> {
        return userRepository.findById(request.id)
    }
}


@Component
class ReactiveWebSocketHandler(val ctx: ApplicationContext, val objectMapper: ObjectMapper) : WebSocketHandler {
    override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
        val reply = webSocketSession
                .receive()
                .map { objectMapper.readValue<ReactiveRequest>(it.payloadAsText) }
                .log()
                .flatMap { reactiveRequest ->
                    val request = objectMapper.readValue(reactiveRequest.payload, reactiveRequest.eventType.requestClass.java)
                    ctx.getBean<ReactiveHandler<ServiceRequest, Any>>(reactiveRequest.eventType.name)
                            .handle(request)
                            .toFlux()
                            .map {
                                objectMapper.writeValueAsString(it)
                            }
                            .map {
                                ReactiveReply(
                                        reactiveRequest.eventId,
                                        reactiveRequest.eventType,
                                        it
                                )
                            }
                            .map {
                                objectMapper.writeValueAsString(it)
                            }
                            .map {
                                webSocketSession.textMessage(it)
                            }
                }
        return webSocketSession.send(reply)
    }
}