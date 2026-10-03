package com.example.weatherly

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        Thread {
            Thread.sleep(2000)

            runOnUiThread {

                val intent = Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )

                startActivity(intent)

                finish()
            }

        }.start()
    }
}