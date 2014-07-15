package com.example.travis.ribbit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.travis.ribbit.R;
import com.example.travis.ribbit.utils.MD5Util;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends ArrayAdapter<ParseUser> {

    protected Context mContext;
    protected List<ParseUser> mUsers;

    public UserAdapter(Context context, List<ParseUser> users) {
        super(context, R.layout.user_item, users);

        mContext = context;
        mUsers = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Check to see if view for item exists
        if (convertView == null) { // No
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
            holder = new ViewHolder();
            holder.imageViewUser = (ImageView) convertView.findViewById(R.id.imageViewUser);
            holder.imageViewCheckmark = (ImageView) convertView.findViewById(R.id.imageViewCheckmark);
            holder.textViewUser = (TextView) convertView.findViewById(R.id.textViewUser);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate icon image and sender name for specific item view
        ParseUser user = mUsers.get(position);
        String email = user.getEmail().toLowerCase();

        if (email.equals("")) {
            // Use default picture
            holder.imageViewUser.setImageResource(R.drawable.avatar_empty);
        }
        else {
            String hash = MD5Util.md5Hex(email);
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash + "?s=204&d=404";

            Picasso.with(mContext).load(gravatarUrl).placeholder(R.drawable.avatar_empty).into(holder.imageViewUser);
        }

        // Set checkmark status if previously checked
        if (((GridView)parent).isItemChecked(position)) {
            holder.imageViewCheckmark.setVisibility(View.VISIBLE);
        }
        else {
            holder.imageViewCheckmark.setVisibility(View.INVISIBLE);
        }

        holder.textViewUser.setText(user.getUsername());

        return convertView;
    }

    private class ViewHolder {
        ImageView imageViewUser;
        ImageView imageViewCheckmark;
        TextView textViewUser;
    }

    public void refill(List<ParseUser> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
