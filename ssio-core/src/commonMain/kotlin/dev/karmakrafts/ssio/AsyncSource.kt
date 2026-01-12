package dev.karmakrafts.ssio

import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

sealed interface AsyncSource : AsyncRawSource {
    @InternalIoApi
    val buffer: Buffer

    suspend fun await(predicate: AwaitPredicate): Result<Boolean>
    suspend fun awaitOrThrow(predicate: AwaitPredicate) = await(predicate).getOrThrow()
}