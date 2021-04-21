package com.jj.androidenergyconsumer.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.fragments.InternetLauncherFragment
import com.jj.androidenergyconsumer.domain.internet.FileDownloader
import com.jj.androidenergyconsumer.domain.internet.InternetPingsCreator
import com.jj.androidenergyconsumer.utils.performClick
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

    private lateinit var fileDownloader: FileDownloader

    private lateinit var internetPingsCreator: InternetPingsCreator

    private lateinit var mockModule: Module

    @Before
    fun setup() {
        fileDownloader = spyk(inject<FileDownloader>(FileDownloader::class.java).value)
        internetPingsCreator = spyk(inject<InternetPingsCreator>(InternetPingsCreator::class.java).value)

        setupMocks()
        launchFragmentInContainer<InternetLauncherFragment>()
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val defaultUrl = context.getString(R.string.default_file_download_url)

        performClick(R.id.startFileDownloadButton)

        coVerify { fileDownloader.downloadFile(eq(defaultUrl)) }

        // TODO In future launch every test in new environment instead of closing service by clicking stop
        performClick(R.id.stopInternetCallsButton)
    }

    @Test
    fun stopInternetCallsShouldAbortDownloadingAndPingsCreator() {

        performClick(R.id.stopInternetCallsButton)

        coVerify { fileDownloader.cancelDownload() }
        coVerify { internetPingsCreator.stopWorking() }

        performClick(R.id.stopInternetCallsButton)
    }
}