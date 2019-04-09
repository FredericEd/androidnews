package com.fredd.fomatprueba;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fredd.fomatprueba.adapters.NoticiasAdaptar;
import com.fredd.fomatprueba.helpers.JSONParser;
import com.fredd.fomatprueba.helpers.NetworkUtils;

import org.json.JSONObject;

public class FragmentHome extends Fragment {

    private View rootView;
    private View mProgressView;
    private View mContentView;

	public FragmentHome(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        LinearLayout layDeportes = (LinearLayout) rootView.findViewById(R.id.lay_deportes);
        layDeportes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "816");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout laySalud = (LinearLayout) rootView.findViewById(R.id.lay_salud);
        laySalud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "1109");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layCultura = (LinearLayout) rootView.findViewById(R.id.lay_cultura);
        layCultura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "827");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layCine = (LinearLayout) rootView.findViewById(R.id.lay_cine);
        layCine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "1090");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layMundo = (LinearLayout) rootView.findViewById(R.id.lay_mundo);
        layMundo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "828");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layClima = (LinearLayout) rootView.findViewById(R.id.lay_clima);
        layClima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "821");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layBienestar = (LinearLayout) rootView.findViewById(R.id.lay_bienestar);
        layBienestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "1076");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layBiologia = (LinearLayout) rootView.findViewById(R.id.lay_biologia);
        layBiologia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "1077");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layArqueologia = (LinearLayout) rootView.findViewById(R.id.lay_arqueologia);
        layArqueologia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "1064");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        LinearLayout layAstronomia = (LinearLayout) rootView.findViewById(R.id.lay_astronomia);
        layAstronomia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", "1067");
                Fragment fragment = new FragmentNoticias();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

		return rootView;
	}
}