package com.bornfight.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bornfight.utils.adapters.GenericAdapter.GenericViewHolder
import java.util.*

/**
 * Created by ianic on 16/02/2018.

 * This is a generic adapter class made for [RecyclerView].
 * It offers general functions to work with collections
 *  ([setItems], [addItems], [getItems], [getItem], [getItemPosition]).
 *
 * When extending, you just have to implement [getViewHolder] and [getLayoutId].
 * Also, you will need to set a type T for the data which this adapter will hold,
 * and implement a [GenericViewHolder] (for [onCreateViewHolder]).
 */
abstract class GenericAdapter<T> : RecyclerView.Adapter<GenericAdapter.GenericViewHolder<T>>() {

    protected var listItems: MutableList<T> = ArrayList()

    open fun setItems(listItems: List<T>) {
        this.listItems.clear()
        this.listItems.addAll(listItems)
        notifyDataSetChanged()
    }

    open fun addItems(listItems: List<T>) {
        val index = this.listItems.size
        this.listItems.addAll(listItems)
        notifyItemRangeInserted(index, listItems.size)
    }

    open fun addItem(listItem: T) {
        listItems.add(listItem)
        val index = listItems.indexOf(listItem)
        notifyItemInserted(index)
    }

    fun getItems(): List<T> {
        return listItems
    }

    fun getItem(position: Int): T {
        return listItems[position]
    }

    fun getItemPosition(item: T): Int {
        return listItems.indexOf(item)
    }

    fun clearItems() {
        listItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        return getViewHolder(
            LayoutInflater.from(parent.context).inflate(getLayoutId(viewType), parent, false),
            viewType
        )
    }

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        (holder).bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    protected abstract fun getLayoutId(viewType: Int): Int

    protected abstract fun getViewHolder(view: View, viewType: Int): GenericViewHolder<T>

    abstract class GenericViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(data: T)
    }
}