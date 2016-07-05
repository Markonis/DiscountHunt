package rs.elfak.mosis.marko.discounthunt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserLocationChangeEndpoint;

public class BackgroundService extends Service {
    private static final long REFRESH_INTERVAL = 20000;
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.background_service_started;

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();
    private Timer refreshTimer;
    private CurrentLocation mCurrentLocation;

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mCurrentLocation = new CurrentLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, R.string.background_service_started, Toast.LENGTH_SHORT).show();
        startRefreshTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);
        refreshTimer.purge();
        Toast.makeText(this, R.string.background_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void startRefreshTimer() {
        if(refreshTimer == null){
            TimerTask refreshTimerTask = new TimerTask() {
                @Override
                public void run() {
                    updateUserLocation();
                }
            };
            refreshTimer = new Timer();
            refreshTimer.schedule(refreshTimerTask, 1000, REFRESH_INTERVAL);
        }
    }

    private void updateUserLocation() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            jsonObject.put("user_id", userJsonObject.getInt("id"));
            jsonObject.put("location_attributes", mCurrentLocation.toJSONObject());
            UserLocationChangeEndpoint endpoint = new UserLocationChangeEndpoint();
            endpoint.post(jsonObject,
                    new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
        }catch (Exception ex){}
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.background_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SigninActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.background_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}
