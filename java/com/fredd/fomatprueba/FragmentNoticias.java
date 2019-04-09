package com.fredd.fomatprueba;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fredd.fomatprueba.adapters.NoticiasAdaptar;
import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;

import org.json.JSONObject;

public class FragmentNoticias extends Fragment {

    private View rootView;
    private View mProgressView;
    private View mContentView;
    private GetJSONTask jsonTask;
    private String URL, categoria;

	public FragmentNoticias(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_recycler, container, false);

        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        try {
            Bundle bundl = getArguments();
            categoria = bundl.getString("EXTRA1");
            URL = "http://www.rtve.es/api/tematicas/" + categoria + "/noticias.json";
            if (!NetworkUtils.isConnected(getActivity())) {
                Toast.makeText(getActivity(), R.string.no_conexion, Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
                return rootView;
            }
            jsonTask = new GetJSONTask(URL);
            jsonTask.execute();
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
        }
		return rootView;
	}

    public class GetJSONTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private boolean success = false;

        GetJSONTask(String URL) {
            mURL = URL;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                Log.i("URL", mURL);
                jsonOb = jParser.getJSONFromUrl(mURL);
                Log.i("response", jsonOb.toString());
                if (jsonOb.getJSONObject("page").getJSONArray("items").length() > 0) success = true;
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
                RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
                if (success) {
                    mRecyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity());
                    mRecyclerView.setLayoutManager(mLayoutManager2);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    NoticiasAdaptar mAdapter = new NoticiasAdaptar(getActivity(), response.getJSONObject("page").getJSONArray("items"), categoria);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    TextView textEmpty = (TextView) rootView.findViewById(R.id.textEmpty);
                    textEmpty.setVisibility(View.VISIBLE);
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
}