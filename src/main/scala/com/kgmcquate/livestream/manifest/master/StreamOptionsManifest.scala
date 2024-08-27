package com.kgmcquate.livestream.manifest.master

import com.kgmcquate.livestream.manifest.media.MediaManifestInfo
import io.lindstrom.m3u8.model.{MasterPlaylist, Resolution, Variant}
import io.lindstrom.m3u8.parser.{MasterPlaylistParser, ParsingMode}

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}


case class StreamOptionsManifest(masterPlaylist: MasterPlaylist) {

  def getMediaManifestInfo(resolution: Resolution): MediaManifestInfo = {
    val variant = StreamOptionsManifest.getVariant(masterPlaylist, resolution)
    MediaManifestInfo.fromVariant(variant)
  }

  def getMediaManifestInfo(resolution: String): MediaManifestInfo = {
    val resolutionRegex = """(\d+)x(\d+)""".r
    val parsedResolution = resolutionRegex.findFirstMatchIn(resolution) match {
      case Some(resolutionMatch) => {
        val width = resolutionMatch.group(1).toInt
        val height = resolutionMatch.group(2).toInt
        Resolution.of(width, height)
      }
      case None => throw new Exception(s"Invalid resolution ${resolution}")
    }

    getMediaManifestInfo(parsedResolution)
  }
}
object StreamOptionsManifest {
  private def getVariant(masterPlaylist: MasterPlaylist, desiredResolution: Resolution): Variant = {
    masterPlaylist.variants().forEach(v => {
      if (v.resolution().isPresent && v.resolution().get().equals(desiredResolution)) {
        return v
      }
    })
    throw new Exception(s"No variant found for resolution ${desiredResolution}")
  }

  def fromUrl(url: String): StreamOptionsManifest = {
    val manifestConnection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    manifestConnection.setRequestMethod("GET")
    val manifestInputStream = manifestConnection.getInputStream

    val masterPlaylistParser = new MasterPlaylistParser(ParsingMode.LENIENT)

    val reader = new BufferedReader(new InputStreamReader(manifestInputStream))
//    println(reader.lines().toArray.mkString("\n").hashCode)
//    reader.reset()

    // Parse playlist
    val masterPlaylist = masterPlaylistParser.readPlaylist(reader)
    StreamOptionsManifest(masterPlaylist)
  }
}