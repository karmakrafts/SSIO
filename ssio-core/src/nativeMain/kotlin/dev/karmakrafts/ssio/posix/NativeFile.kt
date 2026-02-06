/*
 * Copyright 2026 Karma Krafts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.ssio.posix

import dev.karmakrafts.ssio.AsyncCloseable
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.atomics.AtomicBoolean
import platform.posix.close as posixClose
import platform.posix.read as posixRead
import platform.posix.write as posixWrite

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class NativeFile( // @formatter:off
    val fd: Int,
    coroutineScope: CoroutineScope
) : AsyncCloseable { // @formatter:on
    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val channel: Channel<NativeIoTask> = Channel()

    private val ioJob: Job = coroutineScope.launch {
        for (task in channel) dispatchTask(task)
    }

    private fun dispatchTask(task: NativeIoTask) {
        when (task) {
            is NativeIoTask.Read -> {
                task.result.complete(task.action(fd))
            }

            is NativeIoTask.Write -> {
                task.action(fd)
                task.result.complete(Unit)
            }

            is NativeIoTask.Flush -> {
                platformSyncFd(fd)
                task.result.complete(Unit)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is NativeFile) false
        else fd == other.fd
    }

    override fun hashCode(): Int = fd.hashCode()
    override fun toString(): String = "NativeFile[fd=$fd]"

    suspend fun flush() {
        val task = NativeIoTask.Flush()
        channel.send(task)
        task.result.await()
    }

    suspend fun read(buffer: COpaquePointer, bufferSize: UInt): Long {
        val task = NativeIoTask.Read({ fd ->
            posixRead(fd, buffer, bufferSize.convert()).convert()
        })
        channel.send(task)
        return task.result.await()
    }

    suspend fun write(buffer: COpaquePointer, bufferSize: UInt) {
        val task = NativeIoTask.Write({ fd ->
            posixWrite(fd, buffer, bufferSize.convert())
        })
        channel.send(task)
        task.result.await()
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "NativeFile is already closed"
        }
        channel.close()
        ioJob.join() // Gracefully wait for IO job to finish
        posixClose(fd)
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "NativeFile is already closed"
        }
        channel.close()
        runBlocking { // Block and wait until IO job has finished
            ioJob.join()
        }
        posixClose(fd)
    }
}