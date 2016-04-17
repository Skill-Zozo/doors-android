package com.doors.thegrid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

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
import java.util.List;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private AuthData mAuthData;
    private Firebase mFirebaseRef;
    private Firebase.AuthStateListener mAuthStateListener;
    private Button mPasswordLoginButton;
    private static final String TAG = LoginActivity.class.getSimpleName();

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
    private View mProgressView;
    private ProgressDialog mAuthProgressDialog;
    private Button mStartSignUp;
    private Button mSignUp;
    private TextView mReEnterPWD;
    private EditText mReEnterPassword;
    private boolean signupClicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
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
                        resolveSignInError();
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
        ;

        mProgressView = findViewById(R.id.login_progress);

        /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Firebase...");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.hide();
                setAuthenticatedUser(authData);
            }
        };
        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        mFirebaseRef.addAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if changing configurations, stop tracking firebase session.
        mFirebaseRef.removeAuthStateListener(mAuthStateListener);
        mAuthProgressDialog.dismiss();
    }

    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            /* show a provider specific status text */
            String name = null;
            if (authData.getProvider().equals("google")) {
                name = (String) authData.getProviderData().get("displayName");
            } else if (authData.getProvider().equals("anonymous")) {
                name = authData.getUid();
            }

        } else {
            /* No authenticated user show all the login buttons */
            mGoogleLoginButton.setVisibility(View.VISIBLE);
        }
        this.mAuthData = authData;
        /* invalidate options menu to hide/show the logout button */
        supportInvalidateOptionsMenu();
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

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(boolean... signup) {
        mAuthProgressDialog.show();
        mFirebaseRef.authWithPassword("test@firebaseuser.com", "test1234", new AuthResultHandler("password"));
        if (mAuthTask != null) {
            return;
        }

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
            //showProgress(true);
            loginUser(email, password);
        }
    }

    private void loginUser(String email, String password) {
        mFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
            }
        });
    }

    private void createUser(String email, String password) {
        mFirebaseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("Successfully created user account with uid: " + result.get("uid"));
            }

            @Override
            public void onError(FirebaseError fberror){
                System.out.println(fberror);
            }
        });
    }

    private void attemptSignup() {
        attemptLogin(true);
    }

    private boolean isEmailValid(String usrname) {
        //TODO: Replace this with your own logic
        return usrname.length() > 3;
    }

    private boolean isPasswordValid(String password) {
        if (!password.matches(".*\\d+.*")) {
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
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getGoogleOAuthTokenAndLogin();

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
                    mFirebaseRef.authWithOAuthToken("google", token, new AuthResultHandler("google"));
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

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            mAuthProgressDialog.hide();
            Log.i(TAG, provider + " auth successful");
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            mAuthProgressDialog.hide();
            showErrorDialog(firebaseError.toString());
        }
    }

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
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        if (this.mAuthData != null) {
            /* logout of Firebase */
            mFirebaseRef.unauth();
            /* Logout of any of the Frameworks. This step is optional, but ensures the user is not logged into
             * Facebook/Google+ after logging out of Firebase. */
            if (this.mAuthData.getProvider().equals("google")) {
                /* Logout from Google+ */
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
            }
            /* Update authenticated user and show login buttons */
            setAuthenticatedUser(null);
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
                mFirebaseRef.authWithPassword(mUser, mPassword, new AuthResultHandler("password"));
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

