package com.example.buggerduckbot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Our GroundStation Activity. This Activity has 4 {@link State}s.
 *
 * <p>{@link State#UNKNOWN}: We cannot do anything while we're in this state. The app is likely in
 * the background.
 *
 * <p>{@link State#DISCOVERING}: Our default state (after we've connected). We constantly listen for
 * a device to advertise near us.
 *
 * <p>{@link State#ADVERTISING}: If a user shakes their device, they enter this state. We advertise
 * our device so that others nearby can discover us.
 *
 * <p>{@link State#CONNECTED}: We've connected to another device. We can now talk to them by holding
 * down the volume keys and speaking into the phone. We'll continue to advertise (if we were already
 * advertising) so that more people can connect to us.
 */

public class ReciverActivity extends ConnectionsActivity {
    /**
     * If true, debug logs are shown on the device.
     */
    private static final boolean DEBUG = true;

    /**
     * The connection strategy we'll use for Nearby Connections. In this case, we've decided on
     * P2P_STAR, which is a combination of Bluetooth Classic and WiFi Hotspots.
     */
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    /**
     * Advertise for 30 seconds before going back to discovering. If a client connects, we'll continue
     * to advertise indefinitely so others can still connect.
     */
    private static final long ADVERTISING_DURATION = 30000;

    /**
     * Length of state change animations.
     */
    private static final long ANIMATION_DURATION = 600;

    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    private static final String SERVICE_ID =
            "it.unive.dais.nearby.apps.SERVICE_ID";

    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private State mState = State.UNKNOWN;

    /**
     * A random UID used as this device's endpoint name.
     */
    private String mName;

    /**
     * Displays the previous state during animation transitions.
     */
    private TextView mPreviousStateView;

    /**
     * Displays the current state.
     */
    private TextView mCurrentStateView;

    /**
     * An animator that controls the animation from previous state to current state.
     */
    @Nullable
    private Animator mCurrentAnimator;

    /**
     * A running log of debug messages. Only visible when DEBUG=true.
     */
    private TextView mDebugLogView;

    private String KEY = "abcdefgh";
    PopupWindow popupWindow;

    /**
     * Array of mStop agents
     */
    private boolean[] mStop;


    /**
     * A Handler that allows us to post back on to the UI thread. We use this to resume discovery
     * after an uneventful bout of advertising.
     */
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    /**
     * Starts discovery. Used in a postDelayed manor with {@link #mUiHandler}.
     */
    private final Runnable mDiscoverRunnable =
            new Runnable() {
                @Override
                public void run() {
                    setState(State.DISCOVERING);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar()
                .setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.actionBar));

        mPreviousStateView = (TextView) findViewById(R.id.previous_state);
        mCurrentStateView = (TextView) findViewById(R.id.current_state);

        mDebugLogView = (TextView) findViewById(R.id.debug_log);
        mDebugLogView.setVisibility(DEBUG ? View.VISIBLE : View.GONE);
        mDebugLogView.setMovementMethod(new ScrollingMovementMethod());

        /* Comment here to generate a random name for the GroundStation */
        //mName = generateRandomName();
        mName = "GroundStation";

        ((TextView) findViewById(R.id.name)).setText(mName);
        ((TextView) findViewById(R.id.edit_key)).setText(KEY);
        ((TextView) findViewById(R.id.edit_key)).setTypeface(null, Typeface.BOLD);

        mStop = new boolean[6];
        // all the robot are assumed to be in move
        Arrays.fill(mStop, true);
    }

    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Swap the two functions below if you want to start on Discovering rather than Advertising.
        //setState(State.DISCOVERING);
        setState(State.ADVERTISING);
    }

    @Override
    protected void onStop() {

        setState(State.UNKNOWN);

        mUiHandler.removeCallbacksAndMessages(null);

        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getState() == State.CONNECTED || getState() == State.ADVERTISING) {
            setState(State.DISCOVERING);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        // We found an advertiser!
        if (!isConnecting()) {
            connectToEndpoint(endpoint);
        }
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated! We'll accept the connection immediately.
        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();

        // If we lost all our endpoints, then we should reset the state of our app and go back
        // to our initial state (discovering).
        if (getConnectedEndpoints().isEmpty()) {
            setState(State.DISCOVERING);
        }
    }

    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
        // Let's try someone else.
        if (getState() == State.DISCOVERING && !getDiscoveredEndpoints().isEmpty()) {
            connectToEndpoint(pickRandomElem(getDiscoveredEndpoints()));
        }
    }

    /**
     * The state has changed. I wonder what we'll be doing now.
     *
     * @param state The new state.
     */
    private void setState(State state) {
        if (mState == state) {
            logW("State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }

    /**
     * @return The current state.
     */
    private State getState() {
        return mState;
    }

    /**
     * State has changed.
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    private void onStateChanged(State oldState, State newState) {
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        // Update Nearby Connections to the new state.
        switch (newState) {
            case DISCOVERING:
                if (isAdvertising()) {
                    stopAdvertising();
                }
                disconnectFromAllEndpoints();
                startDiscovering();
                break;
            case ADVERTISING:
                if (isDiscovering()) {
                    stopDiscovering();
                }
                disconnectFromAllEndpoints();
                startAdvertising();
                break;
            case CONNECTED:
                if (isDiscovering()) {
                    stopDiscovering();
                } else if (isAdvertising()) {
                    // Continue to advertise, so others can still connect,
                    // but clear the discover runnable.
                    removeCallbacks(mDiscoverRunnable);
                }
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

        // Update the UI.
        switch (oldState) {
            case UNKNOWN:
                // Unknown is our initial state. Whatever state we move to,
                // we're transitioning forwards.
                transitionForward(oldState, newState);
                break;
            case DISCOVERING:
                switch (newState) {
                    case UNKNOWN:
                        transitionBackward(oldState, newState);
                        break;
                    case ADVERTISING:
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case ADVERTISING:
                switch (newState) {
                    case UNKNOWN:
                    case DISCOVERING:
                        transitionBackward(oldState, newState);
                        break;
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case CONNECTED:
                // Connected is our final state. Whatever new state we move to,
                // we're transitioning backwards.
                transitionBackward(oldState, newState);
                break;
            default:
                // no-op
                break;
        }
    }

    /**
     * Transitions from the old state to the new state with an animation implying moving forward.
     */
    @UiThread
    private void transitionForward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mPreviousStateView, oldState);
        updateTextView(mCurrentStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(false /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }

    /**
     * Transitions from the old state to the new state with an animation implying moving backward.
     */
    @UiThread
    private void transitionBackward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mCurrentStateView, oldState);
        updateTextView(mPreviousStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(true /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }

    @NonNull
    private Animator createAnimator(boolean reverse) {
        Animator animator;
        if (Build.VERSION.SDK_INT >= 21) {
            int cx = mCurrentStateView.getMeasuredWidth() / 2;
            int cy = mCurrentStateView.getMeasuredHeight() / 2;
            int initialRadius = 0;
            int finalRadius = Math.max(mCurrentStateView.getWidth(), mCurrentStateView.getHeight());
            if (reverse) {
                int temp = initialRadius;
                initialRadius = finalRadius;
                finalRadius = temp;
            }
            animator =
                    ViewAnimationUtils.createCircularReveal(
                            mCurrentStateView, cx, cy, initialRadius, finalRadius);
        } else {
            float initialAlpha = 0f;
            float finalAlpha = 1f;
            if (reverse) {
                float temp = initialAlpha;
                initialAlpha = finalAlpha;
                finalAlpha = temp;
            }
            mCurrentStateView.setAlpha(initialAlpha);
            animator = ObjectAnimator.ofFloat(mCurrentStateView, "alpha", finalAlpha);
        }
        animator.addListener(
                new AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }
                });
        animator.setDuration(ANIMATION_DURATION);
        return animator;
    }

    /**
     * Updates the {@link TextView} with the correct color/text for the given {@link State}.
     */
    @UiThread
    private void updateTextView(TextView textView, State state) {
        switch (state) {
            case DISCOVERING:
                textView.setBackgroundResource(R.color.state_discovering);
                textView.setText(R.string.status_discovering);
                break;
            case ADVERTISING:
                textView.setBackgroundResource(R.color.state_advertising);
                textView.setText(R.string.status_advertising);
                break;
            case CONNECTED:
                textView.setBackgroundResource(R.color.state_connected);
                textView.setText(R.string.status_connected);
                break;
            default:
                textView.setBackgroundResource(R.color.state_unknown);
                textView.setText(R.string.status_unknown);
                break;
        }
    }


    public void start_advertise(View view) {

        setState(State.ADVERTISING);
        postDelayed(mDiscoverRunnable, ADVERTISING_DURATION);
        Toast toast = Toast.makeText(this, "Starting Advertising", Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Test function for all the possible strings of the protocol
     */
    public void send_Byte(View view) {

        // passive protocol
        String x = "Coordinate recupero:3;6;";
        byte[] bytes = x.getBytes();
        send(Payload.fromBytes(bytes));


        // test encrypted
        x = "Operazione in corso:4;8;";
        Calendar calendar = Calendar.getInstance();
        Long time_long = calendar.getTimeInMillis();
        x = x+time_long.toString()+";";

        bytes = x.getBytes();
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
            Cipher c = Cipher.getInstance("DES/ECB/ISO10126Padding");
            c.init(c.ENCRYPT_MODE, key);

            byte[] ciphertext = c.doFinal(bytes);
            send(Payload.fromBytes(ciphertext));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        x = "Benvenuto sono Pippo";
        bytes = x.getBytes();
        send(Payload.fromBytes(bytes));

        x = "0STOP";
        bytes = x.getBytes();
        send(Payload.fromBytes(bytes));

        x = "1STOP";
        bytes = x.getBytes();
        send(Payload.fromBytes(bytes));
    }

    /**
     * Show on screen a Popup for modifying the secret key
     */
    public void edit_secret_key(View view) {

        showPopup();
    }

    // move along.. this function is a mess
    public void showPopup() {

        // Container layout to hold other components
        LinearLayout llContainer = new LinearLayout(this);

        // Set its orientation to vertical to stack item
        llContainer.setOrientation(LinearLayout.VERTICAL);

        // Container layout to hold EditText and Button
        LinearLayout llContainerInline = new LinearLayout(this);

        // Set its orientation to horizontal to place components next to each other
        llContainerInline.setOrientation(LinearLayout.HORIZONTAL);

        // EditText to get input
        final EditText etInput = new EditText(this);

        // TextView to show an error message when the user does not provide input
        final TextView tvError = new TextView(this);

        // For when the user is done
        Button bDone = new Button(this);

        // If tvError is showing, make it disappear
        etInput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tvError.setVisibility(View.GONE);
            }
        });

        // This is what will show in etInput when the Popup is first created
        etInput.setHint("Insert new Password");
        etInput.setTextColor(Color.WHITE);
        // Input type allowed: Numbers
        //etInput.setRawInputType(Configuration.KEYBOARD_12KEY);

        // Center text inside EditText
        etInput.setGravity(Gravity.CENTER);

        // tvError should be invisible at first
        tvError.setVisibility(View.GONE);

        bDone.setText("Done");

        bDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // If user didn't input anything, show tvError
                if (etInput.getText().toString().equals("")) {
                    //tvError.setText("Please enter a valid value");
                    tvError.setVisibility(View.VISIBLE);
                    etInput.setText("");

                    // else, call method `doneInput()` which we will define later
                } else {
                    doneInput(etInput.getText().toString());
                    popupWindow.dismiss();
                }
            }
        });

        // Define LayoutParams for tvError
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.topMargin = 20;

        // Define LayoutParams for InlineContainer
        LinearLayout.LayoutParams layoutParamsForInlineContainer = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParamsForInlineContainer.topMargin = 30;

        // Define LayoutParams for EditText
        LinearLayout.LayoutParams layoutParamsForInlineET = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Set ET's weight to 1 // Take as much space horizontally as possible
        layoutParamsForInlineET.weight = 1;

        // Define LayoutParams for Button
        LinearLayout.LayoutParams layoutParamsForInlineButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // Set Button's weight to 0
        layoutParamsForInlineButton.weight = 0;

        // Add etInput to inline container
        llContainerInline.addView(etInput, layoutParamsForInlineET);

        // Add button with layoutParams // Order is important
        llContainerInline.addView(bDone, layoutParamsForInlineButton);

        // Add tvError with layoutParams
        llContainer.addView(tvError, layoutParams);

        // Finally add the inline container to llContainer
        llContainer.addView(llContainerInline, layoutParamsForInlineContainer);

        // Set gravity
        llContainer.setGravity(Gravity.CENTER);

        // Set any color to Container's background
        llContainer.setBackgroundColor(0x95000000);

        // Create PopupWindow
        popupWindow = new PopupWindow(llContainer,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // Should be focusable
        popupWindow.setFocusable(true);

        // Show the popup window
        popupWindow.showAtLocation(llContainer, Gravity.CENTER, 0, 0);

    }

    // function called by the pop-up written above ... move along
    public void doneInput(String input) {
        KEY = input;
        ((TextView) findViewById(R.id.edit_key)).setText(KEY);
        // Do anything else with input!
    }
    /**
     * Send coordinate for the second task. The function takes the values from 'test.csv' and
     * creates a thread.. therefore, the UI is not busy
     */
    public void send_coordinate(View view) {

        Thread_Coordinate task = new Thread_Coordinate(this);
        task.execute();
    }

    public class Thread_Coordinate extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private int i;

        public Thread_Coordinate (Context context){
            mContext = context;
            i = 0;
        }

        @Override
        protected void onPreExecute() {
            /*
             *    do things before doInBackground() code runs
             *    such as preparing and showing a Dialog or ProgressBar
             */
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            /*
             *    updating data
             *    such a Dialog or ProgressBar
             */
            String str = "Sent coordinate number "+i;
            Toast.makeText(mContext,str, Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            BufferedReader br = null;
            AssetManager am = mContext.getAssets();
            String coordinate;
            try {
                String sCurrentLine;
                br = new BufferedReader(new InputStreamReader(am.open("test.csv")));
                while ((sCurrentLine = br.readLine()) != null) {
                    i++;
                    String[] mines = sCurrentLine.split(",");
                    coordinate = "Coordinate obiettivo:" + mines[1] + ";" + mines[2] + ";";
                    byte[] bytes = coordinate.getBytes();
                    send(Payload.fromBytes(bytes));
                    publishProgress();
                    SystemClock.sleep(4000);
                }
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (br != null) br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(mContext,"Coordinate sending completed", Toast.LENGTH_LONG).show();
        }
    }

    /** {@see ConnectionsActivity#onReceive(Endpoint, Payload)} */
    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            byte[] bytes = payload.asBytes();
            // comment this send if we are not the Groundstation anymore
            send(payload);
            String str_bytes = new String(bytes);

            // those are needed if you are a robot!
            /*
            Integer aux = Character.getNumericValue(str_bytes.charAt(0));
            if((aux >= 0 && aux <=6) && ((str_bytes.charAt(1)=='S'))){
                if(aux == 0 || aux == 1) {
                    logD(
                            String.format(
                                    "STOP/RESUME message intercepted %s",
                                    str_bytes));
                    // il messaggio è per noi!
                    return;
                }
                else {
                    logD(
                            String.format(
                                    "STOP/RESUME message ignored %s",
                                    str_bytes));
                    // altrimenti lo ignoriamo
                    return;
                }
            }


                if (str_bytes.toLowerCase().contains("obiettivo")) {
                    logD(
                            String.format(
                                    "Recovery message: %s",
                                    str_bytes));
                    // messaggio del protocollo passivo
                    return;
                }
              */

            if (str_bytes.toLowerCase().contains("recupero")) {
                logD(
                        String.format(
                                "Recovery message: %s",
                                str_bytes));
                // messaggio del protocollo passivo (broadcast) terza prova
                return;
            }

            if (str_bytes.toLowerCase().contains("benvenuto")) {
                logD(
                        String.format(
                                "Welcome message: %s",
                                str_bytes));
                // messaggio di benvenuto
                return;
            }

            try {
                SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
                Cipher c = Cipher.getInstance("DES/ECB/ISO10126Padding");
                c.init(c.DECRYPT_MODE, key);

                byte[] plaintext = c.doFinal(bytes);
                String s = new String(plaintext);

                logD(
                        String.format(
                                "BYTE received %s from endpoint %s",
                                s, endpoint.getName()));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (InvalidKeyException)",
                                endpoint.getName()));
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (NoSuchPaddingException)",
                                endpoint.getName()));
                e.printStackTrace();
            } catch (BadPaddingException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (BadPaddingException)",
                                endpoint.getName()));
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                logD(
                        String.format(
                                "BYTE (crypted) received from %s unreadable (IllegalBlockSizeException)",
                                endpoint.getName()));
                e.printStackTrace();
            }
        }

    }




    private void motion_stop (Integer n){
        String str;

        if (mStop[n]) {
            str = n.toString()+"STOP";
            mStop[n] = false;
            if(n == 0){ Arrays.fill(mStop, false);}
        }
        else
        {
            str = n.toString()+"RESUME";
            mStop[n] = true;
            if(n == 0){ Arrays.fill(mStop, true);}
        }
        byte[] bytes = str.getBytes();
        send(Payload.fromBytes(bytes));

    }

    public void stop_stop_click(View view) {
        Integer value =  Integer.valueOf((view.getTag().toString()));
        motion_stop(value);
    }

    /** {@see ConnectionsActivity#getRequiredPermissions()} */
    @Override
    protected String[] getRequiredPermissions() {
        return join(
                super.getRequiredPermissions(),
                Manifest.permission.RECORD_AUDIO);
    }

    /** Joins 2 arrays together. */
    private static String[] join(String[] a, String... b) {
        String[] join = new String[a.length + b.length];
        System.arraycopy(a, 0, join, 0, a.length);
        System.arraycopy(b, 0, join, a.length, b.length);
        return join;
    }

    /**
     * Queries the phone's contacts for their own profile, and returns their name. Used when
     * connecting to another device.
     */
    @Override
    protected String getName() {
        return mName;
    }

    /** {@see ConnectionsActivity#getServiceId()} */
    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }

    /** {@see ConnectionsActivity#getStrategy()} */
    @Override
    public Strategy getStrategy() {
        return STRATEGY;
    }

    /** {@see Handler#post()} */
    protected void post(Runnable r) {
        mUiHandler.post(r);
    }

    /** {@see Handler#postDelayed(Runnable, long)} */
    protected void postDelayed(Runnable r, long duration) {
        mUiHandler.postDelayed(r, duration);
    }

    /** {@see Handler#removeCallbacks(Runnable)} */
    protected void removeCallbacks(Runnable r) {
        mUiHandler.removeCallbacks(r);
    }

    @Override
    protected void logV(String msg) {
        super.logV(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_verbose)));
    }

    @Override
    protected void logD(String msg) {
        super.logD(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_debug)));
    }

    @Override
    protected void logW(String msg) {
        super.logW(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logW(String msg, Throwable e) {
        super.logW(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logE(String msg, Throwable e) {
        super.logE(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_error)));
    }

    private void appendToLogs(CharSequence msg) {
        mDebugLogView.append("\n");
        mDebugLogView.append(DateFormat.format("hh:mm", System.currentTimeMillis()) + ": ");
        mDebugLogView.append(msg);
    }

    private static CharSequence toColor(String msg, int color) {
        SpannableString spannable = new SpannableString(msg);
        spannable.setSpan(new ForegroundColorSpan(color), 0, msg.length(), 0);
        return spannable;
    }



    @SuppressWarnings("unchecked")
    private static <T> T pickRandomElem(Collection<T> collection) {
        return (T) collection.toArray()[new Random().nextInt(collection.size())];
    }

    /**
     * Provides an implementation of Animator.AnimatorListener so that we only have to override the
     * method(s) we're interested in.
     */
    private abstract static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }

    /** States that the UI goes through. */
    public enum State {
        UNKNOWN,
        DISCOVERING,
        ADVERTISING,
        CONNECTED
    }
}
