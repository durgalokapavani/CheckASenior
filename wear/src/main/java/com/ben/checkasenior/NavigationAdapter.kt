package com.ben.checkasenior

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.wear.widget.drawer.WearableNavigationDrawerView
import com.ben.checkasenior.home.WatchMainNavActivity

class NavigationAdapter(val context: Context) : WearableNavigationDrawerView.WearableNavigationDrawerAdapter() {


    override fun getItemText(pos: Int): CharSequence {
        return context.getString(WatchMainNavActivity.Section.values()[pos].titleRes);

    }

    override fun getItemDrawable(pos: Int): Drawable {
        return context.getDrawable(WatchMainNavActivity.Section.values()[pos].drawableRes);
    }

    override fun getCount(): Int {
        return WatchMainNavActivity.Section.values().count()
    }

}