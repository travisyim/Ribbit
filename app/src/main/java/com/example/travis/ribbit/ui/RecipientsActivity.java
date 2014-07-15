package com.example.travis.ribbit.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.travis.ribbit.R;
import com.example.travis.ribbit.adapters.UserAdapter;
import com.example.travis.ribbit.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RecipientsActivity extends Activity {

    private static final String TAG = RecipientsActivity.class.getSimpleName();

    protected MenuItem mMenuItem;
    protected GridView mGridView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private List<ParseUser> mFriends;
    private ParseUser mUser;
    private ParseRelation<ParseUser> mFriendsRelation;
    private Uri mUri;
    private String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);

        mGridView = (GridView) findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

        // Link empty TextView
        TextView textViewEmpty = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(textViewEmpty);

        // Setup GridView onItemClick listener
        mGridView.setOnItemClickListener(mOnItemClickListener);

        // Ignore SwipeRefreshLayout effects
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);

        mUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();

        mUser = ParseUser.getCurrentUser();
        mFriendsRelation = mUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mFriends = parseUsers;

                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }
                }
                else {
                    Log.e(TAG, e.getMessage());

                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);

        mMenuItem = menu.getItem(0); // Only menu item in menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send) {
            ParseObject message = createMessage();

            if (message == null) {
                // Error
                AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                builder.setTitle(R.string.error_title)
                        .setMessage(getString(R.string.error_file_message))
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog alert = builder.create();
                alert.show();
            }
            else {
                // Success
                sendMessage(message);
                finish();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        String fileName = mUri.getLastPathSegment();

        if (! fileName.contains(".")) {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileName = fileName + ".jpg";
            }
            else if (mFileType.equals(ParseConstants.TYPE_VIDEO)) {
                fileName = fileName + ".mp4";
            }
        }
        ParseFile parseFile = new ParseFile(fileName, convertFileURIToByteArray());

        message.put(ParseConstants.KEY_SENDER_ID, mUser.getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, mUser.getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
        message.put(ParseConstants.KEY_FILE, parseFile);

        return message;
    }

    private ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();

        for (int i = 0; i < mGridView.getCount(); i++) {
            if (mGridView.isItemChecked(i)) {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }

        return recipientIds;
    }

    private byte[] convertFileURIToByteArray() {
        byte[] byteArray = null;
        InputStream inputStream = null;
        ByteArrayOutputStream bos = null;

        try {
            inputStream = getContentResolver().openInputStream(mUri);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
                bos.close();
            }
            catch (Exception e) {/* Intentionally left blank */}
        }

        return byteArray;
    }

    private void sendMessage(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(RecipientsActivity.this, "Message sent!", Toast.LENGTH_LONG).show();
                }
                else {
                    // Error
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(getString(R.string.error_message_send))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            ImageView imageViewCheckmark = (ImageView) view.findViewById(R.id.imageViewCheckmark);

            // Determine whether to show the "Send" menu icon
            if (mGridView.getCheckedItemCount() > 0) {
                mMenuItem.setVisible(true);
            }
            else {
                mMenuItem.setVisible(false);
            }

            // Update checkmark status
            if (mGridView.isItemChecked(position)) {
                // Check
                imageViewCheckmark.setVisibility(View.VISIBLE);
            }
            else {
                // Uncheck
                imageViewCheckmark.setVisibility(View.INVISIBLE);
            }
        }
    };
}