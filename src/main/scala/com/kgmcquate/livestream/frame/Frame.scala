package com.kgmcquate.livestream.frame

case class Frame(frame: Array[Byte], frameSequence: FrameOffset) {
  override def toString: String = {
    s"Frame(${frameSequence.mediaSequence.mediaSequence}, ${frameSequence.frameNumber})"
  }
}
