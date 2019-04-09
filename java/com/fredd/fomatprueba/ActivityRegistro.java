package com.fredd.fomatprueba;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class ActivityRegistro extends AppCompatActivity {

    private UserRegistroTask mAuthTask = null;
    private UploadTask mUploadTask = null;

    // UI references.
    private View mProgressView;
    private View mContentView;
    private EditText mEmailView, mPasswordView, mPassword2View, mNombreView;
    private String URL = "http://camioner.com/api/v1/paradasfom/create", URL_upload = "http://camioner.com/api/uploads/photo_uploader_api.php";
    private String nacimiento = "", URLPic = "";
    private final int GALLERY_REQUEST_CODE = 234, PERMISSIONS = 5643;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mContentView = findViewById(R.id.contentView);
        mProgressView = findViewById(R.id.progressView);


        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPassword2View = (EditText) findViewById(R.id.password2);
        mNombreView = (EditText) findViewById(R.id.nombre);

        Button btnRegistro = (Button) findViewById(R.id.buttonRegistro);
        btnRegistro.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkUtils.isConnected(ActivityRegistro.this)) {
                    Toast.makeText(ActivityRegistro.this, R.string.no_conexion, Toast.LENGTH_LONG).show();
                    return;
                }
                attemptRegistro();
            }
        });

        Button btnImagen = (Button) findViewById(R.id.buttonImagen);
        btnImagen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ActivityRegistro.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ActivityRegistro.this, new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSIONS);
            } else openGalley();
            }
        });

        final Button btnNacimiento = (Button) findViewById(R.id.btnNacimiento);
        btnNacimiento.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Calendar cal = Calendar.getInstance();
                    final int mYear = cal.get(Calendar.YEAR);
                    final int mMonth = cal.get(Calendar.MONTH);
                    final int mDay = cal.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityRegistro.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            if (year > mYear || year == mYear && monthOfYear > mMonth || year == mYear && monthOfYear == mMonth && dayOfMonth > mDay) {
                                Toast.makeText(ActivityRegistro.this, R.string.fecha_superior, Toast.LENGTH_LONG).show();
                                return;
                            }
                            nacimiento = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                            btnNacimiento.setText(nacimiento);
                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                } catch (Exception e) {
                    Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
                }
            }
        });
    }

    private void attemptRegistro() {
        if (mAuthTask != null) return;

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPassword2View.setError(null);
        mNombreView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password2 = mPassword2View.getText().toString();
        String nombre = mNombreView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nombre)) {
            mNombreView.setError(getString(R.string.error_field_required));
            focusView = mNombreView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!password.equals(password2)) {
            mPassword2View.setError(getString(R.string.error_mismatch_password));
            focusView = mPassword2View;
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
        if (nacimiento.equals("")) {
            Snackbar.make(mContentView, getResources().getString(R.string.activity_auth_message_nacimiento_required), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            cancel = true;
        }
        /*if (URLPic.equals("")) {
            Snackbar.make(mContentView, getResources().getString(R.string.activity_auth_message_foto_required), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            cancel = true;
        }*/
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserRegistroTask(URL, nombre, email, password, nacimiento, URLPic);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    ImageView imgPerfil = (ImageView) findViewById(R.id.imgPerfil);
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    imgPerfil.setImageURI(selectedImage);
                    mUploadTask = new UploadTask(selectedImage);
                    mUploadTask.execute((Void) null);
                    break;
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalley();
                }
                return;
            }
        }
    }
    public class UploadTask extends AsyncTask<Void, Void, JSONObject> {

        private final Uri selectedImage;

        UploadTask(Uri selectedImage) {
            this.selectedImage = selectedImage;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                String fileName = getPath(selectedImage);

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;

                File sourceFile = new File(getPath(selectedImage));
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(URL_upload);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("filename", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"filename\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){

                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                InputStream responseStream = new BufferedInputStream(conn.getInputStream());
                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();
                String response = stringBuilder.toString();
                jsonOb = new JSONObject(response);

            } catch (MalformedURLException ex) {
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                Log.e("Upload to server", "Exception : "  + e.getMessage(), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mUploadTask = null;
            showProgress(false);
            try {
                if (response.has("file"))
                    URLPic = "http://camioner.com/api/uploads/images/" + response.getString("file");
                else Snackbar.make(mContentView, R.string.image_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } catch (Exception e) {
                Log.e(ActivityRegistro.this.getResources().getString(R.string.app_name), ActivityRegistro.this.getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mUploadTask = null;
            showProgress(false);
        }
    }

    public class UserRegistroTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final String mNombre;
        private final String mEmail;
        private final String mPassword;
        private final String mNacimiento;
        private final String mFoto;
        private boolean success;

        UserRegistroTask(String URL, String nombre, String email, String password, String nacimiento, String foto) {
            mURL = URL;
            mNombre = nombre;
            mEmail = email;
            mPassword = password;
            mNacimiento = nacimiento;
            mFoto = foto;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("nombre", mNombre);
                meMap.put("email", mEmail);
                meMap.put("clave", mPassword);
                meMap.put("nacimiento", mNacimiento);
                meMap.put("foto", mFoto);

                Log.i("DATA", meMap.toString());
                Log.i("URL", mURL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPOSTFromUrl(mURL, meMap);
                Log.i("response", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(ActivityRegistro.this.getResources().getString(R.string.app_name), ActivityRegistro.this.getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mAuthTask = null;
            showProgress(false);

            try {
                if (success) {
                    Toast.makeText(ActivityRegistro.this, response.getString("message"), Toast.LENGTH_LONG).show();
                    ActivityRegistro.this.finish();
                    Intent mainIntent = new Intent(ActivityRegistro.this, LoginActivity.class);
                    ActivityRegistro.this.startActivity(mainIntent);
                } else {
                    Snackbar.make(mContentView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } catch (Exception e) {
                Log.e(ActivityRegistro.this.getResources().getString(R.string.app_name), ActivityRegistro.this.getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void openGalley() {
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private String getPath(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = ActivityRegistro.this.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

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
}