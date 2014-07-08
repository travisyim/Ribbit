package com.example.travis.ribbit;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Travis on 7/7/2014.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "4Epcnsn9rfH6GZZJw1jq59sExnHg9gEi9yC8E5eC", "FjFRBG49QyAUnXDB7gglLQn11i3gx7vwBpqqxJGX");
    }
}
