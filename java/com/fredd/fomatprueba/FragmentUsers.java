package com.fredd.fomatprueba;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.fredd.fomatprueba.adapters.UsersAdaptar;
import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;

import org.json.JSONObject;

import java.util.HashMap;

public class FragmentUsers extends Fragment {

    private View rootView;
    private View mProgressView;
    private View mContentView;

    private GetJSONTask jsonTask;
    private UserProcessTask mAddTask;
    private String URL, URL_process = "http://www.camioner.com/api/v1/paradasfom2/";
    private JSONObject usuario;

	public FragmentUsers(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_recycler_double, container, false);

        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        try {
            SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
            usuario = new JSONObject(settings.getString("jsonUsuario", ""));

            URL = "http://www.camioner.com/api/v1/paradasfom/" + usuario.getString("id_usuario");
            if (!NetworkUtils.isConnected(getActivity())) {
                Toast.makeText(getActivity(), R.string.no_conexion, Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
                return rootView;
            }
            getUsers();
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
        }
		return rootView;
	}

    public class GetJSONTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;

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
                TextView textEmpty = (TextView) rootView.findViewById(R.id.textEmpty);
                if (response.getJSONArray("usuarios").length() > 0) {
                    mRecyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    UsersAdaptar mAdapter = new UsersAdaptar(getActivity(), response.getJSONArray("usuarios"), 1, FragmentUsers.this);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.GONE);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.VISIBLE);
                }
                RecyclerView mRecyclerView2 = (RecyclerView) rootView.findViewById(R.id.recycler_view2);
                TextView textEmpty2 = (TextView) rootView.findViewById(R.id.textEmpty2);
                if (response.getJSONArray("follow1").length() > 0) {
                    mRecyclerView2.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    mRecyclerView2.setLayoutManager(mLayoutManager);
                    mRecyclerView2.setItemAnimator(new DefaultItemAnimator());
                    UsersAdaptar mAdapter = new UsersAdaptar(getActivity(), response.getJSONArray("follow1"), 2, FragmentUsers.this);
                    mRecyclerView2.setAdapter(mAdapter);
                    mRecyclerView2.setVisibility(View.VISIBLE);
                    textEmpty2.setVisibility(View.GONE);
                } else {
                    mRecyclerView2.setVisibility(View.GONE);
                    textEmpty2.setVisibility(View.VISIBLE);
                }
                RecyclerView mRecyclerView3 = (RecyclerView) rootView.findViewById(R.id.recycler_view3);
                TextView textEmpty3 = (TextView) rootView.findViewById(R.id.textEmpty3);
                if (response.getJSONArray("follow2").length() > 0) {
                    mRecyclerView3.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    mRecyclerView3.setLayoutManager(mLayoutManager);
                    mRecyclerView3.setItemAnimator(new DefaultItemAnimator());
                    UsersAdaptar mAdapter = new UsersAdaptar(getActivity(), response.getJSONArray("follow2"), 0, FragmentUsers.this);
                    mRecyclerView3.setAdapter(mAdapter);
                    mRecyclerView3.setVisibility(View.VISIBLE);
                    textEmpty3.setVisibility(View.GONE);
                } else {
                    mRecyclerView3.setVisibility(View.GONE);
                    textEmpty3.setVisibility(View.VISIBLE);
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

    private void getUsers() {
	    if (jsonTask != null) return;
        jsonTask = new GetJSONTask(URL);
        jsonTask.execute();
    }

    public void addUser(String id_usuario) {
        try {
            if (mAddTask != null) return;
            mAddTask = new UserProcessTask(URL_process + "create", usuario.getString("id_usuario"), id_usuario);
            mAddTask.execute();
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
        }
    }

    public void deleteUser(String id_usuario) {
        try {
            if (mAddTask != null) return;
            mAddTask = new UserProcessTask(URL_process + "delete", usuario.getString("id_usuario"), id_usuario);
            mAddTask.execute();
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
        }
    }

    public class UserProcessTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mUsuario1;
        private final String mUsuario2;
        private final String mURL;
        private boolean success;

        UserProcessTask(String URL, String id_usuario1, String id_usuario2) {
            mURL = URL;
            mUsuario1 = id_usuario1;
            mUsuario2 = id_usuario2;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("id_usuario1", mUsuario1);
                meMap.put("id_usuario2", mUsuario2);

                Log.i("URL", mURL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPOSTFromUrl(mURL, meMap);
                Log.i("response", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mAddTask = null;
            showProgress(false);

            try {
                Snackbar.make(mContentView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if (success) {
                    getUsers();
                }
            } catch (Exception e) {
                Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mAddTask = null;
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