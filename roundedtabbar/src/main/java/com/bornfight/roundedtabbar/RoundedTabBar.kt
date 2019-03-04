package com.bornfight.roundedtabbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bornfight.roundedtabbar.utils.HorizontalOffsetDecoration
import com.bornfight.roundedtabbar.utils.dp
import com.bornfight.utils.adapters.GenericAdapter
import kotlinx.android.synthetic.main.custom_tab_bar_item.view.*
import kotlinx.android.synthetic.main.rounded_tab_bar_view.view.*

/**
 * Custom view for creating rounded tab bar, like the one in locations/events fragment in Zagreb be there.
 *
 * To pass your own generic type to the view, you must create another class extending this one, accepting
 * context, attrs and defStyleAttr in the constructor and passing them to RoundedTabBar. You can then set your
 * custom type as the generic for this class.
 *
 * There are several properties possible to define using XML (attrs):
 * @attr rtb_background_color     ->  Tab bar background color,
 * @attr rtb_text_color           ->  Tab bar text color,
 * @attr rtb_corner_radius        ->  Tab bar corner radius,
 * @attr rtb_indicator_drawable   ->  Drawable resource for the indicator.
 */
abstract class RoundedTabBar<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    RoundedTabBarAdapter.OnRoundedTabBarItemInteractionListener<T> {

    private var adapter = RoundedTabBarAdapter<T>()
    var listener: OnRoundedTabBarItemInteractionListener<T>? = null

    private var selectedItemPosition: Int = 0
    private var viewWidth: Int = 0

    private var tabBackgroundColor: Int = 0
    private var tabTextColor: Int = 0
    private var tabCornerRadius: Float = 0f
    private var tabIndicatorDrawable: Int = R.drawable.category_indicator
    private var textAllCaps: Boolean = true

    init {
        View.inflate(context, R.layout.rounded_tab_bar_view, this)

        val rtbData = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RoundedTabBar,
            0, 0
        )

        val defaultCornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics)

        try {
            tabBackgroundColor = rtbData.getColor(
                R.styleable.RoundedTabBar_rtb_background_color,
                Color.BLACK
            )
            tabTextColor = rtbData.getColor(
                R.styleable.RoundedTabBar_rtb_text_color,
                Color.WHITE
            )
            tabCornerRadius = rtbData.getDimension(R.styleable.RoundedTabBar_rtb_corner_radius, defaultCornerRadius)
            tabIndicatorDrawable = rtbData.getResourceId(
                R.styleable.RoundedTabBar_rtb_indicator_drawable,
                R.drawable.category_indicator
            )
            textAllCaps = rtbData.getBoolean(R.styleable.RoundedTabBar_android_textAllCaps, true)
        } catch (ex: RuntimeException) {
            Log.d("exception", ex.toString())
        } finally {
            rtbData.recycle()
        }

        rtb_cardLayout.setCardBackgroundColor(tabBackgroundColor)
        rtb_cardLayout.radius = tabCornerRadius

        rtb_itemIndicator.setBackgroundResource(tabIndicatorDrawable)

        adapter.itemsTextColor = tabTextColor
        adapter.textAllCaps = textAllCaps
        adapter.listener = this

        rtb_recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rtb_recyclerView.adapter = adapter
        rtb_recyclerView.isNestedScrollingEnabled = false
        rtb_recyclerView.isMotionEventSplittingEnabled = false
        rtb_recyclerView.addItemDecoration(HorizontalOffsetDecoration(context, 8f))

        rtb_scrollView.smoothScrollTo(0, 0)

        if (isInEditMode) setEditModeItems()
    }

    private fun setEditModeItems() {
        val tabItems = listOf("Zagreb", "London", "New York", "Tokyo", "Sydney")

        setItems(tabItems as List<T>) { item, holder ->
            holder.tabNameView.text = item.toString()
        }

        // Add 8dp to compensate padding start
        rtb_itemIndicator.x = x + 20f.dp
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
    }

    /**
     * @param items The list of items you want to put in the tab bar.
     * @param binder Lambda you must use to define view binding (i.e. text you want your tab bar items to display)
     */
    fun setItems(items: List<T>, binder: (data: T, holder: RoundedTabBarAdapter<T>.RoundedTabViewHolder<T>) -> Unit) {
        adapter.setItemsWithBinder(items, binder)
        setRoundedTabBarIndicator()
    }

    /**
     * Must be called after the items have been set. Otherwise, it has no effect.
     * Sets tab bar indicator to the position of currently selected item. Adjusts its width as well.
     *
     * This function is called with setItems() so you don't have to set it manually when loading items.
     */
    private fun setRoundedTabBarIndicator() {
        if (adapter.itemCount == 0) return

        rtb_recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rtb_recyclerView?.findViewHolderForAdapterPosition(selectedItemPosition)?.itemView?.let {
                    // When categories are first loaded, set indicator bellow selected item.
                    val layoutParams: ViewGroup.LayoutParams = rtb_itemIndicator.layoutParams
                    layoutParams.width = it.width
                    rtb_itemIndicator.layoutParams = layoutParams
                    // Add 8dp to compensate padding start
                    rtb_itemIndicator.x = it.x + 8f.dp
                }
                rtb_recyclerView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onItemClicked(item: T, itemPositionOnAxisX: Float, itemWidth: Int, adapterPosition: Int) {
        selectedItemPosition = adapterPosition

        val animDuration: Long = 200
        // Expand/reduce indicator width
        val animWidth: ValueAnimator = ValueAnimator.ofInt(rtb_itemIndicator.width, itemWidth)
        animWidth.addUpdateListener { valueAnimator ->
            val layoutParams: ViewGroup.LayoutParams = rtb_itemIndicator.layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            rtb_itemIndicator.layoutParams = layoutParams
        }
        animWidth.duration = animDuration
        animWidth.start()

        // Animate category indicator position change
        rtb_itemIndicator.animate().apply {
            // Add 8dp to compensate start padding.
            x(itemPositionOnAxisX + 8f.dp)
            duration = animDuration
        }.start()

        // Place the category item in the middle of the screen if possible. If not, bring it
        // closest possible to the center.
        Handler().postDelayed({
            val scrollX = rtb_itemIndicator.x - (viewWidth * .5f) + (itemWidth * .63)
            rtb_scrollView.smoothScrollTo(scrollX.toInt(), 0)
        }, animDuration)

        listener?.onRoundedTabBarItemClicked(item)
    }

    interface OnRoundedTabBarItemInteractionListener<T> {
        fun onRoundedTabBarItemClicked(item: T)
    }
}


class RoundedTabBarAdapter<T> : GenericAdapter<T>() {
    var listener: OnRoundedTabBarItemInteractionListener<T>? = null
    var textAllCaps: Boolean = true
    private lateinit var binder: (data: T, holder: RoundedTabViewHolder<T>) -> Unit
    @ColorRes
    var itemsTextColor: Int = 0

    fun setItemsWithBinder(listItems: List<T>, binder: (data: T, holder: RoundedTabViewHolder<T>) -> Unit) {
        this.binder = binder
        super.setItems(listItems)
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.custom_tab_bar_item
    }

    override fun getViewHolder(view: View, viewType: Int): GenericViewHolder<T> {
        return RoundedTabViewHolder(view, binder).apply {
            itemView.setOnClickListener {

                setIsRecyclable(false)

                listener?.onItemClicked(
                    listItems[adapterPosition],
                    itemView.x,
                    this.itemView.width,
                    adapterPosition
                )
            }
        }
    }

    inner class RoundedTabViewHolder<T>(
        itemView: View,
        private val binder: (data: T, holder: RoundedTabViewHolder<T>) -> Unit
    ) : GenericAdapter.GenericViewHolder<T>(itemView) {

        var tabNameView: TextView = itemView.tabName

        init {
            tabNameView.setTextColor(itemsTextColor)
            tabNameView.isAllCaps = textAllCaps
        }

        override fun bind(data: T) {
            binder(data, this)
        }
    }

    interface OnRoundedTabBarItemInteractionListener<T> {
        fun onItemClicked(item: T, itemPositionOnAxisX: Float, itemWidth: Int, adapterPosition: Int)
    }
}