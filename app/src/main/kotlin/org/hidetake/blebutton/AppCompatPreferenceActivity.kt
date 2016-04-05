package org.hidetake.blebutton

import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatDelegate
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup

abstract class AppCompatPreferenceActivity : PreferenceActivity() {

    private var appCompatDelegateInstance: AppCompatDelegate? = null
    val appCompatDelegate: AppCompatDelegate
        get() = when (appCompatDelegateInstance) {
            null -> {
                appCompatDelegateInstance = AppCompatDelegate.create(this, null)
                appCompatDelegateInstance!!
            }
            else -> appCompatDelegateInstance!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        appCompatDelegate.installViewFactory()
        appCompatDelegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        appCompatDelegate.onPostCreate(savedInstanceState)
    }

    override fun getMenuInflater(): MenuInflater {
        return appCompatDelegate.menuInflater
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        appCompatDelegate.setContentView(layoutResID)
    }

    override fun setContentView(view: View) {
        appCompatDelegate.setContentView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        appCompatDelegate.setContentView(view, params)
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        appCompatDelegate.addContentView(view, params)
    }

    override fun onPostResume() {
        super.onPostResume()
        appCompatDelegate.onPostResume()
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        appCompatDelegate.setTitle(title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        appCompatDelegate.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        super.onStop()
        appCompatDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        appCompatDelegate.onDestroy()
    }

    override fun invalidateOptionsMenu() {
        appCompatDelegate.invalidateOptionsMenu()
    }

}
