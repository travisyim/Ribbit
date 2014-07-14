package com.example.travis.ribbit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.travis.ribbit.R;
import com.parse.ParseUser;

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
//            holder.imageView = (ImageView) convertView.findViewById(R.id.imageViewMessage);
            holder.textViewUser = (TextView) convertView.findViewById(R.id.textViewUser);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate icon image and sender name for specific item view
        ParseUser user = mUsers.get(position);

        // Set appropriate icon image
/*        if (user.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.imageView.setImageResource(R.drawable.ic_picture);
        }
        else {
            holder.imageView.setImageResource(R.drawable.ic_video);
        }*/

        holder.textViewUser.setText(user.getUsername());

        return convertView;
    }

    private class ViewHolder {
//        ImageView imageView;
        TextView textViewUser;
    }

    public void refill(List<ParseUser> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
