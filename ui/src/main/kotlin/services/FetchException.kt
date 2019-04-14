package services

class FetchException(message: String, val status: Short, val body: String) : Exception(message)
