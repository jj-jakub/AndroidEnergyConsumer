package com.jj.androidenergyconsumer.data.streams

import java.io.BufferedInputStream
import java.io.InputStream

class BufferedInputStreamFactory {
    fun create(inputStream: InputStream) = BufferedInputStream(inputStream)
}