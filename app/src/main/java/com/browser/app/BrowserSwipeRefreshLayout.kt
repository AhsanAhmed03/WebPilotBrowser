package com.browser.app

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class BrowserSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    var activeWebView: WebView? = null

    override fun canChildScrollUp(): Boolean {
        val wv = activeWebView
        return if (wv != null) {
            wv.scrollY > 4
        } else {
            super.canChildScrollUp()
        }
    }
}
