package vote.util

interface Task<T> {
    fun await(): T
}

inline fun <reified T> async(crossinline block: () -> T): Task<T> {
    val res = arrayOf<T?>(null)
    val thread = Thread.ofVirtual().start {
        res[0] = block()
    }
    return object : Task<T> {
        override fun await(): T {
            thread.join()
            return res[0] as T
        }
    }
}
