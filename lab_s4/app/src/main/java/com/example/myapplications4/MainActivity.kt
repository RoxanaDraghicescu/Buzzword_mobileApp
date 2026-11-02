package com.example.myapplications4

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val taskInput = findViewById<EditText>(R.id.taskInput)
        val addButton = findViewById<Button>(R.id.addButton)
        val container = findViewById<LinearLayout>(R.id.taskContainer)

        val dao = AppDatabase.create(this).taskDao()           // Room (local)
        val cloud = FirebaseFirestore.getInstance().collection("tasks") // Firebase (cloud)

        // The task being edited
        var editingTask: Task? = null
        var editingView: CheckBox? = null

        // Save = create or update, in Room and in Firebase
        fun save(task: Task) {
            lifecycleScope.launch { dao.save(task) }
            cloud.document(task.id).set(task)
        }

        // A function that shows one task and adds it to the container
        fun showTask(initial: Task) {
            var task = initial
            val view = CheckBox(this)
            view.text = task.text
            view.isChecked = task.done
            view.textSize = 18f

            // UPDATE: tick the checkbox
            view.setOnCheckedChangeListener { _, checked ->
                task = task.copy(done = checked)
                save(task)
            }

            // Long-press the task: Edit or Delete
            view.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setItems(arrayOf("Edit", "Delete")) { _, which ->
                        if (which == 0) {
                            taskInput.setText(task.text)
                            taskInput.setSelection(taskInput.text.length)
                            editingTask = task
                            editingView = view
                            addButton.text = "Save"
                        } else {
                            // DELETE
                            lifecycleScope.launch { dao.delete(task) }
                            cloud.document(task.id).delete()
                            container.removeView(view)
                            editingTask = null
                            addButton.text = "Add"
                            taskInput.text.clear()
                        }
                    }
                    .show()
                true
            }

            // Editing replaces the task text
            view.tag = { newText: String ->
                task = task.copy(text = newText)
                view.text = newText
                save(task)
            }

            container.addView(view)
        }

        // READ: load the saved tasks from Room
        lifecycleScope.launch {
            dao.getAll().forEach { showTask(it) }
        }

        addButton.setOnClickListener {
            val text = taskInput.text.toString().trim()

            if (text.isEmpty()) {
                Toast.makeText(this, "Write a task first", Toast.LENGTH_SHORT).show()
            } else if (editingTask != null) {
                // UPDATE
                @Suppress("UNCHECKED_CAST")
                (editingView!!.tag as (String) -> Unit)(text)
                editingTask = null
                editingView = null
                addButton.text = "Add"
                taskInput.text.clear()
            } else {
                // CREATE
                val task = Task(UUID.randomUUID().toString(), text)
                save(task)
                showTask(task)
                taskInput.text.clear()
            }
        }
    }
}
