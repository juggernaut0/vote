package vote.auth

import kotlin.coroutines.CoroutineContext

class AuthContext(val userId: UserId) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<AuthContext> = Key
    companion object Key : CoroutineContext.Key<AuthContext>
}
