package com.example.base

import android.app.ListActivity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Config
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

object polivEngine{
    val ZONES = 5
    var count_of_notif = 1
    var activTomorrow: ArrayList <Int> = arrayListOf(
            0,1,4
    )
    var activToday: ArrayList <Int> = arrayListOf(
        2,4
    )
    var brizg: Boolean = true
}
class MainActivity : AppCompatActivity() {

    lateinit var TodayCheck: Array<CheckBox>
    lateinit var TomorrowCheck: Array<CheckBox>
    lateinit var RigText: Array<TextView>
    fun fakeVals() : List<Triple<String,Int, Short>>{
        return mutableListOf(Triple("February 14, 2021", R.drawable.cloudy, 23),
                Triple("February 15, 2021", R.drawable.rain, 24),
                Triple("February 16, 2021", R.drawable.partly_cloudy, 25)
        )
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("Main", "Created")
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            var recyClouds: RecyclerView = findViewById(R.id.recyCloud)
            recyClouds.layoutManager = LinearLayoutManager(this)
            recyClouds.adapter = MyAdapter(fakeVals())
        }


        var not: TextView = findViewById(R.id.notific)
        not.text = polivEngine.count_of_notif.toString()
        if(not.text.toString().toInt() == 0) not.background = getDrawable(R.drawable.nut)
        not.setOnClickListener {
            val count = not.text.toString().toInt()
            if (count > 0){
                Toast.makeText(this,"Завтра все еще Зима",Toast.LENGTH_SHORT).show()
                not.text = (count-1).toString()
                polivEngine.count_of_notif = count-1
                if (count == 1) not.background = getDrawable(R.drawable.nut)
            } else
                Toast.makeText(this,"Уведомлений больше нет",Toast.LENGTH_SHORT).show()
        }
        TodayCheck = arrayOf(
            findViewById(R.id.rightCheck1),
            findViewById(R.id.rightCheck2),
            findViewById(R.id.rightCheck3),
            findViewById(R.id.rightCheck4),
            findViewById(R.id.rightCheck5)
        )
        TomorrowCheck = arrayOf(
            findViewById(R.id.leftCheck1),
            findViewById(R.id.leftCheck2),
            findViewById(R.id.leftCheck3),
            findViewById(R.id.leftCheck4),
            findViewById(R.id.leftCheck5)
        )
        RigText = arrayOf(
                findViewById(R.id.rig_text1),
                findViewById(R.id.rig_text2),
                findViewById(R.id.rig_text3),
                findViewById(R.id.rig_text4),
                findViewById(R.id.rig_text5),
        )
        val s:CheckBox = findViewById(R.id.leftCheck1)
        val shlang: CheckBox = findViewById(R.id.Shlang)
        if(polivEngine.brizg){
            shlang.isChecked = true
            shlang.contentDescription = "Shlungs vodoi brizguet"
        } else{
            shlang.isChecked = false
            shlang.contentDescription = "Shlungs vodoi ne brizguet"
        }
        shlang.setOnClickListener{
            if (shlang.isChecked){
                polivEngine.brizg = true
                onPrediction()
                shlang.contentDescription = "Shlungs vodoi brizguet"
            }
            else {
                polivEngine.brizg = false
                for (chB in TomorrowCheck) chB.isChecked = false
                shlang.contentDescription = "Shlungs vodoi ne brizguet"
            }
        }
        for (ch in 0..(polivEngine.ZONES-1)){
            TomorrowCheck[ch].setOnClickListener {
                if (TomorrowCheck[ch].isChecked){
                    polivEngine.activTomorrow.add(ch)
                    TomorrowCheck[ch].contentDescription = "Zavtra poliyom tsvetochkee"
                }
                else {
                    polivEngine.activTomorrow.remove(ch)
                    TomorrowCheck[ch].contentDescription = "Zavtra ne poliyom tsvetochkee"
                }
            }
        }
        onToday()
        onPrediction()
    }

    fun onPrediction(){
        for (i in polivEngine.activTomorrow){
            TomorrowCheck[i].isChecked = true
            TomorrowCheck[i].contentDescription = "Zavtra poliyom tsvetochkee"
        }
    }

    fun onToday(){
        for (i in polivEngine.activToday){
            setEnable(i)
        }
    }
    fun setEnable(i: Int){
        if(i in 0..(polivEngine.ZONES-1)){
            TodayCheck[i].isChecked = true
            RigText[i].setTextColor(getResources().getColor(R.color.text_active_col))
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Toast.makeText(applicationContext, "Anything",Toast.LENGTH_SHORT)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(applicationContext, "Portrat((",Toast.LENGTH_SHORT)
            Log.d("Ori", "pORT")
        }else
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(applicationContext, "LAND))",Toast.LENGTH_SHORT)
            Log.d("Ori", "Land")
        }
    }
    class MyAdapter(private var values : List<Triple<String, Int, Short>>): RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(
                itemView?:throw object : Exception("Pizdec"){}
        ){
            var imgView: ImageView? = null
            var txtView: TextView? = null
            var gradeView: TextView? = null
            init{
                imgView = itemView?.findViewById(R.id.mem_im)
                txtView = itemView?.findViewById(R.id.mem_text)
                gradeView = itemView?.findViewById(R.id.mem_grd)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mem1,
                parent,false))


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imgView?.setImageResource(values[position].second)
            holder.txtView?.text = values[position].first
            holder.gradeView?.text = "${values[position].third}\u00B0"
        }

        override fun getItemCount() = values.size
    }
}

