package com.fredd.fomatprueba;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;

import org.json.JSONObject;

import java.util.HashMap;

public class ActivityForgot extends AppCompatActivity {

    private EditText mEmailView;
    private EditText mEmail2View;
    private EditText mCodeView;
    private View mProgressView;
    private View mContentView;
    private CheckPasswordTask checkTask = null;
    private SavePasswordTask saveTask = null;
    private ForgotPasswordTask forgotTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        mContentView = findViewById(R.id.contentView);
        mProgressView = findViewById(R.id.progressView);

        mEmailView = (EditText) findViewById(R.id.email);
        mEmail2View = (EditText) findViewById(R.id.email2);
        mCodeView = (EditText) findViewById(R.id.code);

        Button buttonForgot = (Button) findViewById(R.id.buttonForgot);
        buttonForgot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = ActivityForgot.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (!NetworkUtils.isConnected(ActivityForgot.this)) {
                    Snackbar.make(mContentView, R.string.no_conexion, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                attemptForgot();
            }
        });

        Button buttonCheck = (Button) findViewById(R.id.buttonCheck);
        buttonCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = ActivityForgot.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (!NetworkUtils.isConnected(ActivityForgot.this)) {
                    Snackbar.make(mContentView, R.string.no_conexion, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                attemptCheck();
            }
        });
    }

    private void attemptCheck() {
        if (checkTask != null) return;
        mEmail2View.setError(null);
        mCodeView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmail2View.getText().toString();
        String code = mCodeView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(code)) {
            mCodeView.setError(getString(R.string.error_field_required));
            focusView = mCodeView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail2View.setError(getString(R.string.error_field_required));
            focusView = mEmail2View;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail2View.setError(getString(R.string.error_invalid_email));
            focusView = mEmail2View;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            checkTask = new CheckPasswordTask("http://camioner.com/api/v1/paradasfom/recover/verify", email, code);
            checkTask.execute((Void) null);
        }
    }

    private void attemptForgot() {
        if (checkTask != null) return;
        mEmail2View.setError(null);
        mCodeView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        boolean cancel = false;
        View focusView = null;


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
            showProgress(true);
            forgotTask = new ForgotPasswordTask("http://camioner.com/api/v1/paradasfom/recover/request", email);
            forgotTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) { return email.contains("@"); }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
            mContentView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class CheckPasswordTask extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private final String mEmail;
        private final String mCode;
        private boolean success;

        CheckPasswordTask(String URL, String email, String code) {
            this.URL = URL;
            mEmail = email;
            mCode = code;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("email", mEmail);
                meMap.put("code", mCode);

                JSONParser jParser = new JSONParser();
                Log.i("URL", URL);
                jsonOb = jParser.getJSONPOSTFromUrl(URL, meMap);
                Log.i("DATA", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                e.printStackTrace();
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            checkTask = null;
            showProgress(false);

            try {
                if (success) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ActivityForgot.this);
                    alert.setTitle(getResources().getString(R.string.app_name));
                    alert.setMessage(R.string.forgot_alert_password);
                    final EditText input = new EditText(ActivityForgot.this);
                    alert.setView(input);
                    alert.setPositiveButton("GUARDAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            if (!value.equals("")) {
                                if (!NetworkUtils.isConnected(ActivityForgot.this)) {
                                    Snackbar.make(mContentView, R.string.no_conexion, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                    return;
                                }
                                saveTask = new SavePasswordTask("http://camioner.com/api/v1/paradasfom/recover/password", mEmail, value, mCode);
                                saveTask.execute((Void) null);
                            }
                        }
                    });
                    alert.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });
                    alert.show();
                } else {
                    Snackbar.make(mContentView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } catch (Exception e) {
                Log.e(ActivityForgot.this.getResources().getString(R.string.app_name), ActivityForgot.this.getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            checkTask = null;
            showProgress(false);
        }
    }

    public class SavePasswordTask extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private final String email;
        private final String clave;
        private final String code;
        private boolean success;

        SavePasswordTask(String URL, String email, String clave, String code) {
            this.URL = URL;
            this.email = email;
            this.clave = clave;
            this.code = code;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("email", email);
                meMap.put("clave", clave);
                meMap.put("code", code);

                Log.i("URL", URL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPOSTFromUrl(URL, meMap);
                Log.i("DATA", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(ActivityForgot.this.getResources().getString(R.string.app_name), ActivityForgot.this.getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            saveTask = null;
            showProgress(false);
            try {
                Toast.makeText(ActivityForgot.this, response.getString("message"), Toast.LENGTH_LONG).show();
                if (success) {
                    ActivityForgot.this.finish();
                }
            } catch (Exception e) {
                Log.e(ActivityForgot.this.getResources().getString(R.string.app_name), ActivityForgot.this.getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            saveTask = null;
            showProgress(false);
        }
    }

    public class ForgotPasswordTask extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private final String email;
        private boolean success;

        ForgotPasswordTask(String URL, String email) {
            this.URL = URL;
            this.email = email;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("email", email);

                Log.i("URL", URL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPOSTFromUrl(URL, meMap);
                Log.i("DATA", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(ActivityForgot.this.getResources().getString(R.string.app_name), ActivityForgot.this.getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            forgotTask = null;
            showProgress(false);
            try {
                Snackbar.make(mContentView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if (success) {
                    mEmailView.setText("");
                    mEmail2View.setText(email);
                }
            } catch (Exception e) {
                Log.e(ActivityForgot.this.getResources().getString(R.string.app_name), ActivityForgot.this.getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            forgotTask = null;
            showProgress(false);
        }
    }
}

