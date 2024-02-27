package models

import multiplatform.UUID

data class PollHistoryItem(val id: UUID, val title: String, val responded: Boolean = false)
