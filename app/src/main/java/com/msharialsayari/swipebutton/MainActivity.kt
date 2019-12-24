package com.msharialsayari.swipebutton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mySwipeButton.setSwipeButtonListener(object:SwipeButton.SwipeButtonListener{
            override fun onCompletelySlidingButton(view: SwipeButton) {
                Log.d("MSH Swipe Button" , "onCompletelySlidingButton")
            }

            override fun onStartSwiping(view: SwipeButton) {
                Log.d("MSH Swipe Button" , "onStartSwiping")
            }

            override fun onTouchEndEdge(view: SwipeButton) {
                Log.d("MSH Swipe Button" , "onTouchEndEdge")
            }

        })
    }
}
