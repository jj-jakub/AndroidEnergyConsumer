package com.jj.androidenergyconsumer.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.fragments.InternetLauncherFragment
import com.jj.androidenergyconsumer.domain.internet.FileDownloader
import com.jj.androidenergyconsumer.domain.internet.InternetPingsCreator
import com.jj.androidenergyconsumer.utils.performClick
import com.jj.androidenergyconsumer.utils.typeText
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

@RunWith(AndroidJUnit4::class)
class InternetFragmentInstrumentedTest {

    companion object {
        private const val DEFAULT_REQUEST_INTERVAL = 10L
    }

    private lateinit var fileDownloader: FileDownloader

    private lateinit var internetPingsCreator: InternetPingsCreator

    private lateinit var mockModule: Module

    @Before
    fun setup() {
        fileDownloader = spyk(inject<FileDownloader>(FileDownloader::class.java).value)
        internetPingsCreator = spyk(inject<InternetPingsCreator>(InternetPingsCreator::class.java).value)

        setupMocks()
        launchFragmentInContainer<InternetLauncherFragment>()
        checkNotWorkingStatusValue()
    }

    private fun setupMocks() {
        MockKAnnotations.init(this)
        mockModule = module {
            single(override = true) { fileDownloader }
            single(override = true) { internetPingsCreator }
        }
        GlobalContext.loadKoinModules(mockModule)
    }

    @Test
    fun startFileDownloadShouldCallDownloadFileOnFileDownloader() {
        performClick(R.id.startFileDownloadButton)
        checkWorkingStatusValue()

        coVerify { fileDownloader.downloadFile(eq(getDefaultUrl())) }

        // TODO In future launch every test in new environment instead of closing service by clicking stop
        stopInternetWork()
    }

    @Test
    fun startFileDownloadShouldCallDownloadFileOnFileDownloaderWithProperUrl() {
        val inputUrl = "http://0.0.0.0:10000/file"
        writeUrl(inputUrl)

        performClick(R.id.startFileDownloadButton)
        checkWorkingStatusValue()

        coVerify { fileDownloader.downloadFile(eq(inputUrl)) }
        stopInternetWork()
    }

    @Test
    fun startConstantInternetWorkToDefaultUrl() {
        performClick(R.id.constantInternetWorkButton)
        checkWorkingStatusValue()

        coVerify { internetPingsCreator.startOneAfterAnotherPings(getDefaultUrl()) }
        stopInternetWork()
    }

    @Test
    fun startConstantInternetWorkToCustomUrl() {
        val inputUrl = "http://0.0.0.0:10000/ping"
        writeUrl(inputUrl)

        performClick(R.id.constantInternetWorkButton)
        checkWorkingStatusValue()

        coVerify { internetPingsCreator.startOneAfterAnotherPings(inputUrl) }
        stopInternetWork()
    }

    @Test
    fun startPeriodicCallsToDefaultUrlWithDefaultInterval() {
        performClick(R.id.periodicInternetWorkButton)
        checkWorkingStatusValue()

        coVerify { internetPingsCreator.pingUrlWithPeriod(getDefaultUrl(), DEFAULT_REQUEST_INTERVAL) }
        stopInternetWork()
    }

    @Test
    fun startPeriodicCallsToDefaultUrlWithCustomInterval() {
        val interval = 256L
        writeInterval(interval)

        performClick(R.id.periodicInternetWorkButton)
        checkWorkingStatusValue()

        coVerify { internetPingsCreator.pingUrlWithPeriod(getDefaultUrl(), interval) }
        stopInternetWork()
    }

    @Test
    fun startPeriodicCallsToCustomUrlWithDefaultInterval() {
        val inputUrl = "http://0.0.0.0:10000/ping"
        writeUrl(inputUrl)

        performClick(R.id.periodicInternetWorkButton)
        checkWorkingStatusValue()

        coVerify { internetPingsCreator.pingUrlWithPeriod(inputUrl, DEFAULT_REQUEST_INTERVAL) }
        stopInternetWork()
    }

    @Test
    fun startPeriodicCallsToCustomUrlWithCustomInterval() {
        val inputUrl = "http://0.0.0.0:10000/ping"
        writeUrl(inputUrl)

        val interval = 512L
        writeInterval(interval)

        performClick(R.id.periodicInternetWorkButton)
        checkWorkingStatusValue()

        coVerify { internetPingsCreator.pingUrlWithPeriod(inputUrl, interval) }
        stopInternetWork()
    }

    @Test
    fun stopInternetCallsShouldAbortDownloadingAndPingsCreator() {
        stopInternetWork()

        coVerify { fileDownloader.cancelDownload() }
        coVerify { internetPingsCreator.stopWorking() }
    }

    private fun checkNotWorkingStatusValue() =
        onView(withId(R.id.internetWorkingStatusValue)).check(matches(withText("Not running")))

    private fun checkWorkingStatusValue() =
        onView(withId(R.id.internetWorkingStatusValue)).check(matches(withText("Running")))

    private fun stopInternetWork() {
        performClick(R.id.stopInternetCallsButton)
        checkNotWorkingStatusValue()
    }

    private fun writeUrl(url: String) = typeText(R.id.urlInput, url)

    private fun writeInterval(millis: Long) = typeText(R.id.internetIntervalInput, millis.toString())

    private fun getDefaultUrl(): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.getString(R.string.default_file_download_url)
    }
}