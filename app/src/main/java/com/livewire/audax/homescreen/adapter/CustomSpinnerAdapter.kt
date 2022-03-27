package com.livewire.audax.homescreen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.livewire.audax.R
import com.livewire.audax.homescreen.model.SpinnerModel

class CustomSpinnerAdapter(val context : Context, var data : List<SpinnerModel>) :BaseAdapter()  {

    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")

        val view : View
        val vh : ItemHolder

        if (convertView == null){
            view = inflater.inflate(R.layout.drop_down, parent, false)
            vh = ItemHolder(view)
            view?.tag = vh
        }else{
            view = convertView
            vh= view.tag as ItemHolder
        }
        vh.label.text = data.get(position).name
        return view

    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
        return data.size
    }

    override fun getItem(position: Int): Any {
        TODO("Not yet implemented")
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        TODO("Not yet implemented")
        return position.toLong()
    }



    private class ItemHolder(row: View?) {
        val label: TextView

        init {
            label = row?.findViewById(R.id.txt_bike_name) as TextView
        }
    }
}