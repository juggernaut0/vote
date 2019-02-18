package vote.db

import org.jooq.TableRecord
import java.util.concurrent.CompletableFuture

fun <R : TableRecord<R>> TableRecord<R>.insertAsync() = CompletableFuture.supplyAsync { insert() }!!
