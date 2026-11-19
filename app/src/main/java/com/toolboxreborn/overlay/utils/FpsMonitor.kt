package com.toolboxreborn.overlay.utils
import android.view.Choreographer
class FpsMonitor(private val onFpsUpdated: (Int) -> Unit) : Choreographer.FrameCallback {
    private var lastTime = 0L; private var frames = 0; private var running = false
    fun start() { if (running) return; running = true; lastTime = 0L; frames = 0; Choreographer.getInstance().postFrameCallback(this) }
    fun stop() { running = false; Choreographer.getInstance().removeFrameCallback(this) }
    override fun doFrame(t: Long) { if (!running) return; if (lastTime == 0L) lastTime = t; frames++; val diff = t - lastTime; if (diff >= 1_000_000_000L) { onFpsUpdated(((frames * 1_000_000_000L) / diff).toInt()); frames = 0; lastTime = t }; Choreographer.getInstance().postFrameCallback(this) }
}
