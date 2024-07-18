package com.example.productivitygame.foreground

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.productivitygame.ui.TimerContainer

const val ACTION_PAUSE_TIMER = "PAUSE TIMER"
const val ACTION_RESUME_TIMER = "RESUME TIMER"
const val ACTION_NEXT_SEGMENT = "NEXT SEGMENT"

class TimerServiceReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PAUSE_TIMER -> {
                TimerContainer.pauseTimer()
            }
            ACTION_RESUME_TIMER -> {
                TimerContainer.resumeTimer()
            }
            ACTION_NEXT_SEGMENT -> {
                TimerContainer.startNextSegment()
            }
        }
    }
}