package com.jj.androidenergyconsumer.data.internet

import java.net.URL
import java.net.URLConnection

class URLConnectionCreator {
    fun makeConnection(sourceUrl: String): URLConnection {
        val url = URL(sourceUrl)
        return url.openConnection().apply { connect() }
    }
}