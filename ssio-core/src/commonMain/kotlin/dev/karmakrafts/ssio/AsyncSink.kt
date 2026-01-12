package dev.karmakrafts.ssio

import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

sealed interface AsyncSink : AsyncRawSink {
    @InternalIoApi
    val buffer: Buffer
}