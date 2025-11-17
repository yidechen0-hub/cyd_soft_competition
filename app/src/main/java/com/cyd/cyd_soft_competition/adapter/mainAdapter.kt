package com.cyd.cyd_soft_competition.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.cyd.cyd_soft_competition.R

class mainAdapter(
    private val context: Context,
    private val items: List<AiTestItem>
) : RecyclerView.Adapter<mainAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.btn_main)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.button.text = item.title
        holder.button.setOnClickListener {
            val intent = Intent(context, item.activityClass)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}

data class AiTestItem(
    val title: String,
    val activityClass: Class<*>
)