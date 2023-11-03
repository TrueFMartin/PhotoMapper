package com.github.truefmartin.photomapper.NewEditTaskActivity

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.github.truefmartin.photomapper.Model.Task
import com.github.truefmartin.photomapper.R
import com.github.truefmartin.photomapper.PhotoMapper
import com.github.truefmartin.photomapper.NotificationHandler
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

const val EXTRA_ID:String = "com.github.truefmartin.NewTaskActivity.EXTRA_ID"
class NewTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskBody: EditText
    private lateinit var tvDateView: TextView
    private lateinit var tvTimeView: TextView

    private lateinit var btnSetDate: Button
    private lateinit var btnSetTime: Button

    // Time to display on and pass from the date selector
    private lateinit var newDate: LocalDateTime
    private lateinit var newTime: LocalDateTime
    // Combination of newDate and newTime
    private lateinit var newDateTime: LocalDateTime

    private val formatterDate = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.getDefault())
    private val formatterTime = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    private lateinit var recurringVal: RecurringState

    private lateinit var spinner: Spinner

    private var isComplete = false;

    private var taskNotificationID: Int = -1
    private val newTaskViewModel: NewTaskViewModel by viewModels {
        NewTaskViewModelFactory((application as PhotoMapper).repository,-1)
    }

    private val notificationHandler = NotificationHandler
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)
        val id = intent.getIntExtra(EXTRA_ID,-1)

// -----------------Get views---------------------
        etTaskTitle = findViewById(R.id.edit_task_title)
        etTaskBody = findViewById(R.id.edit_task_body)
        tvDateView = findViewById(R.id.text_view_date)
        tvTimeView = findViewById(R.id.text_view_time)
        spinner = findViewById(R.id.spinner_recurring)
        btnSetDate = findViewById(R.id.btn_pick_date)
        btnSetTime = findViewById(R.id.btn_pick_time)
        val switchIsComplete = findViewById<SwitchMaterial>(R.id.switch_is_completed)

// --------------Set listeners--------------------
        // Create 'Confirm Delete' dialog on clicking delete button
        findViewById<Button>(R.id.button_delete).setOnClickListener {
            deleteTaskDialog(id)
        }
        btnSetTime.setOnClickListener {
            TimePickerFragment { t: LocalDateTime -> setTime(t) }.show(supportFragmentManager, "timePicker")
        }
        btnSetDate.setOnClickListener {
            val newFragment = DatePickerFragment{ d: LocalDateTime -> setDate(d) }
            newFragment.show(supportFragmentManager, "datePicker")
        }
        // Set the listener for toggling of the 'is task complete' switch
        switchIsComplete.setOnCheckedChangeListener {
                _, isToggled -> setComplete(isToggled) }
        // Set the on click function when user hits 'save'. Add or Update task in DB
        findViewById<Button>(R.id.button_save).setOnClickListener { onSaveClick(id) }

//--------------handle editing a previous task----------------

        if(id != -1){
            newTaskViewModel.updateId(id)
        }

        newTaskViewModel.curTask.observe(this){
            task->task?.let {
            etTaskTitle.setText(task.title)
            etTaskBody.setText(task.body)

            tvDateView.text = task.date.format(formatterDate)
            tvTimeView.text = task.date.format(formatterTime)
            // Since not a new task, set newDate and newTime to task's time. Combine() will combine later
            newDate = task.date
            newTime = task.date
            recurringVal = task.repeated
            switchIsComplete.isChecked = task.completed
            // Used if the user decides to delete the task
            taskNotificationID = task.noteID

            // Spinner needs a starting position from the tasks 'recurrence',
            // use display string of RecurringState 'recurringVal'.
            // Pass call back function for spinner that selects if the task is recurring
            RecurringSpinner(this, spinner, recurringVal) { a: String -> setRecurring(a) }

            }
        }
// ------------Handle a new task----------------
        if (id == -1) {
            val time = LocalDateTime.now()
            tvDateView.text = time.format(formatterDate)
            tvTimeView.text = time.format(formatterTime)
            // With a new task, set the date and time to now
            newDate = time
            newTime = time
            recurringVal = RecurringState.NONE
            RecurringSpinner(this, spinner, recurringVal) { a: String -> setRecurring(a) }

        }
    }

    // Call back function for date setter
    private fun setDate(date: LocalDateTime) {
        newDate = date
        val selectedDateTime = newDate.toLocalDate().format(formatterDate)
        tvDateView.text = selectedDateTime
    }
    // Call back function for time setter
    private fun setTime(time: LocalDateTime) {
        newTime = time
        val selectedDateTime = newTime.toLocalTime().format(formatterTime)
        tvTimeView.text = selectedDateTime
    }
    // Combine the date in time from the date setter and time setter
    private fun combineDateTime(): LocalDateTime {
        return LocalDateTime.of(
            newDate.year,
            newDate.month,
            newDate.dayOfMonth,
            newTime.hour,
            newTime.minute
        )
    }

    // Call back function for recurrence of task selector
    private fun setRecurring(s: String){
        Log.d("NewTaskActivity", "setting recurring to $s")
        recurringVal = RecurringState.valueOf(s.uppercase())
    }

    // Call back function for task completion switch
    private fun setComplete(isToggled: Boolean) {
        isComplete = isToggled
    }

    // Call back function for save button
    private fun onSaveClick(id: Int) {
        // combine the date and time selected above
        newDateTime = combineDateTime()
        CoroutineScope(SupervisorJob()).launch {
            if(id==-1) {
                taskNotificationID = Random.nextInt(Int.MAX_VALUE - 1)
                val tempTask = Task(null, etTaskTitle.text.toString(),etTaskBody.text.toString(),
                    newDateTime, isComplete, recurringVal, taskNotificationID)
                if (!isComplete) {
                    notificationHandler.scheduleNotification(tempTask)
                // If marked as 'completed', but is a recurring task, create new incomplete task at next date
                } else if (recurringVal != RecurringState.NONE) {
                    tempTask.date = recurringVal.modifyDate(tempTask.date)
                    tempTask.completed = false
                    notificationHandler.scheduleNotification(tempTask)
                    // Inform user why their task marked as 'complete' is showing as 'incomplete'
                    runOnUiThread( kotlinx.coroutines.Runnable {
                        toaster("New recurring task set to future date as 'ToDo'")
                    })
                }
                newTaskViewModel.insert(tempTask)
            // Updating a task instead of creating a new one
            }else{
                val updatedTask = newTaskViewModel.curTask.value
                if (updatedTask != null) {
                    updatedTask.title = etTaskTitle.text.toString()
                    updatedTask.body = etTaskBody.text.toString()
                    updatedTask.date = newDateTime
                    updatedTask.repeated = recurringVal

// -------------------Logic to handle recurring tasks-------------------------------
                    // If not recurring, remove or add notification depending on complete status
                    if (recurringVal == RecurringState.NONE) {
                        if (isComplete) {
                            notificationHandler.removeNotification(updatedTask.noteID)
                        } else {
                            notificationHandler.scheduleNotification(updatedTask)
                        }
                        updatedTask.completed = isComplete
                    } else {
                        // Recurring task marked complete, change date to next occurrence
                        if (isComplete) {
                            updatedTask.date = updatedTask.repeated.modifyDate(newDateTime)
                            // Inform user why their task marked as 'complete' is showing as incomplete
                            runOnUiThread( kotlinx.coroutines.Runnable {
                                toaster("New recurring task set to future date as 'ToDo'")
                            })
                        }
                        // Recurring tasks are never 'complete', update notification with 'new' task
                        notificationHandler.scheduleNotification(updatedTask)
                        updatedTask.completed = false
                    }
                    newTaskViewModel.update(updatedTask)
                }
            }
        }
        setResult(RESULT_OK)
        finish()
    }

    // Call back function for delete button
    private fun deleteTaskDialog(id: Int) {
        val builder = AlertDialog.Builder(this)
        // Set contents of dialog
        builder.setTitle(R.string.string_on_delete_title)
        builder.setMessage(R.string.string_on_delete_msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        // Set function to be called when user selects "Yes"
        builder.setPositiveButton("Yes"){ _, _ ->
            deleteTaskFn(id)
            toaster("Task deleted")
        }
        builder.setNeutralButton("Cancel"){_ , _ -> }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    // Call back function for the delete alert dialog confirmation button
    private fun deleteTaskFn(id: Int) {
        if (id == -1) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            // Delete task from DB
            CoroutineScope(SupervisorJob()).launch {
                newTaskViewModel.curTask.value?.let { newTaskViewModel.deleteTask(it) }
            }
            // Delete task alarm
            notificationHandler.removeNotification(taskNotificationID)
            setResult(RESULT_OK)
            finish()
        }

    }

    // Create a long toast with the passed in string
    private fun toaster(string: String) {
        Toast.makeText(applicationContext,string,Toast.LENGTH_LONG).show()
    }
}
