package com.example.base.PolyVanko

import android.annotation.SuppressLint
import android.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.RetrofitClient
import api.model.*
import com.example.base.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Response
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable


//Next will added MT
interface  SetRetroDataToUI{
    fun fillData(retroDate: RetroDate)
}
interface  GetDataFromRetro{
    fun getForcast(): Call<WeatherForecast>
    fun getCurrent(): Call<CurrentWeatherForecast>
}
//About Threads
class WeatherForcastThread(var callBacks: Pair<SetRetroDataToUI,GetDataFromRetro>, var mode: WETHER.Mode) : Runnable{

    var current: RetroDate? = null

    override fun run() {
        try{
            current = when(mode){
                WETHER.Mode.CURRENT -> callBacks.second.getCurrent().execute().body()
                WETHER.Mode.FORWEEK -> callBacks.second.getForcast().execute().body()
            }
        }catch (ex: Exception){
            Log.e("Exception.Thread", ex.message!!)
        }
        current?.let{this.callBacks.first.fillData(it)}
    }

}

//About Hendler


//About Async
class WeatherForcastAsync(
        var callBacks: Pair<SetRetroDataToUI,GetDataFromRetro>,
        var mode: WETHER.Mode)
    : AsyncTask<Unit, Unit, RetroDate>(){
    override fun doInBackground(vararg params: Unit?): RetroDate? =
        when(mode){
            WETHER.Mode.CURRENT -> callBacks.second.getCurrent().execute().body()
            WETHER.Mode.FORWEEK -> callBacks.second.getForcast().execute().body()

    }
    override fun onPostExecute(result: RetroDate?) {
        result?.let{callBacks.first.fillData(it)}
        super.onPostExecute(result)
    }
}

//About VM
class MyVM() : ViewModel(){
    private var forcast: MutableLiveData<Single<RetroDate?>> = MutableLiveData()
    private var current: MutableLiveData<Single<RetroDate?>> = MutableLiveData()

    fun updateForecast() {
        try {
            val single_Forecast: Single<RetroDate?> = Single.create {
                it.onSuccess(
                    RetrofitClient.getWeatherForecast().execute().body()
                )
            }
            forcast.value = single_Forecast
        }catch (ex: Exception){
            Log.e("Exception.MV", ex.message!!)
        }
    }
    fun updateCurrent() {
        try {
            val single_Current: Single<RetroDate?> = Single.create {
                it.onSuccess(
                    RetrofitClient.getCurrentWeather().execute().body()
                )
            }
            current.value = single_Current
        }catch (ex: Exception){
            Log.e("Exception.MV", ex.message!!)
        }
    }
    fun getForecast() = forcast
    fun getCurrent() = current

}

//About Loader
class TimeLoader(context: Context?, args: Bundle?) : Loader<RetroDate?>(context) {
    val LOG_TAG = "Loader"
    var weatherAsync: WeatherAsync? = null

    override fun onForceLoad() {//Loader job
        super.onForceLoad()
        Log.e(LOG_TAG, hashCode().toString() + " onForceLoad")
        if (weatherAsync != null) weatherAsync!!.cancel(true)
        WeatherAsync(WETHER.callPair,WETHER.Mode.FORWEEK)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) as WeatherAsync
        WeatherAsync(WETHER.callPair,WETHER.Mode.CURRENT)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) as WeatherAsync
    }

    fun getResultFromTask(result: RetroDate?) {
        deliverResult(result)
    }

    inner class WeatherAsync(
        var callBacks: Pair<SetRetroDataToUI,GetDataFromRetro>,
        var mode: WETHER.Mode)
        : AsyncTask<Unit, Unit?, RetroDate?>() {
        override fun doInBackground(vararg params: Unit?): RetroDate? =
            when(mode){
                WETHER.Mode.CURRENT -> callBacks.second.getCurrent().execute().body()
                WETHER.Mode.FORWEEK -> callBacks.second.getForcast().execute().body()

            }
        override fun onPostExecute(result: RetroDate?) {
            result?.let{callBacks.first.fillData(it)}
            super.onPostExecute(result)
            getResultFromTask(result)
        }
    }

    companion object {//May contain constants
    }

    init {
        //Read and set bundle data  (make default)
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
object WETHER{
    enum class Mode{
        CURRENT,
        FORWEEK
    }
    lateinit var Humidity: String
    lateinit var Temperature: String
    lateinit var forecastList : List<Triple<String, Int, Short>>
    val FORCAST_LENGTH = 3
    lateinit var callPair : Pair<SetRetroDataToUI, GetDataFromRetro>

    fun setCurrent(forecast: CurrentWeatherForecast){
        Temperature = forecast.weather.temp.toString()
        Humidity = forecast.weather.humidity.toString()
    }
    fun setForecast(forecast: WeatherForecast, count : Int) {
        forecastList = (0 until count)//it == 0 is today, it == 1 is yesterday etc...
                .map { i ->
                    Triple(forecast.daily[i].getDate(),
                            R.drawable.cloudy,
                            forecast.daily[i].temp.max.toInt().toShort()
                    )
                }
    }
}
class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<RetroDate?>  {
    val scope = CoroutineScope(Job())
//    lateinit var myMV: MyVM
    val LOG_TAG = "Loader"
    val LOADER_ID = 1
    private val myMV by lazy { ViewModelProviders.of(this).get(MyVM::class.java)}
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



        TodayCheck = arrayOf(
            rightCheck1,
            rightCheck2,
            rightCheck3,
            rightCheck4,
            rightCheck5
        )
        TomorrowCheck = arrayOf(
            leftCheck1,
            leftCheck2,
            leftCheck3,
            leftCheck3,
            leftCheck5
        )
        RigText = arrayOf(
            rig_text1,
            rig_text2,
            rig_text3,
            rig_text4,
            rig_text5
        )

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Custom View exist only in Portrait
            myslider = findViewById(R.id.mySlider)
            polivEngine.savedPolivPower?.let {
                myslider!!.setCurrentValue(polivEngine.savedPolivPower!!)
            } ?: let {
                myslider!!.initAttrs()
            }
        }

        var multyThreadingMethod = MultyThreadingMethod.COROUTINES
        retroInit(multyThreadingMethod)
        updateBut.setOnClickListener{
            retroInit(multyThreadingMethod)
        }

        //Orange notification circle
        notific.text = polivEngine.count_of_notif.toString()
        if (notific.text.toString().toInt() == 0) notific.background = getDrawable(R.drawable.nut)
        notific.setOnClickListener {
            val count = notific.text.toString().toInt()
            if (count > 0) {
                Toast.makeText(this, "Завтра все еще Зима", Toast.LENGTH_LONG).show()
                notific.text = (count - 1).toString()
                polivEngine.count_of_notif = count - 1
                if (count == 1) notific.background = getDrawable(R.drawable.nut)
            } else
                Toast.makeText(this, "Уведомлений больше нет", Toast.LENGTH_LONG).show()
        }

        //Checkbox polivaika
        if (polivEngine.brizg) {
            Shlang.isChecked = true
            Shlang.contentDescription = "Shlungs vodoi brizguet"
        } else {
            Shlang.isChecked = false
            Shlang.contentDescription = "Shlungs vodoi ne brizguet"
            myslider?.Vikl()
        }
        Shlang.setOnClickListener {
            if (Shlang.isChecked) {
                polivEngine.brizg = true
                onPrediction()
                myslider?.Vkl()
                for (chB in TomorrowCheck) {
                    chB.isClickable = true
                }
                Shlang.contentDescription = "Shlungs vodoi brizguet"
            } else {
                polivEngine.brizg = false
                myslider?.Vikl()
                for (chB in TomorrowCheck) {
                    chB.isChecked = false
                    chB.isClickable = false
                }
                Shlang.contentDescription = "Shlungs vodoi ne brizguet"
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

    inner class UseHandler{
        fun fillTempAndHumidity() {
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
    }

    class MyAdapter(var values: List<Triple<String, Int, Short>>) :  RecyclerView.Adapter<MyAdapter.ViewHolder>() {

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

    private fun updateWether() {
        findViewById<TextView>(R.id.Huidity_val).text = WETHER.Humidity
        findViewById<TextView>(R.id.Temp_val).text = WETHER.Temperature
    }
    private fun updateForecast() =
            recucleViewInit()?.let{it.adapter = MyAdapter(WETHER.forecastList)}

    fun recucleViewInit() : RecyclerView?{
        val recyCler: RecyclerView
        try {
            recyCler = findViewById(R.id.recyCloud)//RecyclerView exist only in  Landscape
            recyCler.layoutManager = LinearLayoutManager(this)
        }catch (ex: IllegalArgumentException){
            Log.e("Recycler-layoutManager", ex.message!!)
            return null
        }catch (ex: java.lang.Exception){
            Log.e("Recycler", ex.message!!)
            return null
        }
        return recyCler
    }
    fun retroInit(mode: MultyThreadingMethod) {
        val callBacks: Pair<SetRetroDataToUI, GetDataFromRetro> = Pair(
            //Two callbacks: 1.What to do to data? 2.Where get the data?
            object : SetRetroDataToUI {
                override fun fillData(retroDate: RetroDate) {
                    (retroDate as? CurrentWeatherForecast)?.let {
                        WETHER.setCurrent(retroDate)
                        runOnUiThread(::updateWether)
                    } ?: (retroDate as? WeatherForecast)?.let {
                        WETHER.setForecast(retroDate, WETHER.FORCAST_LENGTH)
                        runOnUiThread(::updateForecast)
                    }
                }
            },
            object : GetDataFromRetro {

                override fun getCurrent(): Call<CurrentWeatherForecast> =
                    RetrofitClient.getCurrentWeather()


                override fun getForcast(): Call<WeatherForecast> =
                    RetrofitClient.getWeatherForecast()

            }
        )

        fun execRx(single: Single<RetroDate?>) {
            single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({//onSuccess
                    it?.let {
                        callBacks.first.fillData(it)
                    }
                }, {//onError
                    Log.e("SUBSCRIBE", "Error")
                })
        }

        when (mode) {
            MultyThreadingMethod.THREADS -> {
                Log.e("MultiThreadingTest", "Threds")
                Thread(WeatherForcastThread(callBacks, WETHER.Mode.CURRENT)).start()
                Thread(WeatherForcastThread(callBacks, WETHER.Mode.FORWEEK)).start()
            }
            MultyThreadingMethod.ASSYNK -> {
                Log.e("MultiThreadingTest", "Async")
                WeatherForcastAsync(callBacks, WETHER.Mode.CURRENT).execute()
                WeatherForcastAsync(callBacks, WETHER.Mode.FORWEEK).execute()
            }
            MultyThreadingMethod.RX -> {//Without Livedata & ModelView (I don't understand
                // why I need their (in current case))
                Log.e("MultiThreadingTest", "RX")

                val single_Current: Single<RetroDate?> = Single.create { subscriber ->
                    val body = callBacks.second.getCurrent().execute().body()
                    body?.let {
                        subscriber.onSuccess(body)
                    }
                }
                execRx(single_Current)

                val single_Forecast: Single<RetroDate?> = Single.create { subscriber ->
                    val body = callBacks.second.getForcast().execute().body()
                    body?.let {
                        subscriber.onSuccess(body)
                    }
                }
                execRx(single_Forecast)

            }
            MultyThreadingMethod.VIEWMODEL_LIVEDATA_RX -> {//With Livedata & ModelView & little RX
                Log.e("MultiThreadingTest", "VM & LD")
                myMV.getForecast().observe(this, Observer {
                    execRx(it)
                })
                myMV.getCurrent().observe(this, Observer {
                    execRx(it)
                })
                myMV.updateCurrent()
                myMV.updateForecast()
            }
            MultyThreadingMethod.LOADER -> {//With Livedata & ModelView & little RX
                Log.e("MultiThreadingTest", "Loader")
                WETHER.callPair = callBacks
                loaderManager.initLoader(LOADER_ID, Bundle(), this).forceLoad()
            }
            MultyThreadingMethod.COROUTINES -> {//With Livedata & ModelView & little RX
                Log.e("MultiThreadingTest", "Coroutines")
                scope.launch {
                    val current = scope.async {
                        callBacks.second.getCurrent().execute().body()
                    }.await()
                    val forecast = scope.async {
                        callBacks.second.getForcast().execute().body()
                    }.await()
                    current?.let { callBacks.first.fillData(current)}
                    forecast?.let { callBacks.first.fillData(forecast)}
                }
            }
            else -> {
                defaultWetherInit()
            }
        }
    }

    private fun defaultWetherInit() {
        TODO("Not yet implemented")
    }
    enum class MultyThreadingMethod{
        THREADS,
        ASSYNK,
        HANDLER,
        LOADER,
        THREAD_POOL_EXECUTOR,
        RX,
        VIEWMODEL_LIVEDATA_RX,
        COROUTINES
    }
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<RetroDate?>? {
        var loader: Loader<RetroDate?>? = null
        if (id == LOADER_ID) {
            loader = TimeLoader(this, args)//Custom constructor
            Log.e(LOG_TAG, "onCreateLoader: " )
        }
        return loader
    }

    override fun onLoadFinished(loader: Loader<RetroDate?>?, data: RetroDate?) {
        //UI Work
        data?.let {
            WETHER.callPair.first.fillData(data)
        }
    }

    override fun onLoaderReset(loader: Loader<RetroDate?>) {}
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}



/*
    *Test Async in Profiler
 */

//Thread нужно закрывать?