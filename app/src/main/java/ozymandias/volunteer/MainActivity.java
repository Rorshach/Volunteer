package ozymandias.volunteer;

import android.content.Intent;
import android.content.IntentSender;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;
    // A flag indicating that a PendingIntent is in progress and prevent us from starting future intents.
    private boolean mIntentInProgress;
    private boolean msignInClicked;

    private Button btnSignOut, btnRevokeAccess;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private LinearLayout llProfileLayout;

    private int enterCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SignInButton googleBtn = (SignInButton) findViewById(R.id.btn_sign_in);
        googleBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                } else {
                    mGoogleApiClient.clearDefaultAccountAndReconnect();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void enterButton(View v) {
        Intent i = new Intent(getApplicationContext(), nextActivity.class);
        startActivity(i);
    }

    public void logoutBtn(View v) {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            // updateUI(false);
            System.err.println("LOG OUT ^^^^^^^^^^^^^^^^^^^^ SUCESS");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors. mGoogleApiClient can be used
        // to access Google APIs on behalf of user.

        if (enterCounter == 1) {
            Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
                    .setResultCallback(this);


            Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = person.getDisplayName();
            //String personBraggingRights = person.getBraggingRights();
            //Image personImage = (Image) person.getImage(); Buggy
            int personGender = person.getGender();
            String personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);

            Toast.makeText(getApplicationContext(), "Welcome to Volunteer " + personName,
                    Toast.LENGTH_LONG).show();
            
            enterButton(this.getCurrentFocus());
            enterCounter++;
        } else if (enterCounter < 1) {
            enterCounter++;
        } else {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0,0,0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent. Return to default
                // state and attempt to connect to get an updated ConnectionResolution.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    Log.d("pBuffer", "Display name: " + personBuffer.get(i).getDisplayName());
                }
            } finally {
                personBuffer.release();
            }
        } else {
            Log.e("pBufferError", "Error requesting visible circles: " + peopleData.getStatus());
        }
    }
}
