package com.example.revision_app.tasks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.revision_app.R
import com.example.revision_app.retrofit.task.TaskDto

class TaskAdapter(private var tasks: MutableList<TaskDto>, val listener: OnItemClickListener) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private lateinit var mContext: Context

    fun setTasks(tasks: List<TaskDto>) {
        this.tasks = tasks as MutableList<TaskDto>
        notifyDataSetChanged()
    }

    fun getItemAtPosition(index: Int): TaskDto {
        return this.tasks[index]
    }

    fun removeItem(index: Int) {
        this.tasks.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addItem( task: TaskDto) {
        this.tasks.add(task)
        notifyDataSetChanged()
    }

    fun getItemAt(task: TaskDto): Int{
        return this.tasks.indexOf(task)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val taskDesc: TextView = itemView.findViewById(R.id.tvTaskDes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        mContext = parent.context
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = tasks[position]
        holder.taskName.text = currentTask.desc
        holder.taskDesc.text = currentTask.value.toString()

        holder.itemView.setOnLongClickListener{
            val item = this.tasks[position]
            listener.onItemLongClick(item, holder.itemView)
            true
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}