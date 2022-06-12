package com.example.bancodigital.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.transferencia.TransferenciaReciboActivity;

import java.util.List;

public class ExtratoAdapter extends RecyclerView.Adapter<ExtratoAdapter.MyViewHolder> {

    private final List<Extrato> extratoList;
    private OnClickListener onClickListener;
    private Context context;

    public ExtratoAdapter(List<Extrato> extratoList, OnClickListener onClickListener, Context context) {
        this.extratoList = extratoList;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_extrato, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Extrato extrato = extratoList.get(position);


        holder.textIcon.setText(extrato.getOperacao().substring(0, 1));
        holder.textOperacao.setText(extrato.getOperacao());
        holder.textData.setText(GetMask.getDate(extrato.getData(), 4));

        if (extrato.getTipo().equals("SAIDA")) {
            holder.textValor.setText(String.format("- %s", GetMask.getValor(extrato.getValor())));
            holder.textIcon.setBackgroundResource(R.drawable.bg_saida);
        } else if (extrato.getTipo().equals("ENTRADA")) {
            holder.textValor.setText(String.format("+ %s", GetMask.getValor(extrato.getValor())));
            holder.textIcon.setBackgroundResource(R.drawable.bg_entrada);
        }

        holder.itemView.setOnClickListener(v -> onClickListener.onClick(extrato));
    }

    @Override
    public int getItemCount() {
        return extratoList.size();
    }

    public interface OnClickListener{
        void onClick(Extrato extrato);
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textIcon, textOperacao, textData, textValor;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textIcon = itemView.findViewById(R.id.textIcon);
            textOperacao = itemView.findViewById(R.id.textOperacao);
            textData = itemView.findViewById(R.id.textData);
            textValor = itemView.findViewById(R.id.textValor);
        }
    }
}
