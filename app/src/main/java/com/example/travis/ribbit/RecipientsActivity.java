package com.example.travis.ribbit;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class RecipientsActivity extends ListActivity {

    private static final String TAG = RecipientsActivity.class.getSimpleName();

    protected MenuItem mMenuItem;

    private List<ParseUser> mFriends;
    private ParseUser mUser;
    private ParseRelation<ParseUser> mFriendsRelation;
    private Uri mUri;
    private String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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
                    int i = 0;
                    mFriends = parseUsers;
                    String[] usernames = new String[mFriends.size()];

                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (RecipientsActivity.this, android.R.layout.simple_list_item_checked, usernames);
                    setListAdapter(adapter);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (l.getCheckedItemCount() > 0) {
            mMenuItem.setVisible(true);
        }
        else {
            mMenuItem.setVisible(false);
        }
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

        for (int i = 0; i < getListView().getCount(); i++) {
            if (getListView().isItemChecked(i)) {
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
            int bytesRead = 0;

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
}