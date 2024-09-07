package com.kgmcquate.livestream

import org.jsoup.Jsoup
import org.jsoup.select.Elements

import java.time.Instant

case class YouTubePage(url: String) {

  private var currentManifestUrl: String = _
  private var currentManifestUrlExpiration: Instant = Instant.now

  def manifestIsExpired: Boolean = {
    currentManifestUrlExpiration.isBefore(Instant.now)
  }

  private def getManifestUrl: String = {
    val doc = Jsoup.connect(url).get
    val scripts = doc.getElementsByTag("script")
    val jsonString = YouTubePage.findScriptWithStreamData(scripts).split("};", 2).head + "}"
    val parsed = ujson.read(jsonString)
    if (!parsed.obj.contains("streamingData")) {
      throw new Exception(s"No stream found. Page received: \n${doc.data()}")
    }

    parsed("streamingData")("hlsManifestUrl").str
  }

  def getCurrentManifestUrl: String = {
    if (currentManifestUrl == null || manifestIsExpired) {
      currentManifestUrl = getManifestUrl

      val splits = currentManifestUrl.split("/", 10)

      val expirationStr = splits(splits.indexOf("expire") + 1).toLong
      val expirationInstant = java.time.Instant.ofEpochSecond(expirationStr)
      currentManifestUrlExpiration = expirationInstant.minusSeconds(60*60) // Refresh 1 hour early for safety
    }

    currentManifestUrl
  }
}
object YouTubePage {
  private def findScriptWithStreamData(elements: Elements): String = {
    val startString = "var ytInitialPlayerResponse = "
    elements.forEach(s => {
      if (s.data().startsWith(startString)) {
        return s.data().substring(startString.length)
      }
    })
    throw new Exception("No script found")
  }
}