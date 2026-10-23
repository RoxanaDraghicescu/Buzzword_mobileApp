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

        // The task being edited
        var editingTask: CheckBox? = null

        // A function that creates one task and adds it to the container
        fun addTask(text: String) {
            val task = CheckBox(this)
            task.text = text
            task.textSize = 18f

            // Long-press the task: Edit or Delete
            task.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setItems(arrayOf("Edit", "Delete")) { _, which ->
                        if (which == 0) {
                            taskInput.setText(task.text)
                            taskInput.setSelection(taskInput.text.length)
                            editingTask = task
                            addButton.text = "Save"
                        } else {
                            container.removeView(task)   // DELETE
                            editingTask = null
                            addButton.text = "Add"
                            taskInput.text.clear()
                        }
                    }
                    .show()
                true
            }

            container.addView(task)
        }

        val startingTasks = listOf("Prepare lunch", "Play tennis")
        for (name in startingTasks) {
            addTask(name)
        }

        addButton.setOnClickListener {
            val text = taskInput.text.toString().trim()

            if (text.isEmpty()) {
                Toast.makeText(this, "Write a task first", Toast.LENGTH_SHORT).show()
            } else if (editingTask != null) {
                // UPDATE
                editingTask!!.text = text
                editingTask = null
                addButton.text = "Add"
                taskInput.text.clear()
            } else {
                // CREATE
                addTask(text)
                taskInput.text.clear()
            }
        }
    }
}