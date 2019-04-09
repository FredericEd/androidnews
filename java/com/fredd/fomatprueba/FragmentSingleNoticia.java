package com.fredd.fomatprueba;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.HashMap;

public class FragmentSingleNoticia extends Fragment {

    private View rootView;
    private View mProgressView;
    private View mContentView;
    private GetJSONTask jsonTask;
    private String esCont = "", enCont = "", esTitle = "", enTitle = "";
    private Boolean translated = false;
    private Bundle bundl;

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.fomat)
            .showImageOnFail(R.drawable.fomat)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

	public FragmentSingleNoticia(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_noticia, container, false);

        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        try {
            bundl = getArguments();
            JSONObject noticia = new JSONObject(bundl.getString("EXTRA2"));
            esTitle = convertUTF8ToString(noticia.getString("title"));
            esCont = stripHtml(noticia.getString("text"));

            TextView textTitulo = (TextView) rootView.findViewById(R.id.textTitulo);
            TextView textFecha = (TextView) rootView.findViewById(R.id.textFecha);
            TextView textContenido = (TextView) rootView.findViewById(R.id.textContenido);
            ImageView imgNoticia = (ImageView) rootView.findViewById(R.id.imgNoticia);

            ImageLoader imageLoader = ImageLoader.getInstance();
            textTitulo.setText(esTitle);
            String fecha = noticia.getString("publicationDate").split(" ")[0].replace('-', '/');
            textFecha.setText(fecha);
            textContenido.setText(esCont);
            imageLoader.displayImage(noticia.getString("image"), imgNoticia, options);

            Button btnTranslate = (Button) rootView.findViewById(R.id.btnTraducir);
            btnTranslate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView textTitulo = (TextView) rootView.findViewById(R.id.textTitulo);
                    TextView textContenido = (TextView) rootView.findViewById(R.id.textContenido);
                    Button btnTranslate = (Button) rootView.findViewById(R.id.btnTraducir);
                    if (translated) {
                        textTitulo.setText(esTitle);
                        textContenido.setText(esCont);
                        btnTranslate.setText(getActivity().getString(R.string.es_translate));
                        translated = false;
                    } else if (!enCont.equals("")) {
                        textTitulo.setText(enTitle);
                        textContenido.setText(enCont);
                        btnTranslate.setText(getActivity().getString(R.string.en_translate));
                        translated = true;
                    } else {
                        if (!NetworkUtils.isConnected(getActivity())) {
                            Toast.makeText(getActivity(), R.string.no_conexion, Toast.LENGTH_LONG).show();
                            return;
                        }
                        jsonTask = new GetJSONTask("http://camioner.com/api/v1/translate", esTitle, esCont);
                        jsonTask.execute();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
        }
		return rootView;
	}

    private static String convertUTF8ToString(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("ISO-8859-1"), "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    private String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    public class GetJSONTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final String mTitulo;
        private final String mTexto;
        private boolean success = true;

        GetJSONTask(String URL, String titulo, String texto) {
            mURL = URL;
            mTitulo = titulo;
            mTexto = texto;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("title", mTitulo);
                meMap.put("text", mTexto);
                Log.i("URL", mURL);
                jsonOb = jParser.getJSONPOSTFromUrl(mURL, meMap);
                Log.i("response", jsonOb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            jsonTask = null;
            showProgress(false);
            try {
                if (success) {
                    enTitle = response.getString("title");
                    enCont = response.getString("message");
                    TextView textTitulo = (TextView) rootView.findViewById(R.id.textTitulo);
                    textTitulo.setText(enTitle);
                    TextView textContenido = (TextView) rootView.findViewById(R.id.textContenido);
                    textContenido.setText(enCont);
                    Button btnTranslate = (Button) rootView.findViewById(R.id.btnTraducir);
                    btnTranslate.setText(getActivity().getString(R.string.en_translate));
                    translated = true;
                }
            } catch (Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            jsonTask = null;
            showProgress(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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

    public void backPressed () {
        Fragment fragment = new FragmentNoticias();
        fragment.setArguments(bundl);
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();
    }
}