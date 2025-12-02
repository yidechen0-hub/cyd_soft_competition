package com.cyd.cyd_soft_competition.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.activity.endingActivity.EndingImgActivity
import com.cyd.cyd_soft_competition.llm.PersonaGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class InsightDetailActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val typewriterDelay = 100L // 每个字符的延迟（毫秒）
    private val sectionDelay = 500L // 每个段落之间的延迟

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insight_detail)

        findViewById<LinearLayout>(R.id.rootLayout).setOnClickListener {
            startActivity(Intent(this, EndingImgActivity::class.java))
        }
        
        // Get JSON from Intent, if available
        val intentJson = intent.getStringExtra("resultJson")
        if (!intentJson.isNullOrEmpty() && intentJson != "{}") {
            // Use the pre-loaded JSON
            try {
                val jsonObj = JSONObject(intentJson)
                showContentWithAnimation(jsonObj)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "数据解析失败", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Fallback: load data asynchronously
            loadInsightData()
        }
    }

    private fun loadInsightData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbPath = getDatabasePath("competition_database.db").absolutePath
            try {
                val resultObj = PersonaGenerator(dbPath, null).generate()
                val resultJson = resultObj?.toString(2)

                withContext(Dispatchers.Main) {
                    if (!resultJson.isNullOrEmpty()) {
                        try {
                            val jsonObj = JSONObject(resultJson)
                            showContentWithAnimation(jsonObj)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@InsightDetailActivity, "解析数据失败", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@InsightDetailActivity, "生成数据为空", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InsightDetailActivity, "生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showContentWithAnimation(jsonObj: JSONObject) {
        var delay = 0L

        // 1. 展示场景（打字机效果）
        val tvScene = findViewById<TextView>(R.id.tvScene)
        val sceneText = "🎬 场景：${jsonObj.optString("scene")}"
        handler.postDelayed({
            showViewWithFade(tvScene)
            typewriterEffect(tvScene, sceneText)
        }, delay)
        delay += sceneText.length * typewriterDelay + sectionDelay

        // 2. 展示summary（打字机效果）
        val tvSummary = findViewById<TextView>(R.id.tvSummary)
        val summaryText = jsonObj.optString("summary")
        handler.postDelayed({
            showViewWithFade(tvSummary)
            typewriterEffect(tvSummary, summaryText)
        }, delay)
        delay += summaryText.length * typewriterDelay + sectionDelay

        // 3. 展示用户画像模块
        val layoutUserPortrait = findViewById<LinearLayout>(R.id.layoutUserPortrait)
        handler.postDelayed({
            showViewWithFade(layoutUserPortrait)
        }, delay)
        delay += sectionDelay

        val userPortraitObj = jsonObj.optJSONObject("user_portrait")
        if (userPortraitObj != null) {
            // 3.1 生活主旋律
            val tvLifeTheme = findViewById<TextView>(R.id.tvLifeTheme)
            val lifeThemeText = "🎯 生活主旋律：${userPortraitObj.optString("life_theme")}"
            handler.postDelayed({
                typewriterEffect(tvLifeTheme, lifeThemeText)
            }, delay)
            delay += lifeThemeText.length * typewriterDelay + sectionDelay
            
            // 3.2 情绪特质列表
            val layoutEmotionalTraits = findViewById<LinearLayout>(R.id.layoutEmotionalTraits)
            val emotionalTraits = userPortraitObj.optJSONArray("emotional_traits")
            if (emotionalTraits != null) {
                for (i in 0 until emotionalTraits.length()) {
                    val text = emotionalTraits.optString(i)
                    val tv = createItemTextView(text)
                    layoutEmotionalTraits.addView(tv)
                    
                    handler.postDelayed({
                        showViewWithFade(tv)
                        typewriterEffect(tv, text)
                    }, delay)
                    delay += text.length * typewriterDelay + 300
                }
            }
            delay += sectionDelay

            // 3.3 行为模式列表
            val layoutBehaviorPatterns = findViewById<LinearLayout>(R.id.layoutBehaviorPatterns)
            val behaviorPatterns = userPortraitObj.optJSONArray("behavior_patterns")
            if (behaviorPatterns != null) {
                for (i in 0 until behaviorPatterns.length()) {
                    val text = behaviorPatterns.optString(i)
                    val tv = createItemTextView(text)
                    layoutBehaviorPatterns.addView(tv)
                    
                    handler.postDelayed({
                        showViewWithFade(tv)
                        typewriterEffect(tv, text)
                    }, delay)
                    delay += text.length * typewriterDelay + 300
                }
            }
            delay += sectionDelay

            // 3.4 关系特质列表
            val layoutRelationshipTraits = findViewById<LinearLayout>(R.id.layoutRelationshipTraits)
            val relationshipTraits = userPortraitObj.optJSONArray("relationship_traits")
            if (relationshipTraits != null) {
                for (i in 0 until relationshipTraits.length()) {
                    val text = relationshipTraits.optString(i)
                    val tv = createItemTextView(text)
                    layoutRelationshipTraits.addView(tv)
                    
                    handler.postDelayed({
                        showViewWithFade(tv)
                        typewriterEffect(tv, text)
                    }, delay)
                    delay += text.length * typewriterDelay + 300
                }
            }
        }
        delay += sectionDelay

        // 4. 展示故事洞察
        val layoutStoryPoints = findViewById<LinearLayout>(R.id.layoutStoryPoints)
        handler.postDelayed({
            showViewWithFade(layoutStoryPoints)
        }, delay)
        delay += sectionDelay

        val storyPoints = jsonObj.optJSONArray("story_points")
        if (storyPoints != null) {
            for (i in 0 until storyPoints.length()) {
                val text = storyPoints.optString(i)
                val tv = createItemTextView(text)
                layoutStoryPoints.addView(tv)
                
                handler.postDelayed({
                    showViewWithFade(tv)
                    typewriterEffect(tv, text)
                }, delay)
                delay += text.length * typewriterDelay + 300
            }
        }
        delay += sectionDelay

        // 5. 展示建议
        val layoutSuggestions = findViewById<LinearLayout>(R.id.layoutSuggestions)
        handler.postDelayed({
            showViewWithFade(layoutSuggestions)
        }, delay)
        delay += sectionDelay

        val suggestions = jsonObj.optJSONArray("suggestions")
        if (suggestions != null) {
            for (i in 0 until suggestions.length()) {
                val text = suggestions.optString(i)
                val tv = createItemTextView(text)
                layoutSuggestions.addView(tv)
                
                handler.postDelayed({
                    showViewWithFade(tv)
                    typewriterEffect(tv, text)
                }, delay)
                delay += text.length * typewriterDelay + 300
            }
        }
    }

    /**
     * 打字机效果
     */
    private fun typewriterEffect(textView: TextView, fullText: String) {
        textView.text = ""
        var currentIndex = 0
        
        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex <= fullText.length) {
                    textView.text = fullText.substring(0, currentIndex)
                    currentIndex++
                    handler.postDelayed(this, typewriterDelay)
                }
            }
        }
        handler.post(runnable)
    }

    /**
     * 淡入展示单个View
     */
    private fun showViewWithFade(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        
        // 淡入 + 从下方滑入
        val alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val translateAnim = ObjectAnimator.ofFloat(view, "translationY", 50f, 0f)
        
        alphaAnim.duration = 500
        translateAnim.duration = 500
        
        alphaAnim.start()
        translateAnim.start()
    }

    /**
     * 创建列表项TextView
     */
    private fun createItemTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = ""
            textSize = 15f
            setTextColor(resources.getColor(R.color.colorTextSecondary, theme))
            setLineSpacing(4f, 1.0f)
            visibility = View.INVISIBLE
            setPadding(0, 4, 0, 4)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}