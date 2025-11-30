package com.cyd.cyd_soft_competition.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.adapter.AiTestItem
import com.cyd.cyd_soft_competition.adapter.mainAdapter
import com.cyd.cyd_soft_competition.activity.competitionActivity.EntryActivity
import com.cyd.cyd_soft_competition.activity.llmActivity.LLMActivity
import com.cyd.cyd_soft_competition.databinding.ActivityMainBinding
import com.cyd.cyd_soft_competition.remoteService.CommitAITaskActivity
import com.cyd.cyd_soft_competition.remoteService.GetTasksResActivity
import com.cyd.cyd_soft_competition.remoteService.TestFdsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // 视图绑定
    private val TAG = "mianActivity"
    private lateinit var pythonModule: PyObject // Python 模块实例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Python 模块（my_python.py 对应模块名 "my_python"）
        val python = Python.getInstance()
        pythonModule = python.getModule("photo_exif_reader")

        val mainTests = listOf(
            AiTestItem(resources.getString(R.string.build_db), DBActivity::class.java),
            AiTestItem(resources.getString(R.string.get_geo), ReGeoCodeActivity::class.java),
            AiTestItem(resources.getString(R.string.test), ScrollImageActivity::class.java),
            AiTestItem(resources.getString(R.string.mutil_img), MutilImgActivity::class.java),
            AiTestItem(resources.getString(R.string.mask_img), MaskDemoActivity::class.java),
            AiTestItem(resources.getString(R.string.entry), EntryActivity::class.java),
            AiTestItem(resources.getString(R.string.competition_buildDB), BuildDBActivity::class.java),
            AiTestItem(resources.getString(R.string.fds), TestFdsActivity::class.java),
            AiTestItem(resources.getString(R.string.llm), LLMActivity::class.java),
            AiTestItem(resources.getString(R.string.GetTasksResActivity), GetTasksResActivity::class.java),
            AiTestItem(resources.getString(R.string.CommitAITaskActivity), CommitAITaskActivity::class.java),

        )
        binding.rvMain.layoutManager = GridLayoutManager(this, 2)
        binding.rvMain.adapter = mainAdapter(this, mainTests)




    }


}