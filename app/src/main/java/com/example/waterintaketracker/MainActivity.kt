package com.example.waterintaketracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Hosts the two fragments and switches between them with a BottomNavigationView. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_log -> showFragment(LogFragment(), R.string.title_log)
                R.id.nav_dashboard -> showFragment(DashboardFragment(), R.string.title_dashboard)
            }
            true
        }

        // Ignore taps on the tab that's already open.
        bottomNav.setOnItemReselectedListener { }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_log
        }
    }

    private fun showFragment(fragment: Fragment, titleRes: Int) {
        supportActionBar?.setTitle(titleRes)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
