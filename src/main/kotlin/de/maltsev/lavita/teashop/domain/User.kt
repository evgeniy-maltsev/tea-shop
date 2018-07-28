package de.maltsev.lavita.teashop.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
        @Id
        val id: Long,

        val name: String
)