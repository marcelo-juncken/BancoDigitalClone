package com.example.bancodigital.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bancodigital.R;
import com.example.bancodigital.model.Usuario;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.MyViewHolder> {

    private final List<Usuario> usuarioList;
    private OnClickListener onClickListener;

    public UsuarioAdapter(List<Usuario> usuarioList, OnClickListener onClickListener) {
        this.usuarioList = usuarioList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_usuario, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);

        if (usuario.getUrlImagem() != null && !usuario.getUrlImagem().isEmpty()) {
            Picasso.get().load(usuario.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_user)
                    .into(holder.imageUsuario);
        } else {
            holder.imageUsuario.setImageResource(R.drawable.ic_user);
        }
        holder.textUsuario.setText(usuario.getNome());

        holder.itemView.setOnClickListener(v -> {
            onClickListener.OnClick(usuario);
        });
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public interface OnClickListener {
        void OnClick(Usuario usuario);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageUsuario;
        TextView textUsuario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUsuario = itemView.findViewById(R.id.imageUsuario);
            textUsuario = itemView.findViewById(R.id.textUsuario);
        }
    }
}

