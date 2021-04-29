package com.jj.androidenergyconsumer.data.internet

import com.jj.androidenergyconsumer.TestCoroutineScopeProvider
import com.jj.androidenergyconsumer.data.streams.BufferedInputStreamFactory
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLConnection

@ExperimentalCoroutinesApi
class FileDownloaderTest {

    @Mock
    private lateinit var bufferedInputStreamFactory: BufferedInputStreamFactory

    @Mock
    private lateinit var urlConnectionCreator: URLConnectionCreator

    @Mock
    private lateinit var urlConnection: URLConnection

    @Mock
    private lateinit var bufferedInputStream: BufferedInputStream

    @Mock
    private lateinit var inputStream: InputStream

    private lateinit var fileDownloader: FileDownloader

    private lateinit var testCoroutineScopeProvider: TestCoroutineScopeProvider

    private lateinit var coroutineScopeProvider: ICoroutineScopeProvider

    private val downloadProgressValues = mutableListOf<DownloadProgress>()

    private val testUrl = "testUrl"
    private val testContentLength = 5000000

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        downloadProgressValues.clear()
        testCoroutineScopeProvider = TestCoroutineScopeProvider()
        coroutineScopeProvider = CoroutineScopeProvider()

        whenever(urlConnection.contentLength).thenReturn(testContentLength)
        whenever(urlConnection.getInputStream()).thenReturn(inputStream)
        whenever(urlConnectionCreator.makeConnection(any())).thenReturn(urlConnection)
        whenever(bufferedInputStreamFactory.create(any())).thenReturn(bufferedInputStream)

        fileDownloader = FileDownloader(testCoroutineScopeProvider, bufferedInputStreamFactory, urlConnectionCreator)
    }

    @Test
    fun `fileDownloader should emit download progress with download finished set to true after stream returns -1`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            whenever(bufferedInputStream.read(any())).thenReturn(8192, -1)

            observeResultsAndStartDownloading()
            assertTrue(downloadProgressValues.last().downloadFinished)
        }

    @Test
    fun `fileDownloader should emit proper amount of download progress values`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            whenever(bufferedInputStream.read(any())).thenReturn(8192, 4096, 2048, 1024, 512, 256, 128, -1)

            observeResultsAndStartDownloading()
            assertEquals(8, downloadProgressValues.size)
        }

    @Test
    fun `fileDownloader should immediately emit download progress with download finished set to true after stream first return value is -1`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            whenever(bufferedInputStream.read(any())).thenReturn(-1)

            observeResultsAndStartDownloading()
            assertTrue(downloadProgressValues.first().downloadFinished)
        }

    @Test
    fun `fileDownloader should immediately download progress with percentage 100 after stream first return value is -1`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            whenever(bufferedInputStream.read(any())).thenReturn(-1)

            observeResultsAndStartDownloading()
            assertEquals(100, downloadProgressValues.first().progressPercentage)
        }

    @Test
    fun `fileDownloader should immediately emit only one download progress after stream first return value is -1`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            whenever(bufferedInputStream.read(any())).thenReturn(-1)

            observeResultsAndStartDownloading()
            assertEquals(1, downloadProgressValues.size)
        }

    @Test
    fun `io exception while downloading should be caught and download should stop with flag downloadFinished true`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            val ioException = IOException("Download IOException")
            whenever(bufferedInputStream.read(any())).thenThrow(ioException)

            observeResultsAndStartDownloading()
            assertTrue(downloadProgressValues.first().downloadFinished)
        }

    @Test
    fun `io exception while downloading should be caught and download should stop with proper exception`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            val ioException = IOException("Download IOException")
            whenever(bufferedInputStream.read(any())).thenThrow(ioException)

            observeResultsAndStartDownloading()
            assertEquals(ioException, downloadProgressValues.first().exception)
        }

    @Test
    fun `io exception at download start should be caught and download should stop with percentage 0`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            val ioException = IOException("Download IOException")
            whenever(bufferedInputStream.read(any())).thenThrow(ioException)

            observeResultsAndStartDownloading()
            assertEquals(0, downloadProgressValues.first().progressPercentage)
        }

    @Test
    fun `io exception at download start should be caught and only one progress value should be emitted`() =
        testCoroutineScopeProvider.getIO().runBlockingTest {
            val ioException = IOException("Download IOException")
            whenever(bufferedInputStream.read(any())).thenThrow(ioException)

            observeResultsAndStartDownloading()
            assertEquals(1, downloadProgressValues.size)
        }

    @Test
    fun `cancelling download should not prevent next download to perform successfully`() {
        whenever(bufferedInputStream.read(any())).thenReturn(8192, -1)
        fileDownloader.cancelDownload()

        observeResultsAndStartDownloading()
        assertTrue(downloadProgressValues.last().downloadFinished)
    }

    private fun observeResultsAndStartDownloading() {
        testCoroutineScopeProvider.getIO().launch {
            fileDownloader.observeDownloadProgress().collect { progress -> downloadProgressValues.add(progress) }
        }

        testCoroutineScopeProvider.getIO().launch {
            fileDownloader.downloadFile(testUrl)
        }
    }
}
