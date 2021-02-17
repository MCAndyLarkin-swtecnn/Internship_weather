package com.example.base.PolyVanko

import android.annotation.SuppressLint
import android.content.Context
import android.content.Loader
import android.content.res.Configuration
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.RetrofitClient
import api.model.CurrentWeatherForecast
import api.model.DailyForecast
import api.model.WeatherForecast
import com.example.base.R
import retrofit2.Invocation.of
import retrofit2.Response


//Next will added MT

interface WetherChannel {
    //    var forecast: Response<WeatherForecast>?
    fun fillTempAndHumidity()
    fun getTemp(plus: Int = 0): Short//returns 23 (Expm)
    fun getWetherImg(plus: Int = 0): Int//returns Image
    fun getDateLine(plus: Int = 0): String {//returns "February 16, 2020" (Expm)
        return "Fabruary ${16+plus}, 2020"
    }
}

//For VM
data class MyWether(
    var temp: String = "",
    val hum: String = ""
)

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
    var wetherChannel: WetherChannel = UseAsyncTask()
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
    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        //Bundle is a set of key-val pairs
        // Used with:
//            override fun onSaveInstanceState()
//            and
//            override fun onRestoreInstantState()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when("View mode"){
            "View model" -> {
                //ViewModel Exp

            }
            else -> wetherChannel.fillTempAndHumidity()
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

    inner class UseThread : WetherChannel {
        override fun fillTempAndHumidity() {
            var humaLine: String
            var tempLine: String
            Thread(Runnable {
                val forecast: Response<CurrentWeatherForecast>? = RetrofitClient.getCurrentWeather().execute()
                forecast?.let {
                    tempLine = forecast.body()!!.weather.temp.toString()
                    humaLine = forecast.body()!!.weather.humidity.toString()

                    this@MainActivity.runOnUiThread {
                        (tempLine+"\u00B0").also { findViewById<TextView>(R.id.Temp_val).text = it }
                        findViewById<TextView>(R.id.Huidity_val).text = humaLine+"%"
                    }
                } ?: let {
                    Log.e("Forecast_Thrd", "null")
                }
            }).start()
        }

        //    override var forecast: Response<WeatherForecast>? = null
        override fun getTemp(plus: Int): Short {
//        var forecast: Response<WeatherForecast>?
//        forecast = RetrofitClient.getWeatherForecast().execute()
//        forecast?.let {
//            Log.d("Forecast", forecast!!.message())
//        } ?: let {
//            Log.d("Forecast", "null")
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


//            Log.e("START", "1")
//            Log.e("END", "3")
            return (23 + plus).toShort()
        }

        override fun getWetherImg(plus: Int): Int {
            return R.drawable.rain
        }

        override fun getDateLine(plus: Int): String {
            return super.getDateLine(plus)
        }
    }
    inner class UseHandler : WetherChannel {
        override fun fillTempAndHumidity() {
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
                    Log.e("Forecast_Thrd", "null")
                }
            }).start()
        }
        override fun getTemp(plus: Int): Short {
            return (23 + plus).toShort()
        }

        override fun getWetherImg(plus: Int): Int {
            return R.drawable.rain
        }

        override fun getDateLine(plus: Int): String {
            return super.getDateLine(plus)
        }
    }
    inner class UseAsyncTask : WetherChannel {
        override fun fillTempAndHumidity() {
            var myAsTask = MyAsTask()
            myAsTask.execute()
        }

        override fun getTemp(plus: Int): Short {
            return (23 + plus).toShort()
        }

        override fun getWetherImg(plus: Int): Int {
            return R.drawable.rain
        }

        override fun getDateLine(plus: Int): String {
            return super.getDateLine(plus)
        }

        inner class MyAsTask : AsyncTask<Unit, Unit, Unit>() {
            lateinit var humaLine: String
            lateinit var tempLine: String
            override fun doInBackground(vararg params: Unit?) {
                val forecast: Response<CurrentWeatherForecast>? =
                    RetrofitClient.getCurrentWeather().execute()
                forecast?.let {
                    tempLine = forecast.body()!!.weather.temp.toString()
                    humaLine = forecast.body()!!.weather.humidity.toString()

                } ?: let {
                    Log.e("Forecast_Async", "null")
                }
            }

            override fun onPreExecute() {
                Log.e("Async", "OnPre")
                super.onPreExecute()
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                (tempLine + "\u00B0").also { findViewById<TextView>(R.id.Temp_val).text = it }
                (humaLine + "%").also { findViewById<TextView>(R.id.Huidity_val).text = it }
                Log.e("Async", "OnPost")
            }

        }
    }
    /*
    inner class UseLoader : WetherChannel {
        override fun fillTempAndHumidity() {
            // TODO: 2/17/21
        }

        override fun getTemp(plus: Int): Short {
            return (23 + plus).toShort()
        }

        override fun getWetherImg(plus: Int): Int {
            return R.drawable.rain
        }

        override fun getDateLine(plus: Int): String {
            return super.getDateLine(plus)
        }

        inner class TimeLoader(context: Context?, args: Bundle?) : Loader<String?>(context) {
            val LOG_TAG = "Loader"
            var myAsTask: MyAsTask? = null
            protected override fun onStartLoading() {
                super.onStartLoading()
                Log.d(LOG_TAG, hashCode().toString() + " onStartLoading")
            }

            protected override fun onStopLoading() {
                super.onStopLoading()
                Log.d(LOG_TAG, hashCode().toString() + " onStopLoading")
            }

            protected override fun onForceLoad() {
                super.onForceLoad()
                Log.d(LOG_TAG, hashCode().toString() + " onForceLoad")
                if (myAsTask != null) myAsTask!!.cancel(true)
                myAsTask = MyAsTask()
                myAsTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }

            protected override fun onAbandon() {
                super.onAbandon()
                Log.d(LOG_TAG, hashCode().toString() + " onAbandon")
            }

            protected override fun onReset() {
                super.onReset()
                Log.d(LOG_TAG, hashCode().toString() + " onReset")
            }

            fun getResultFromTask(result: String?) {
                deliverResult(result)
            }

            inner class MyAsTask : AsyncTask<Unit, Unit, Pair<String, String>>() {
                override fun doInBackground(vararg params: Unit?): Pair<String, String>? {
                    val forecast: Response<CurrentWeatherForecast>? =
                            RetrofitClient.getCurrentWeather().execute()
                    forecast?.let {
                        return Pair(forecast.body()!!.weather.temp.toString(),
                            forecast.body()!!.weather.humidity.toString())
                    }
                    Log.e("Forecast_Async", "null")
                    return null
                }

                override fun onPreExecute() {
                    Log.e("Async", "OnPre")
                    super.onPreExecute()
                }

                override fun onPostExecute(result: Pair<String, String>?) {
                    super.onPostExecute(result)
                    findViewById<TextView>(R.id.Temp_val).text = result?.first
                    findViewById<TextView>(R.id.Huidity_val).text = result?.second
                    Log.e("Async", "OnPost")
                }

            }
        }
    }
    */
    /*
    inner class UseMVaLD : WetherChannel {
        inner class MyVM : ViewModel() {

        }
        override fun fillTempAndHumidity() {

//            var viewMod = ViewModelProvider.of(this).get(MyVM::class.java)

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
                    Log.e("Forecast_Thrd", "null")
                }
            }).start()
        }
        override fun getTemp(plus: Int): Short {
            return (23 + plus).toShort()
        }

        override fun getWetherImg(plus: Int): Int {
            return R.drawable.rain
        }

        override fun getDateLine(plus: Int): String {
            return super.getDateLine(plus)
        }
    }
*/

    class MyAdapter :  RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private fun buildForacstData(count: Int = 1): List<Triple<String, Int, Short>> {
//            return mutableListOf(
//                Triple("February 14, 2021", R.drawable.cloudy, 23),
//                Triple("February 15, 2021", R.drawable.rain, 24),
//                Triple("February 16, 2021", R.drawable.partly_cloudy, 25)
//            )
            var forecast: List<DailyForecast>? = null
            val forecast_connection: Response<WeatherForecast>? = RetrofitClient.getWeatherForecast().execute()
            forecast_connection?.let {
                forecast = forecast_connection.body()!!.daily
            } ?: let {
                Log.e("Forecast_Thrd", "null")
            }


            return forecast?.let {
                (0 until count)//it == 0 is today, it == 1 is yesterday etc...
                        .map { i ->
                            Triple(forecast!![i].getDate(),
                                    R.drawable.cloudy,
                                    forecast!![i].temp.max.toInt().toShort()
                            )
                        }
            } ?: let{
                (0 until count)
                        .map{
                            Triple("No Data",R.drawable.nodata,-273)
                        }
            }
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
        //onCreateViewHolder creates holder ant init view for list
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.mem1,
                parent, false
            )
        )//The hard

        //Bind (Svyazivat') View with values
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imgView?.setImageResource(values[position].second)
            holder.txtView?.text = values[position].first
            """${values[position].third}°""".also { holder.gradeView?.text = it }
        }

        override fun getItemCount() = values.size

        ///!!!!
        /// notifyDataSetChanged() - is funk, which call adapter to update data

    }

    var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            (bundle.getString("Temp")+"\u00B0").also { findViewById<TextView>(R.id.Temp_val).text = it }
            (bundle.getString("Hum") + "%").also { findViewById<TextView>(R.id.Huidity_val).text = it }
        }
    }
}

