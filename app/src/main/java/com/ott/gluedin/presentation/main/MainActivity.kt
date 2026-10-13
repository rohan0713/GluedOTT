package com.ott.gluedin.presentation.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.ott.gluedin.R
import com.ott.gluedin.databinding.ActivityMainBinding
import com.ott.gluedin.presentation.detail.ui.DetailFragment
import com.ott.gluedin.presentation.home.ui.HomeFragment

class MainActivity : AppCompatActivity(), HomeFragment.NavigationListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, HomeFragment.newInstance())
            }
        }
    }

    override fun onNavigateToDetail(contentId: String) {
        supportFragmentManager.commit {
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            replace(R.id.fragmentContainer, DetailFragment.newInstance(contentId))
            addToBackStack("DetailFragment")
        }
    }
}