package com.finnvek.anchor

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity

class ShareCaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
