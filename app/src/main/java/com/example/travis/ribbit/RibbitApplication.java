package com.example.travis.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Travis on 7/7/2014.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "4Epcnsn9rfH6GZZJw1jq59sExnHg9gEi9yC8E5eC", "FjFRBG49QyAUnXDB7gglLQn11i3gx7vwBpqqxJGX");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
    }
}
