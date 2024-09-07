package com.kgmcquate.livestream.frame

case class Frame(frame: Array[Byte], frameSequence: FrameOffset) {
  println(s"Created frame: $this")
  override def toString: String = {
    s"Frame(${frameSequence.frameNumber}, frame size: ${frame.slice(0, 10).mkString(",")})"
  }
}
