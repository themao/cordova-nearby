package cordova-plugin-nearby;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Messages;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

/**
 * This class echoes a string called from JavaScript.
 */
public class nearby extends CordovaPlugin {

    private static final int PERMISSIONS_REQUEST_CODE = 1111;

    private static final String KEY_SUBSCRIBED = "subscribed";

    /**
     * Tracks subscription state. Set to true when a call to
     * {@link Messages#subscribe(GoogleApiClient, MessageListener)} succeeds.
     */
    private boolean mSubscribed = false;

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Adapter for working with messages from nearby beacons.
     */
    private ArrayAdapter<String> mNearbyMessagesArrayAdapter;

    /**
     * Backing data structure for {@code mNearbyMessagesArrayAdapter}.
     */
    private List<String> mNearbyMessagesList = new ArrayList<>();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            this.start(callbackContext);
            return true;
        }
        return false;
    }

    private void start(CallbackContext callbackContext) {
        if (savedInstanceState != null) {
            mSubscribed = savedInstanceState.getBoolean(KEY_SUBSCRIBED, false);
        }

        final List<String> cachedMessages = Utils.getCachedMessages(this);
        if (cachedMessages != null) {
            mNearbyMessagesList.addAll(cachedMessages);
        }

        final ListView nearbyMessagesListView = (ListView) findViewById(
                R.id.nearby_messages_list_view);
        mNearbyMessagesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mNearbyMessagesList);
        if (nearbyMessagesListView != null) {
            nearbyMessagesListView.setAdapter(mNearbyMessagesArrayAdapter);
        }
        buildGoogleApiClient();
    }

    private void subscribe() {
        // In this sample, we subscribe when the activity is launched, but not on device orientation
        // change.
        if (mSubscribed) {
            Log.i("Already subscribed.");
            return;
        }

        SubscribeOptions options = new SubscribeOptions.Builder()
            .setStrategy(Strategy.BLE_ONLY)
            .build();

        Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options)
            .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.i("Subscribed successfully.");
                        startService(getBackgroundSubscribeServiceIntent());
                    } else {
                        Log.e("Operation failed. Error: " +
                            NearbyMessagesStatusCodes.getStatusCodeString(
                                  status.getStatusCode()));
                    }
                }
            });
    }

    /**
     * Builds {@link GoogleApiClient}, enabling automatic lifecycle management using
     * {@link GoogleApiClient.Builder#enableAutoManage(FragmentActivity,
     * int, GoogleApiClient.OnConnectionFailedListener)}. I.e., GoogleApiClient connects in
     * {@link AppCompatActivity#onStart} -- or if onStart() has already happened -- it connects
     * immediately, and disconnects automatically in {@link AppCompatActivity#onStop}.
     */
    private synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                            .setPermissions(NearbyPermissions.BLE)
                            .build())
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .build();
        }
    }

    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(this, BackgroundSubscribeIntentService.class);
    }
}
