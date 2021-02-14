//package com.example.base
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Adapter
//import android.widget.AdapterView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import java.lang.Exception
//
//class MyAdapter(private var values : List<String>): RecyclerView.Adapter<MyAdapter.ViewHolder>() {
//    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(
//        itemView?:throw object : Exception("Pizdec"){}
//    ){
//        var textView: TextView? = null
//        init{
//            textView = itemView?.findViewById(R.id.mem1)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
//        = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mem1,
//                parent,false))
//
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.textView?.text = values[position]
//    }
//
//    override fun getItemCount() = values.size
//}