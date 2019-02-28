package com.bornfight.roundedtabbar.utils

import android.content.res.Resources

internal val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
