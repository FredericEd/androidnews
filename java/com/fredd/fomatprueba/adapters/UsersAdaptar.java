package com.fredd.fomatprueba.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fredd.fomatprueba.FragmentSingleNoticia;
import com.fredd.fomatprueba.FragmentUsers;
import com.fredd.fomatprueba.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

public class UsersAdaptar extends RecyclerView.Adapter<UsersAdaptar.ViewHolder> {

    JSONArray values;
    Context contexto;
    final int tipo;
    FragmentUsers fragment;

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_user)
            .showImageOnFail(R.drawable.ic_user)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textNombre;
        TextView textEmail;
        TextView textNacimiento;
        ImageView imgUser;
        ImageView imgAdd;
        ImageView imgDelete;

        public ViewHolder(View v) {
            super(v);
            textNombre = v.findViewById(R.id.textNombre);
            textEmail = v.findViewById(R.id.textEmail);
            textNacimiento = v.findViewById(R.id.textNacimiento);
            imgUser = v.findViewById(R.id.imgUser);
            imgAdd = v.findViewById(R.id.imgAdd);
            imgDelete = v.findViewById(R.id.imgDelete);
        }
    }

    public UsersAdaptar(Context mContext, JSONArray values, int tipo, FragmentUsers fragment) {
        this.contexto = mContext;
        this.values = values;
        this.tipo = tipo;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.imgAdd.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.DARKEN);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            //clear the overlay
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            JSONObject usuario = new JSONObject(v.getTag().toString());
                            fragment.addUser(usuario.getString("id_usuario"));
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        holder.imgDelete.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.DARKEN);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            //clear the overlay
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            JSONObject usuario = new JSONObject(v.getTag().toString());
                            fragment.deleteUser(usuario.getString("id_usuario"));
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            ImageLoader imageLoader = ImageLoader.getInstance();
            JSONObject temp = values.getJSONObject(position);
            holder.textNombre.setText(temp.getString("nombre"));
            holder.textEmail.setText(temp.getString("email"));
            holder.textNacimiento.setText(temp.getString("nacimiento").equals("") ? "N/A" : temp.getString("nacimiento"));
            if (!temp.getString("foto").equals(""))
            imageLoader.displayImage(temp.getString("foto"), holder.imgUser, options);
            holder.imgAdd.setVisibility(View.GONE);
            holder.imgDelete.setVisibility(View.GONE);
            if (tipo == 1)
                holder.imgAdd.setVisibility(View.VISIBLE);
            else if (tipo == 2)
                holder.imgDelete.setVisibility(View.VISIBLE);
            holder.imgAdd.setTag(temp.toString());
            holder.imgDelete.setTag(temp.toString());
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
}