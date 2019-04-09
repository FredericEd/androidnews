package com.fredd.fomatprueba;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;
    private UserGoogleTask mGoogleTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 908;
    private String URL_login = "http://camioner.com/api/v1/paradasfom/login";
    private String URL_glogin = "http://camioner.com/api/v1/paradasfom/glogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        /*mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (!NetworkUtils.isConnected(LoginActivity.this)) {
                        Toast.makeText(LoginActivity.this, R.string.no_conexion, Toast.LENGTH_LONG).show();
                        return false;
                    }
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });*/

        Button mLoginButton = (Button) findViewById(R.id.buttonLogin);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkUtils.isConnected(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, R.string.no_conexion, Toast.LENGTH_LONG).show();
                    return;
                }
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        SignInButton googleButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleButton.setSize(SignInButton.SIZE_WIDE);
        setGooglePlusButtonText(googleButton, "Iniciar sesión con Google");
        googleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkUtils.isConnected(LoginActivity.this)) {
                    Snackbar.make(mLoginFormView,  R.string.no_conexion, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        Button mRegistroButton = (Button) findViewById(R.id.buttonRegistro);
        mRegistroButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(LoginActivity.this, ActivityRegistro.class);
                LoginActivity.this.startActivity(mainIntent);
            }
        });

        TextView textForgot = (TextView) findViewById(R.id.textForgot);
        textForgot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(LoginActivity.this, ActivityForgot.class);
                LoginActivity.this.startActivity(mainIntent);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        SharedPreferences settings = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        if (settings.getString("jsonUsuario", "").equals("") ) {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                if (!NetworkUtils.isConnected(LoginActivity.this)) {
                    Snackbar.make(mLoginFormView, R.string.no_conexion, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                if (mGoogleTask == null) {
                    mGoogleTask = new UserGoogleTask(URL_glogin, acct.getEmail(), acct.getId(), acct.getDisplayName(), acct.getPhotoUrl().toString());
                    mGoogleTask.execute((Void) null);
                }
            }
        } else {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(mainIntent);
            LoginActivity.this.finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            if (acct != null) {
                if (!NetworkUtils.isConnected(LoginActivity.this)) {
                    Snackbar.make(mLoginFormView,  R.string.no_conexion, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                if (mGoogleTask == null) {
                    mGoogleTask = new UserGoogleTask(URL_glogin, acct.getEmail(), acct.getId(), acct.getDisplayName(), acct.getPhotoUrl().toString());
                    mGoogleTask.execute((Void) null);
                }
            }
        } catch (ApiException e) {
            Snackbar.make(mLoginFormView,  R.string.google_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            Log.w(getString(R.string.error_tag), "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    private void attemptLogin() {
        if (mAuthTask != null) return;

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(URL_login, email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final String mEmail;
        private final String mPassword;
        private Boolean success = false;

        UserLoginTask(String URL, String email, String password) {
            mURL = URL;
            mEmail = email;
            mPassword = password;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("email", mEmail);
                meMap.put("clave", mPassword);

                JSONParser jParser = new JSONParser();
                Log.i("URL", mURL);
                jsonOb = jParser.getJSONPOSTFromUrl(mURL, meMap);
                Log.i("DATA", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mAuthTask = null;
            showProgress(false);

            try {
                if (success) {
                    Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                    SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("jsonUsuario", response.getJSONObject("usuario").toString());
                    editor.commit();
                    Log.i("DATA", response.getJSONObject("usuario").toString());

                    LoginActivity.this.finish();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(mainIntent);
                } else {
                    Snackbar.make(mLoginFormView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    mPasswordView.setText("");
                    mPasswordView.requestFocus();
                }
            } catch(Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserGoogleTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final String mEmail;
        private final String mPassword;
        private final String mNombre;
        private final String mFoto;
        private Boolean success = false;

        UserGoogleTask(String URL, String email, String password, String nombre, String foto) {
            mURL = URL;
            mEmail = email;
            mPassword = password;
            mFoto = foto;
            mNombre = nombre;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("email", mEmail);
                meMap.put("clave", mPassword);
                meMap.put("nombre", mNombre);
                meMap.put("foto", mFoto);

                JSONParser jParser = new JSONParser();
                Log.i("URL", mURL);
                jsonOb = jParser.getJSONPOSTFromUrl(mURL, meMap);
                Log.i("DATA", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mGoogleTask = null;
            showProgress(false);

            try {
                if (success) {
                    Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                    SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("jsonUsuario", response.getJSONObject("usuario").toString());
                    editor.commit();

                    LoginActivity.this.finish();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(mainIntent);
                } else {
                    Snackbar.make(mLoginFormView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    mPasswordView.setText("");
                    mPasswordView.requestFocus();
                }
            } catch(Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mGoogleTask = null;
            showProgress(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

