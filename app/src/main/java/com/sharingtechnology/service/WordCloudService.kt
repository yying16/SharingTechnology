package com.sharingtechnology.service

import android.app.IntentService
import android.content.Intent
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import com.sharingtechnology.util.WordCloud

class WordCloudService : IntentService("WordCloudService") {

    override fun onHandleIntent(intent: Intent?) {
        val content = intent?.getStringExtra("content")
        if (content != null) {
            Log.d(TAG, "WordCloud is loading")
            WordCloud.refresh(content)
            Log.d(TAG, "WordCloud has been updated.")
        }
        stopSelf()
    }
}