package com.lentera.silaqserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.model.BestDealModel;
import com.lentera.silaqserver.model.CategoryModel;
import com.lentera.silaqserver.model.FoodModel;
import com.lentera.silaqserver.model.MostPopularModel;
import com.lentera.silaqserver.model.ServerUser;
import com.lentera.silaqserver.model.TokenModel;

import org.w3c.dom.Text;

public class Common {
    public static final String SERVER_REF = "Server";
    public static final String CATEGORY_REF = "Category" ;
    public static final String ORDER_REF = "Order";
    public static final String DRIVER = "Driver";
    public static final String DRIVING_ORDER_REF = "DrivingOrder";
    public static final String BEST_DEALS = "BestDeals";
    public static final String MOST_POPULAR = "MostPopular";
    public static ServerUser currentServerUser;
    public static CategoryModel categorySelected;
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String NOTI_TITLE ="title" ;
    public static final String NOTI_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static FoodModel selectedFood;
    public static BestDealModel bestDealSelected;
    public static MostPopularModel mostPopularSelected;

    public enum ACTION{
        CREATE,
        UPDATE,
        DELETE,
    }

    public static void setSpanString(String welcome, String name, TextView textView){
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Diproses";
            case 1:
                return "Dikirim";
            case 2:
                return "Selesai";
            case -1:
                return "Dibatalkan";
            default:
                return "Error";
        }
    }


    public static void showNotification(Context context, int id, String title, String content, Intent intent){
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "SilaqServer";
        NotificationManager notificationManager =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"silaq v2", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Silaq");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_room_service_black_24dp));
        if(pendingIntent != null){
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();
            notificationManager.notify(id, notification);
        }
    }

    public static void updateToken(Context context, String newToken,boolean isServer, boolean isDriver) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new TokenModel(Common.currentServerUser.getPhone(),newToken, isServer,isDriver))
                .addOnFailureListener(e ->
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    public static String createTopicOrder() {
        return  new StringBuilder("/topics/new_order").toString();
    }
}
