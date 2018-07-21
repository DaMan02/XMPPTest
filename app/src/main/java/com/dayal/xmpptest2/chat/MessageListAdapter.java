package com.dayal.xmpptest2.chat;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dayal.xmpptest2.R;
import com.dayal.xmpptest2.models.ChatMessage;
import com.dayal.xmpptest2.models.Contact;
import com.dayal.xmpptest2.utils.DateUtils;
import com.dayal.xmpptest2.utils.MessageUtils;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Manjeet Dayal on 12-07-2018.
 */

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<ChatMessage> mMessageList;
    private String mCurrUser;

    private static final String TAG = "adapterClass:";

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private DownloadManager downloadManager;


    public MessageListAdapter(Context context,List<ChatMessage> messageList,String currUser) {
        mContext = context;
        mMessageList = messageList;
        mCurrUser = currUser;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = mMessageList.get(position);

         if (message.getSender().getJid().equals(mCurrUser)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ChatMessage message = (ChatMessage) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {

        Log.d(TAG, "msgList size: " + mMessageList.size());
        return mMessageList.size();

    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView imageView;

        SentMessageHolder(View itemView) {
            super(itemView);


            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            imageView = (ImageView)itemView.findViewById(R.id.id_image_sent);
        }

        void bind(ChatMessage message) {

            switch(message.getMesssageType()){

                case MessageUtils.TEXT:

                    imageView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);

                    messageText.setTextColor(Color.BLACK);
                    messageText.setText(message.getMessage());
                    break;

                case MessageUtils.IMAGE:

                    imageView.setVisibility(View.VISIBLE);
                    messageText.setVisibility(View.GONE);

     Glide.with(mContext).load( message.getMessage())
                        .listener(new RequestListener<Drawable>() {
       @Override
       public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                  imageView.setImageResource(R.drawable.ic_launcher_background);
                                    return false;
                                }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    return false;
                                }
                            })
                            .into(imageView);

     break;

                case MessageUtils.DOCUMENT:
                    imageView.setVisibility(View.GONE);

                    messageText.setText("[File Sent]");
                    messageText.setTextColor(Color.BLUE);

            }

            timeText.setText(DateUtils.formatDateTime(message.getTimestamp()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView receivedImageView;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            receivedImageView= (ImageView)itemView.findViewById(R.id.id_image_received);

        }
        void bind(final ChatMessage message) {

            switch(message.getMesssageType()){

                case MessageUtils.TEXT:

                    receivedImageView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);

                    messageText.setTextColor(Color.BLACK);
                    messageText.setText(message.getMessage());
                    break;

                case MessageUtils.IMAGE:
                    receivedImageView.setVisibility(View.VISIBLE);
                    messageText.setVisibility(View.GONE);

                    Glide.with(mContext).load(message.getMessage())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    return false;
                                }
                            }).into(receivedImageView);
                    break;

                case MessageUtils.DOCUMENT:
                    receivedImageView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setTextColor(Color.BLUE);
                    messageText.setText("File Received [click to download]:");

                    messageText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadFile(message.getMessage());
                        }
                    });
                    break;

            }

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(message.getTimestamp()));
            nameText.setText(message.getSender().getUserName().split("@")[0]);
      }

        private void downloadFile(String url) {

            Uri uri = Uri.parse(url);
            downloadManager = (DownloadManager)mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            Long reference = downloadManager.enqueue(request);

            Log.d("url", "fileUrl: " + url );


        }

    }
}
