package eu.baboi.cristian.musicalstructure.utils.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import eu.baboi.cristian.musicalstructure.R;
import eu.baboi.cristian.musicalstructure.utils.net.Model;

// this is a transient activity that launch the web browser to do authentication
// on return it passes the authorization code/error to calling activity
public class LoginCallback extends AppCompatActivity {
    private static final String MY_ID = LoginCallback.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_callback);
        Button return_to_app = findViewById(R.id.return_to_app);
        Button try_again = findViewById(R.id.try_again);

        return_to_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(getIntent());
            }
        });

        Intent intent = getIntent();
        if (intent == null) return;

        String action = intent.getAction();
        if (action == Intent.ACTION_MAIN) actionMain(intent);
        else if (action == Intent.ACTION_VIEW) actionView(intent);//never called from here
    }

    // entry point when called from browser
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        actionView(intent); //launched from browser
    }

    // pass the result back to caller activity
    private void actionView(Intent intent) {
        setResult(RESULT_OK, intent);
        finish();
    }

    private void actionMain(Intent intent) {
        if (intent == null) return;
        int code = intent.getIntExtra(Model.CODE_KEY, 0);
        switch (code) {
            case Model.LOGIN:
                login(intent);
                break;
            case Model.LOGOUT:
                logout(intent);
                break;
        }
    }

    private boolean noNetwork() {
        //check network connectivity
        if (!Model.hasNetwork(this)) {
            Toast toast = Toast.makeText(this, "No network!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            finish();
            return true;
        }
        return false;
    }

    // open browser to logout
    private void logout(Intent data) {
        if (data == null) return;

        //check network connectivity
        if (noNetwork()) return;

        Uri uri = Uri.parse(Model.LOGOUT_URL);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);

        //this make the browser open the url in the same tab
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, MY_ID);
        startActivity(intent);
        setResult(RESULT_OK, null);//no data to return
        finish();// no return expected
    }

    // open browser to login
    private void login(Intent data) {
        if (data == null) return;

        //check network connectivity
        if (noNetwork()) return;

        String state = data.getStringExtra(Model.STATE_KEY);
        Uri uri = buildUri(state);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);

        //this make the browser open the url in the same tab
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, MY_ID);
        startActivity(intent);// expects return via onNewIntent
    }


    // build Spotify Login Uri
    private static Uri buildUri(String state) {
        Uri.Builder builder = Uri.parse(Model.AUTHORIZE_URL).buildUpon();
        builder.appendQueryParameter(Model.CLIENT_ID, Model.CLIENT);
        builder.appendQueryParameter(Model.RESPONSE_TYPE, Model.CODE);
        builder.appendQueryParameter(Model.REDIRECT, Model.REDIRECT_URI);
        builder.appendQueryParameter(Model.STATE, state);
        return builder.build();
    }

}
