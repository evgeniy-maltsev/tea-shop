package de.maltsev.lavita.teashop.repository

import de.maltsev.lavita.teashop.domain.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UserRepository: ReactiveMongoRepository<User, Long>