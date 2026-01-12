package dev.karmakrafts.ssio

import kotlinx.io.Buffer

interface AsyncRawSource : AsyncCloseable {
    suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long
}