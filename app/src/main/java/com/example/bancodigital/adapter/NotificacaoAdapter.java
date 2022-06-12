package com.example.bancodigital.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.MyViewHolder> {

    private List<Notificacao> notificacaoList;
    private OnClickListener onClickListener;

    public NotificacaoAdapter(List<Notificacao> notificacaoList, OnClickListener onClickListener) {
        this.notificacaoList = notificacaoList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_notificacao, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Notificacao notificacao = notificacaoList.get(position);

        String titulo = "";
        switch (notificacao.getOperacao()) {
            case "COBRANCA":
                titulo = "Você recebeu uma cobrança.";
                break;
            case "TRANSFERENCIA":
                titulo = "Você recebeu uma transferência.";
                break;
            case "PAGAMENTO":
                titulo = "Você recebeu um pagamento.";
                break;

        }
        holder.textTitulo.setText(titulo);
        holder.textData.setText(GetMask.getDate(notificacao.getData(), 3));

        if (notificacao.isLida()) {
            holder.textTitulo.setTypeface(null, Typeface.NORMAL);
            holder.textData.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.textTitulo.setTypeface(null, Typeface.BOLD);
            holder.textData.setTypeface(null, Typeface.BOLD);
        }

        recuperaUsuario(notificacao, holder);

        holder.itemView.setOnClickListener(v -> onClickListener.onClick(notificacao));
    }

    @Override
    public int getItemCount() {
        return notificacaoList.size();
    }

    public interface OnClickListener {
        void onClick(Notificacao notificacao);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textData, textEmitente;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitulo = itemView.findViewById(R.id.textTitulo);
            textData = itemView.findViewById(R.id.textData);
            textEmitente = itemView.findViewById(R.id.textEmitente);
        }
    }


    private void recuperaUsuario(Notificacao notificacao, MyViewHolder holder) {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(notificacao.getIdRemetente());
            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            String terminacao = notificacao.getOperacao().substring(notificacao.getOperacao().length() - 1).toLowerCase();
                            holder.textEmitente.setText(String.format("Enviad%s por %s", terminacao ,usuario.getNome()));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

}


