package vote.services

import org.slf4j.LoggerFactory
import vote.api.v1.*
import java.lang.RuntimeException
import javax.inject.Inject

class PollValidator @Inject constructor() {
    fun validate(poll: PollCreateRequest): List<String> {
        return Validator(poll)
                .verify("Title must not be empty") { it.title.isNotBlank() }
                .verify("Must contain 1 or more questions") { it.questions.isNotEmpty() }
                .verify("All non-freeform questions must have options") {
                    it.questions.all { q -> q.type == QuestionType.FREEFORM || q.options.isNotEmpty() }
                }
                .verify("All questions must have titles") { it.questions.all { q -> q.question.isNotBlank() } }
                .errors
    }

    fun validateResponse(poll: Poll, response: PollResponse): List<String> {
        return Validator(response)
                .verify("Must have a response for every poll question") { it.responses.size == poll.questions.size }
                .errors
    }
}

private class Validator<T>(private val value: T) {
    private val _errors = mutableListOf<String>()
    val errors: List<String> get() = _errors

    fun verify(msg: String, test: (T) -> Boolean): Validator<T> {
        try {
            if (!test(value)) _errors.add(msg)
        } catch (e: RuntimeException) {
            log.warn("Could not validate property '$msg' of object $value", e)
            _errors.add(msg)
        }
        return this
    }

    companion object {
        private val log = LoggerFactory.getLogger(Validator::class.java)
    }
}
