package com.bornfight.roundedtabbarlayoutlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bornfight.roundedtabbar.RoundedTabBar
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        val tabItems = listOf(
            TabItem(0, "Zagreb"),
            TabItem(1, "London"),
            TabItem(2, "New York"),
            TabItem(3, "Tokyo"),
            TabItem(4, "Sydney")
        )

        tabBar1.setItems(tabItems) { item, holder ->
            holder.tabNameView.text = item.title
        }

        tabBar2.setItems(tabItems) { item, holder ->
            holder.tabNameView.text = "${item.id} ${item.title}"
        }

        tabBar3.setItems(tabItems) { item, holder ->
            holder.tabNameView.text = "${holder.adapterPosition + 1} ${item.title}"
        }

        val listener = object : RoundedTabBar.OnRoundedTabBarItemInteractionListener<TabItem> {
            override fun onRoundedTabBarItemClicked(item: TabItem) {
                Toast.makeText(this@DemoActivity, "Tab " + item.title + " clicked!", Toast.LENGTH_SHORT).show()
            }
        }

        tabBar1.listener = listener
        tabBar2.listener = listener
        tabBar3.listener = listener
    }
}
