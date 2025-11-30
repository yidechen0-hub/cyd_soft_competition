package com.cyd.cyd_soft_competition.activity.llmActivity

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.llm.PersonaGenerator

class LLMActivity : AppCompatActivity(){
    private val TAG = "LLMActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create UI programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 32, 32, 32)
        }

        val button = Button(this).apply {
            text = "Generate Persona"
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val textView = TextView(this).apply {
            text = "Click button to generate..."
            textSize = 16f
        }

        scrollView.addView(textView)
        layout.addView(button)
        layout.addView(scrollView)

        setContentView(layout)

        val dbPath = getDatabasePath("competition_database.db").absolutePath

        button.setOnClickListener {
            button.isEnabled = false
            textView.text = "Generating... Please wait."
            
            Thread {
                try {
                    val result = PersonaGenerator(dbPath, null).generate()
                    runOnUiThread {
                        if (result != null) {
                            textView.text = result.toString(2)
                        } else {
                            textView.text = "Failed to generate persona (result is null)."
                        }
                        button.isEnabled = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        textView.text = "Error: ${e.message}\n\n" +
                                "Possible causes:\n" +
                                "1. Network blocked (China).\n" +
                                "2. Invalid API Key.\n\n" +
                                "Switching to MOCK MODE for demonstration..."
                        
                        // Auto-switch to mock mode for next try
                        com.cyd.cyd_soft_competition.llm.LLMClient.openaiKey = "mock"
                        button.isEnabled = true
                        button.text = "Retry (Mock Mode)"
                    }
                }
            }.start()
        }
    }
}