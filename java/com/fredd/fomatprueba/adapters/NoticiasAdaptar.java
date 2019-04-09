package com.fredd.fomatprueba.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fredd.fomatprueba.FragmentSingleNoticia;
import com.fredd.fomatprueba.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

public class NoticiasAdaptar extends RecyclerView.Adapter<NoticiasAdaptar.ViewHolder> {

    JSONArray values;
    Context contexto;
    final String categoria;

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.fomat)
            .showImageOnFail(R.drawable.fomat)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textTitulo;
        TextView textFecha;
        ImageView imgNoticia;

        public ViewHolder(View v) {
            super(v);
            textTitulo = v.findViewById(R.id.textTitulo);
            textFecha = v.findViewById(R.id.textFecha);
            imgNoticia = v.findViewById(R.id.imgNoticia);
        }
    }

    public NoticiasAdaptar(Context mContext, JSONArray values, String categoria) {
        this.contexto = mContext;
        this.values = values;
        this.categoria = categoria;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_noticia, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String message = v.getTag().toString();
                    Bundle bundl = new Bundle();
                    bundl.putString("EXTRA1", categoria);
                    bundl.putString("EXTRA2", message);
                    Fragment fragment = new FragmentSingleNoticia();
                    fragment.setArguments(bundl);
                    FragmentTransaction fragTransaction = ((FragmentActivity) contexto).getSupportFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                } catch(Exception e) {
                    Log.e(contexto.getResources().getString(R.string.app_name), contexto.getResources().getString(R.string.error_tag), e);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            ImageLoader imageLoader = ImageLoader.getInstance();
            JSONObject temp = values.getJSONObject(position);
            holder.textTitulo.setText(convertUTF8ToString(temp.getString("title")));
            String fecha = temp.getString("publicationDate").split(" ")[0].replace('-', '/');
            holder.textFecha.setText(fecha);
            imageLoader.displayImage(temp.getString("image"), holder.imgNoticia, options);
            holder.itemView.setTag(temp.toString());
        } catch(Exception e) {
            Log.e(contexto.getResources().getString(R.string.app_name), contexto.getResources().getString(R.string.error_tag), e);
        }
    }

    @Override
    public int getItemCount() {
        return values.length();
    }

    public void updateList (JSONArray items) {
        values = items;
        notifyDataSetChanged();
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
}