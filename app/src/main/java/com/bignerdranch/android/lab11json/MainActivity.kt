package com.bignerdranch.android.lab11json

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.room.Room
import com.bignerdranch.android.lab11json.data.DATABASE_NAME
import com.bignerdranch.android.lab11json.data.TasksBD
import com.bignerdranch.android.lab11json.data.models.Priority
import com.bignerdranch.android.lab11json.data.models.Tasks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var nameTask: EditText
    private lateinit var namesTask: EditText
    private lateinit var textTask: EditText
    private lateinit var preoritytTask: EditText
    private lateinit var dateTask: CalendarView
    private lateinit var btnTask: Button
    private lateinit var delTask: Button
    private lateinit var btnTaskInfo: ImageButton
    private lateinit var tvHead: TextView

    private var date : String? = null
    private var priorityUUID : String? = null
    private var uid: UUID? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        nameTask = findViewById(R.id.nameTask)
        namesTask = findViewById(R.id.namesTask)
        textTask = findViewById(R.id.textTask)
        preoritytTask = findViewById(R.id.preorityTask)
        btnTask = findViewById(R.id.buttonTask)
        delTask = findViewById(R.id.delTasks)
        dateTask = findViewById(R.id.calendarView)
        btnTaskInfo = findViewById(R.id.backTaskInfo)
        tvHead = findViewById(R.id.textView3)


        var index = intent.getIntExtra("index",-1)
        var txtuid = intent.getStringExtra("uid")

        Log.d("UUID-TXT",txtuid.toString())

        if(txtuid != null){
            uid = UUID.fromString(txtuid)
        }
        dateTask.setOnDateChangeListener(){view, year, month, dayOfMonth -> date = "$dayOfMonth.${month+1}.$year"}
        val cl = Calendar.getInstance()

        if(index > -1){
            delTask.isVisible = true
            tvHead.text = "Редактирование"
            btnTask.text = "Изменить"
            var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
            val TaskDAO = db.TasksDAO()
            val Tasks = TaskDAO.getTasks(uid!!)
            val priority = TaskDAO.getPreority(uid!!)
            priority.observe(this, androidx.lifecycle.Observer {
                it.forEach {
                    preoritytTask.setText(it.preority)
                    priorityUUID = it.uid.toString()
                }
            })
            Tasks.observe(this, androidx.lifecycle.Observer {
                it.forEach {
                    nameTask.setText(it.nameTask)
                    namesTask.setText(it.creatTask)
                    textTask.setText(it.text)
                    val date = it.dateTask.split(".")
                    cl.set(date?.get(2)!!.toInt(),date[1].toInt()-1,date[0].toInt())
                    dateTask.date = cl.timeInMillis
                }
            })
        }
        else {
            delTask.isVisible = false
        }

        //Отправка
        btnTask.setOnClickListener {
            if(index == -1){
                var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
                val TaskDAO = db.TasksDAO()
                val exec = Executors.newSingleThreadExecutor()
                //Создание приоритета
                var ui = UUID.randomUUID()
                var uid_preor = UUID.randomUUID()
                //Добавление в базу
                exec.execute{
                    TaskDAO.addPreoruty(Priority(uid_preor,ui,preoritytTask.text.toString()))
                    TaskDAO.addTasks(Tasks(ui!!,uid_preor!!,nameTask.text.toString(),namesTask.text.toString(),textTask.text.toString(),date.toString()))
                }
            }
            else
            {
                var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
                val TaskDAO = db.TasksDAO()
                val exec = Executors.newSingleThreadExecutor()
                exec.execute{
                    TaskDAO.saveTasks(Tasks(uid!!,UUID.fromString(priorityUUID),nameTask.text.toString(),namesTask.text.toString(),textTask.text.toString(),date.toString()))
                }
            }
            super.onBackPressed()
        }
        btnTaskInfo.setOnClickListener {
            super.onBackPressed()
        }
        delTask.setOnClickListener(){
            var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
            val TaskDAO = db.TasksDAO()
            val exec = Executors.newSingleThreadExecutor()
            exec.execute{
                TaskDAO.delPreoruty(Priority(UUID.fromString(priorityUUID),uid!!,preoritytTask.text.toString()))
                TaskDAO.delTasks(Tasks(uid!!,UUID.fromString(priorityUUID),nameTask.text.toString(),namesTask.text.toString(),textTask.text.toString(),date.toString()))
            }
            super.onBackPressed()
        }

    }
}