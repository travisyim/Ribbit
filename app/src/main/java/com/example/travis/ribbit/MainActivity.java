package com.example.travis.ribbit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements ActionBar.TabListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    protected Uri mUri;

    protected static final int TAKE_PHOTO_REQUEST = 0;
    protected static final int TAKE_VIDEO_REQUEST = 1;
    protected static final int PICK_PHOTO_REQUEST = 2;
    protected static final int PICK_VIDEO_REQUEST = 3;
    protected static final int MEDIA_TYPE_IMAGE = 4;
    protected static final int MEDIA_TYPE_VIDEO = 5;
    protected static final int FILE_SIZE_MAX = 1024*1024*10;
    
    protected DialogInterface.OnClickListener mOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case 0: // Take Photo
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mUri = getUri(MEDIA_TYPE_IMAGE);

                    if (mUri == null) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_external_storage), Toast.LENGTH_LONG).show();
                    }
                    else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }

                    break;
                case 1: // Take Video
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mUri = getUri(MEDIA_TYPE_VIDEO);

                    if (mUri == null) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_external_storage), Toast.LENGTH_LONG).show();
                    }
                    else {
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10); // Limit does not appear to work
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 0 for low res - does not appear to work
                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                    }

                    break;
                case 2: // Choose Picture
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickPhotoIntent.setType("image/*");
                    startActivityForResult(pickPhotoIntent, PICK_PHOTO_REQUEST);
                    break;
                case 3: // Choose Video
                    Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickVideoIntent.setType("video/*");
                    Toast.makeText(MainActivity.this, "Video must be less than 10MB.", Toast.LENGTH_LONG).show();
                    startActivityForResult(pickVideoIntent, PICK_VIDEO_REQUEST);
                    break;
            }
        }

        private Uri getUri(int mediaType) {
            // Check to see if external storage is mounted
            if (isExternalStorageMounted()) {
                // Storage device accessible

                // Connect to the storage device
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_PICTURES), MainActivity.this.getString(R.string.app_name));

                // Check to see if the directory exists
                if (! mediaStorageDir.exists()) {
                    if (! mediaStorageDir.mkdirs()) {
                        Log.e(TAG, getString(R.string.error_failed_directory));
                        return null;
                    }
                }

                // Create the file
                File mediaFile;
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

                String path = mediaStorageDir.getPath() + File.separator;

                if (mediaType == MEDIA_TYPE_IMAGE) {
                    mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                }
                else if (mediaType == MEDIA_TYPE_VIDEO) {
                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                }
                else {
                    return null;
                }

                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

                return Uri.fromFile(mediaFile);
            }
            else {
                // Storage device not accessible
                return null;
            }
        }

        private boolean isExternalStorageMounted() {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        // MY CODE
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            loginScreen();
        }
        else {
            Log.i(TAG, currentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Success
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_external_storage), Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    mUri = data.getData();
                }

                if (requestCode == PICK_VIDEO_REQUEST) {
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mUri);
                        fileSize = inputStream.available();
                    }
                    catch (Exception e) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_file), Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        }
                        catch (Exception e) { /* Intentionally left blank */ }
                    }

                    if (fileSize > FILE_SIZE_MAX) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_file_size), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mUri);
                sendBroadcast(mediaScanIntent);
            }

            // Show recipient list
            Intent intent = new Intent(this, RecipientsActivity.class);
            intent.setData(mUri);

            if (requestCode == TAKE_PHOTO_REQUEST || requestCode == PICK_PHOTO_REQUEST) {
                intent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_IMAGE);
            }
            else {
                intent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_VIDEO);
            }

            startActivity(intent);
        }
        else if (resultCode != RESULT_CANCELED) {
            // Error
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                ParseUser.logOut();
                loginScreen();
            case R.id.action_edit_friends:
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_options, mOnClickListener);
                AlertDialog alert = builder.create();
                alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void loginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}