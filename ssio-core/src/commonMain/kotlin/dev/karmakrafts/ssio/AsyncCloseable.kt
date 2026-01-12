package dev.karmakrafts.ssio

/**
 * A resource that can be closed asynchronously.
 */
interface AsyncCloseable {
    /**
     * Closes the resource asynchronously.
     *
     * This method should release any resources held by the object.
     */
    suspend fun close()

    /**
     * Closes the resource abruptly, without waiting for pending operations to complete.
     *
     * This method is intended to be used in cases where a quick shutdown is required,
     * potentially leaving the resource in an inconsistent state.
     */
    fun closeAbruptly()
}

/**
 * Executes the given [block] with this resource and then closes it correctly whether an exception
 * is thrown or not.
 *
 * @param block the block to execute.
 * @return the result of the [block].
 */
suspend inline fun <T : AsyncCloseable, R> T.use(block: suspend (T) -> R): R {
    return try {
        block(this)
    }
    finally {
        close()
    }
}