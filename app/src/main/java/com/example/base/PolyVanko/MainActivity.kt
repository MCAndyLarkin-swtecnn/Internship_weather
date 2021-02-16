package com.example.base.PolyVanko

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.RetrofitClient
import api.model.CurrentWeatherForecast
import com.example.base.R
import retrofit2.Response


//Next will added MT

interface WetherChannel {
    //    var forecast: Response<WeatherForecast>?
    fun getTemp(plus: Int = 0): Short//returns 23 (Expm)
    fun getWetherImg(plus: Int = 0): Int//returns Image
    fun getDateLine(plus: Int = 0): String {//returns "February 16, 2020" (Expm)
        return "Fabruary ${16+plus}, 2020"
    }
}
object polivEngine{
    const val ZONES = 5
    var count_of_notif = 1
    var activTomorrow: ArrayList<Int> = arrayListOf(
        0, 1, 4
    )
    var activToday: ArrayList<Int> = arrayListOf(
        2, 4
    )
    var brizg: Boolean = true
    var savedPolivPower: MCustomView.CurValue? = null
}

class MainActivity : AppCompatActivity() {
    var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            findViewById<TextView>(R.id.Temp_val).text = bundle.getString("Temp")
            findViewById<TextView>(R.id.Huidity_val).text = bundle.getString("Hum")
        }
    }
    private lateinit var TodayCheck: Array<CheckBox>
    private lateinit var TomorrowCheck: Array<CheckBox>
    private lateinit var RigText: Array<TextView>
    private var myslider: MCustomView? = null

    override fun onPause() {
        myslider?.let {
            polivEngine.savedPolivPower = myslider!!.getVal()
        }
        super.onPause()
    }
    private fun fillCurTempAndHumidity(){
        var humaLine: String
        var tempLine: String
        Thread(Runnable {
            val msg: Message = handler.obtainMessage()
            val bundle = Bundle()
            val forecast: Response<CurrentWeatherForecast>? = RetrofitClient.getCurrentWeather().execute()
            forecast?.let {
                tempLine = forecast.body()!!.weather.temp.toString()
                humaLine = forecast.body()!!.weather.humidity.toString()

                bundle.putString("Temp",tempLine)
                bundle.putString("Hum",humaLine)
                msg.data = bundle
                handler.sendMessage(msg)
            } ?: let {
                Log.e("Forecast", "null")
            }
        }).start()
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fillCurTempAndHumidity()
        setContentView(R.layout.activity_main)

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

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {//RecyclerView exist only in  Landscape
            val recyClouds: RecyclerView = findViewById(R.id.recyCloud)
            recyClouds.layoutManager = LinearLayoutManager(this)
            recyClouds.adapter = MyAdapter()
        } else {                                     //Custom View exist only in Portrait
            myslider = findViewById(R.id.mySlider)
            polivEngine.savedPolivPower?.let {
                myslider!!.setCurrentValue(polivEngine.savedPolivPower!!)
            } ?: let {
                myslider!!.initAttrs()
            }
        }

        val not: TextView = findViewById(R.id.notific)//Orange notification circle
        not.text = polivEngine.count_of_notif.toString()
        if (not.text.toString().toInt() == 0) not.background = getDrawable(R.drawable.nut)
        not.setOnClickListener {
            val count = not.text.toString().toInt()
            if (count > 0) {
                Toast.makeText(this, "Завтра все еще Зима", Toast.LENGTH_LONG).show()
                not.text = (count - 1).toString()
                polivEngine.count_of_notif = count - 1
                if (count == 1) not.background = getDrawable(R.drawable.nut)
            } else
                Toast.makeText(this, "Уведомлений больше нет", Toast.LENGTH_LONG).show()
        }


        val shlang: CheckBox = findViewById(R.id.Shlang)//Checkbox polivaika
        if (polivEngine.brizg) {
            shlang.isChecked = true
            shlang.contentDescription = "Shlungs vodoi brizguet"
        } else {
            shlang.isChecked = false
            shlang.contentDescription = "Shlungs vodoi ne brizguet"
            myslider?.Vikl()
        }
        shlang.setOnClickListener {
            if (shlang.isChecked) {
                polivEngine.brizg = true
                onPrediction()
                myslider?.Vkl()
                for (chB in TomorrowCheck) {
                    chB.isClickable = true
                }
                shlang.contentDescription = "Shlungs vodoi brizguet"
            } else {
                polivEngine.brizg = false
                myslider?.Vikl()
                for (chB in TomorrowCheck) {
                    chB.isChecked = false
                    chB.isClickable = false
                }
                shlang.contentDescription = "Shlungs vodoi ne brizguet"
            }
        }

        for (ch in 0 until polivEngine.ZONES) {
            TomorrowCheck[ch].setOnClickListener {
                if (TomorrowCheck[ch].isChecked) {
                    polivEngine.activTomorrow.add(ch)
                    TomorrowCheck[ch].contentDescription = "Zavtra poliyom tsvetochkee"
                } else {
                    polivEngine.activTomorrow.remove(ch)
                    TomorrowCheck[ch].contentDescription = "Zavtra ne poliyom tsvetochkee"
                }
            }
        }
        onToday()
        onPrediction()
    }

    private fun onPrediction() {
        for (i in polivEngine.activTomorrow) {
            TomorrowCheck[i].isChecked = true
            TomorrowCheck[i].contentDescription = "Zavtra poliyom tsvetochkee"
        }
    }

    private fun onToday() {
        for (i in polivEngine.activToday) {
            setEnable(i)
        }
    }

    private fun setEnable(i: Int) {
        if (i in 0 until polivEngine.ZONES) {
            TodayCheck[i].isChecked = true
            RigText[i].setTextColor(resources.getColor(R.color.text_active_col))
        }
    }
    class MyAdapter() :  RecyclerView.Adapter<MyAdapter.ViewHolder>() {
//        var wetherChannel: WetherChannel = UseThread()
        private fun buildForacstData(count: Int = 1): List<Triple<String, Int, Short>> {
            return mutableListOf(
                Triple("February 14, 2021", R.drawable.cloudy, 23),
                Triple("February 15, 2021", R.drawable.rain, 24),
                Triple("February 16, 2021", R.drawable.partly_cloudy, 25)
            )
//            return (0 until count)//it == 0 is today, it == 1 is yesterday etc...
//                .map { elem ->
//                    Triple(wetherChannel.getDateLine(elem),
//                        wetherChannel.getWetherImg(elem),
//                        wetherChannel.getTemp(elem).also { Log.e("Temperature:", "$it") }
//                    )
//                }
        }

        private var values: List<Triple<String, Int, Short>> = buildForacstData(3)

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(
            itemView ?: throw object : Exception("Pizdec") {}
        ) {
            var imgView: ImageView? = null
            var txtView: TextView? = null
            var gradeView: TextView? = null

            init {
                imgView = itemView?.findViewById(R.id.mem_im)
                txtView = itemView?.findViewById(R.id.mem_text)
                gradeView = itemView?.findViewById(R.id.mem_grd)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.mem1,
                parent, false
            )
        )


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imgView?.setImageResource(values[position].second)
            holder.txtView?.text = values[position].first
            """${values[position].third}°""".also { holder.gradeView?.text = it }
        }

        override fun getItemCount() = values.size

//        inner class UseThread : WetherChannel {
//            //    override var forecast: Response<WeatherForecast>? = null
//            override fun getTemp(plus: Int): Short {
////        var forecast: Response<WeatherForecast>?
////        forecast = RetrofitClient.getWeatherForecast().execute()
////        forecast?.let {
////            Log.d("Forecast", forecast!!.message())
////        } ?: let {
////            Log.d("Forecast", "null")
//                val handler =
//                    @SuppressLint("HandlerLeak")
//                    object : Handler() {
//                        override fun handleMessage(msg: Message?) {
//                            var recyClView: RecyclerView = findViewById(R.id.recyCloud)
//
//                        }
//                    }
//
//                var line = 0

//
////            Log.e("START", "1")
////            Log.e("END", "3")
//                return (23 + plus + line).toShort()
//            }
//
//            override fun getWetherImg(plus: Int): Int {
//                return R.drawable.rain
//            }
//
//            override fun getDateLine(plus: Int): String {
//                return super.getDateLine(plus)
//            }
//
//        }

//        class UseAsyncTask : WetherChannel {
//            override fun getTemp(plus: Int): Short {
//                TODO("Not yet implemented")
//            }
//
//            override fun getWetherImg(plus: Int): Int {
//                TODO("Not yet implemented")
//            }
//
//            override fun getDateLine(plus: Int): String {
//                return super.getDateLine(plus)
//            }
//        }
    }


}

