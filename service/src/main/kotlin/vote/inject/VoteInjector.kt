package vote.inject

import dagger.Component
import vote.VoteApp
import javax.inject.Singleton

@Component(modules = [VoteModule::class])
@Singleton
interface VoteInjector {
    fun app(): VoteApp
}
