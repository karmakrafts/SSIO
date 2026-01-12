package dev.karmakrafts.ssio

import kotlinx.io.Buffer

interface AsyncRawSink : AsyncCloseable {
    suspend fun write(source: Buffer, byteCount: Long)
    suspend fun flush()
}