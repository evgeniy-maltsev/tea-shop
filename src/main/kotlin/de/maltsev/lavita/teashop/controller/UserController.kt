package de.maltsev.lavita.teashop.controller

import de.maltsev.lavita.teashop.domain.User
import de.maltsev.lavita.teashop.repository.UserRepository
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import javax.annotation.PostConstruct

//@Controller
//class UserController(
//        val userRepository: UserRepository
//) {
//
//    @SendTo("/reply/users")
//    @MessageMapping("/topic/users")
//    fun list(): Flux<User> = userRepository.findAll()
//
//    fun save(user: User) = userRepository.save(user)
//
//    @PostConstruct
//    fun create() {
//        save(User(1, "Marko")).block()
//    }
//}