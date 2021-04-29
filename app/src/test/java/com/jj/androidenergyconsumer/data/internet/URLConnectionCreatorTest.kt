package com.jj.androidenergyconsumer.data.internet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.MalformedURLException
import java.net.UnknownHostException

class URLConnectionCreatorTest {

    private lateinit var urlConnectionCreator: URLConnectionCreator

    @BeforeEach
    fun setup() {
        urlConnectionCreator = URLConnectionCreator()
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("properProtocols")
    fun `should create connection with proper protocol`(protocol: String) {
        val host = "google.com"
        val url = "$protocol://$host"
        val createdConnection = urlConnectionCreator.makeConnection(url)

        assertEquals(protocol, createdConnection.url.protocol)
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("properHosts")
    fun `should create connection to proper host`(host: String) {
        val protocol = "http"
        val url = "$protocol://$host"
        val createdConnection = urlConnectionCreator.makeConnection(url)

        assertEquals(host, createdConnection.url.host)
    }

    @ParameterizedTest
    @MethodSource("noProtocolUrls")
    fun `should throw exception MalformedURLException about no protocol`(url: String) {
        assertThrows<MalformedURLException> { urlConnectionCreator.makeConnection(url) }
    }

    @ParameterizedTest
    @MethodSource("unknownHosts")
    fun `should throw exception about unknown host`(host: String) {
        val protocol = "http"
        val url = "$protocol://$host"

        assertThrows<UnknownHostException> { urlConnectionCreator.makeConnection(url) }
    }

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun noProtocolUrls() = listOf("a", "a.com")

        @JvmStatic
        fun properProtocols() = listOf("http", "https")

        @JvmStatic
        fun unknownHosts() = listOf("a.com", "b")

        @JvmStatic
        fun properHosts() = listOf("google.com", "stackoverflow.com")
    }
}