package com.doors.thegrid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button mPasswordLoginButton;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Profile mUser;

    /**
     * GOOGLE SETUP
     */
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_GOOGLE_LOGIN = 1;
    private boolean mGoogleLoginClicked;
    private boolean mGoogleIntentInProgress;
    private ConnectionResult mGoogleConnectionResult;
    private SignInButton mGoogleLoginButton;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private ProgressDialog mAuthProgressDialog;
    private Button mStartSignUp;
    private TextView mReEnterPWD;
    private EditText mReEnterPassword;
    private boolean signupClicked = false;

    /**
     *
     * server communications
     */
    private Connector mConnection = new Connector();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mStartSignUp = (Button) findViewById(R.id.signup);
        mReEnterPWD = (TextView) findViewById(R.id.pwrdView2);
        mReEnterPassword = (EditText) findViewById(R.id.pwd2);
        mStartSignUp.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!signupClicked) {
                    mGoogleLoginButton.setVisibility(View.GONE);
                    mPasswordLoginButton.setVisibility(View.GONE);
                    mReEnterPassword.setVisibility(View.VISIBLE);
                    mReEnterPWD.setVisibility(View.VISIBLE);
                    signupClicked = true;
                } else {
                    attemptSignup();
                    signupClicked =false;
                }
            }
        }));
        /**
         *  Handle Google login
         **/
        mGoogleLoginButton = (SignInButton) findViewById(R.id.login_with_google);
        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleLoginClicked = true;
                if (!mGoogleApiClient.isConnecting()) {
                    if (mGoogleConnectionResult != null) {
                        //resolveSignInError();
                    } else if (mGoogleApiClient.isConnected()) {
                        getGoogleOAuthTokenAndLogin();
                    } else {
                        mGoogleApiClient.connect();
                    }
                }
            }
        });

        /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        /**
         * Handle login through username+password
         * */

        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.usr);
        mPasswordView = (EditText) findViewById(R.id.pwd);
        mPasswordLoginButton = (Button) findViewById(R.id.loginButton);
        mPasswordLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });



        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Firebase...");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * This method fires when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Map<String, String> options = new HashMap<String, String>();
        if (requestCode == RC_GOOGLE_LOGIN) {
            /* This was a request by the Google API */
            if (resultCode != RESULT_OK) {
                mGoogleLoginClicked = false;
            }
            mGoogleIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    private void switchActivity() {
        Activity main = new MainActivity();
        Intent intent = new Intent(getApplicationContext(), main.getClass());
        intent.putExtra("user", mUser);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(boolean... signup) {

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = mPasswordView;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mUserView.setError(getString(R.string.error_invalid_email));
            focusView = mUserView;
            cancel = true;
        }

        //create user
        if(signup!=null && signup[0]) {
            String confirmPassword = mReEnterPassword.getText().toString();
            if(TextUtils.isEmpty(confirmPassword)) {
                mReEnterPassword.setError("re enter password");
                cancel = true;
            }
            if(!confirmPassword.equals(password)) {
                mReEnterPassword.setError("passwords do not match");
                cancel = true;
            }
            if (!cancel) createUser(email, password);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginUser(email, password);
        }
    }

    private void loginUser(String email, String password) {
        String connectionResult = mConnection.login(email, password);
        System.out.println(connectionResult);
        if(!connectionResult.contains("failed")) {
            mUser = mConnection.getUser(email, password);
            switchActivity();
        }

    }

    private void createUser(String email, String password) {
        //
    }

    private void attemptSignup() {
        attemptLogin(true);
    }

    private boolean isEmailValid(String usrname) {
        //TODO: Replace this with your own logic
        return usrname.length() > 3;
    }

    private boolean isPasswordValid(String password) {
        /*if (!password.matches(".*\\d+.*")) {
            mPasswordView.setError("password must contain a digit");
            return false;
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            mPasswordView.setError("password must contain a special character");
            return false;
        }
        if (password.matches(".*" + mUserView.getText().toString() +".*")) {
            mPasswordView.setError("password too obvious");
            return false;
        }
        if (password.length() < 6) {
            mPasswordView.setError("password too short");
            return false;
        }*/
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getGoogleOAuthTokenAndLogin();
        //TODO: get your own token and begin session, Dikoko to help
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            }
        }
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert);
    }

    private void getGoogleOAuthTokenAndLogin() {
        mAuthProgressDialog.show();
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(LoginActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                } else if (errorMessage != null) {
                    mAuthProgressDialog.hide();
                }
            }
        };
        task.execute();
    }

    /**
     * Utility class for authentication results
     */


    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String usr, String password) {
            mUser = usr;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                mAuthProgressDialog.show();
                //mFirebaseRef.authWithPassword(mUser, mPassword, new AuthResultHandler("password"));
            } catch (Exception e) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

