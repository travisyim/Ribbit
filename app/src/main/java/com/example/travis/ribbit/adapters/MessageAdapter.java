package com.example.travis.ribbit.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.travis.ribbit.R;
import com.example.travis.ribbit.utils.ParseConstants;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mMessages;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item, messages);

        mContext = context;
        mMessages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Check to see if view for item exists
        if (convertView == null) { // No
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageViewMessage);
            holder.textViewSender = (TextView) convertView.findViewById(R.id.textViewSender);
            holder.textViewTimeElapsed = (TextView) convertView.findViewById(R.id.textViewTimeElapsed);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate icon image and sender name for specific item view
        ParseObject message = mMessages.get(position);

        // Show elapsed time since message was sent
        Date createdAt = message.getCreatedAt();
        long now = new Date().getTime();
        String elapsedTime = DateUtils.getRelativeTimeSpanString(createdAt.getTime(), now, DateUtils.SECOND_IN_MILLIS).toString();
        holder.textViewTimeElapsed.setText(elapsedTime);

        // Set appropriate icon image
        if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.imageView.setImageResource(R.drawable.ic_picture);
        }
        else {
            holder.imageView.setImageResource(R.drawable.ic_video);
        }

        holder.textViewSender.setText(message.getString(ParseConstants.KEY_SENDER_NAME));

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textViewSender;
        TextView textViewTimeElapsed;
    }

    public void refill(List<ParseObject> messages) {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }
}
