package models

import multiplatform.UUID

class ResponseDetailsView(
        val id: UUID,
        val email: String,
        val active: Boolean,
        val questions: List<ResponseDetailsQuestion>
)

class ResponseDetailsQuestion(
        val title: String,
        val responses: List<String>
)
