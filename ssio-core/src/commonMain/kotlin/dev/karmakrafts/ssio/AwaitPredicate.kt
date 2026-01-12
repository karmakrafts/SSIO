package dev.karmakrafts.ssio

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString

fun interface AwaitPredicate {
    companion object {
        fun exhausted(): AwaitPredicate = { buffer, fetchMore -> buffer.exhausted() && !fetchMore() }
        fun available(bytes: Long): AwaitPredicate = { buffer, _ -> buffer.size >= bytes }

        fun contains(data: ByteString, maxLookahead: Long): AwaitPredicate = { buffer, fetchMore ->
            false
        }
    }

    suspend operator fun invoke(buffer: Buffer, fetchMore: suspend () -> Boolean): Boolean
}