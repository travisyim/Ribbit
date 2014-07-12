package com.example.travis.ribbit.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.travis.ribbit.R;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class ImageViewerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Uri fileUri = getIntent().getData();

        Picasso.with(this).load(fileUri).into(imageView);

        // Start the timer
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 10*1000);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // Invoke the equivalent of a back button press by telling the activity to finish instead of starting a new parent activity
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}