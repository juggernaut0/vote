package services

class FetchException(message: String, val status: Short, cause: Throwable? = null) : Exception(message, cause)
