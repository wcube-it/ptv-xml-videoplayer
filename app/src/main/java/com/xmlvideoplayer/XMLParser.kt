package com.xmlvideoplayer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.io.InputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.nio.charset.StandardCharsets

data class MediaFile(
    var url: String? = null,
    var type: String? = null,
    var width: String? = null,
    var height: String? = null
)

suspend fun parseMediaFilesFromUrl(urlString: String): List<MediaFile> = withContext(Dispatchers.IO) {
    val mediaFileList = mutableListOf<MediaFile>()
    val url = URL(urlString)
    val inputStream: InputStream = BufferedInputStream(url.openStream())

    try {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()

        // Set up the parser with UTF-8 encoding
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, StandardCharsets.UTF_8.name())

        var eventType = parser.eventType
        var currentMediaFile: MediaFile? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "MediaFile") {
                        currentMediaFile = MediaFile(
                            type = parser.getAttributeValue(null, "type"),
                            width = parser.getAttributeValue(null, "width"),
                            height = parser.getAttributeValue(null, "height")
                        )
                    }
                }
                XmlPullParser.TEXT -> {
                    if (currentMediaFile != null && currentMediaFile.url == null) {
                        currentMediaFile.url = parser.text.trim()
                    }
                }
                XmlPullParser.CDSECT -> {
                    if (currentMediaFile != null && currentMediaFile.url == null) {
                        currentMediaFile.url = parser.text.trim()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "MediaFile") {
                        // get only the url without CDATA
                        currentMediaFile?.url = currentMediaFile?.url?.removePrefix("<![CDATA[")
                            ?.removeSuffix("]]>")
                        currentMediaFile?.let { mediaFileList.add(it) }
                        currentMediaFile = null
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("parseMediaFilesFromUrl", "Error parsing XML", e)
    } finally {
        inputStream.close()
    }

    return@withContext mediaFileList
}
