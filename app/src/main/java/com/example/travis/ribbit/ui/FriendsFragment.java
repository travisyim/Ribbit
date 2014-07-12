package com.example.travis.ribbit.ui;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.travis.ribbit.R;
import com.example.travis.ribbit.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class FriendsFragment extends ListFragment {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private List<ParseUser> mFriends;
    private ParseUser mUser;
    private ParseRelation<ParseUser> mFriendsRelation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2, R.color.swipe_refresh3, R.color.swipe_refresh4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMessages();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mSwipeRefreshLayout.setRefreshing(true);
        refreshMessages();
    }

    private void refreshMessages() {
        mUser = ParseUser.getCurrentUser();
        mFriendsRelation = mUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                mSwipeRefreshLayout.setRefreshing(false);

                if (e == null) {
                    int i = 0;
                    mFriends = parseUsers;
                    String[] usernames = new String[mFriends.size()];

                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (getActivity(), android.R.layout.simple_list_item_1, usernames);
                    setListAdapter(adapter);
                }
                else {
                    Log.e(TAG, e.getMessage());

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }
}