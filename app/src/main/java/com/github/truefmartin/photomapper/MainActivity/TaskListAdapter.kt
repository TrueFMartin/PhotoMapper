package com.github.truefmartin.photomapper.MainActivity


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.truefmartin.photomapper.Model.Task
import com.github.truefmartin.photomapper.R

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class TaskListAdapter(val taskClickedFn:(id: Int)->Unit, val buttonClickedFn:(id: Int, isChecked: Boolean)->Unit): ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TasksComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.title, current.date, current.completed)
        holder.itemView.tag= current
        holder.itemView.setOnClickListener{
            current.id?.let { it1 -> taskClickedFn(it1) }
        }
        val isComplete = current.completed ?: false
        holder.buttonCompleted.setOnClickListener {
            current.id?.let { it1 ->
            buttonClickedFn(
                it1, !isComplete)
        } }
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskItemView: TextView = itemView.findViewById(R.id.title_text)
        private val dateTextView: TextView = itemView.findViewById(R.id.date_text)
        val buttonCompleted: Button = itemView.findViewById(R.id.button_is_completed)
        private val formatterTimeDateShort = DateTimeFormatter.ofPattern("MM/dd/yy, hh:mm a", Locale.getDefault())
        val TODO = "ToDo"
        val DONE = "Done"
        fun bind(text: String?, date:LocalDateTime?, isCompleted: Boolean) {
            taskItemView.text = text
            dateTextView.text = date?.format(formatterTimeDateShort) ?: LocalDateTime.now().format(formatterTimeDateShort)
            if (isCompleted) buttonCompleted.text = DONE else buttonCompleted.text = TODO
        }
        companion object {
            fun create(parent: ViewGroup): TaskViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return TaskViewHolder(view)
            }
        }
    }

    class TasksComparator : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return (oldItem.title == newItem.title
                    && oldItem.date == newItem.date
                    && oldItem.completed == newItem.completed)
        }
    }
}
