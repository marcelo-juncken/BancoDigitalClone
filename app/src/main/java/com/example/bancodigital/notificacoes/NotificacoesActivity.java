package com.example.bancodigital.notificacoes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.NotificacaoAdapter;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Notificacao;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificacoesActivity extends AppCompatActivity implements NotificacaoAdapter.OnClickListener {

    private RecyclerView rvNotificacoes;
    private NotificacaoAdapter notificacaoAdapter;
    private final List<Notificacao> notificacaoList = new ArrayList<>();

    private TextView textInfo;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);

        configToolbar();
        iniciaComponentes();
        configToolbar();
        configRV();
        recuperaNotificacoes();
    }

    private void recuperaNotificacoes(){
        DatabaseReference notificacoesRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(FirebaseHelper.getIdFirebase());
        notificacoesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificacaoList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Notificacao notificacao = ds.getValue(Notificacao.class);
                        notificacaoList.add(0,notificacao);
                    }
                    textInfo.setText("");
                } else {
                    textInfo.setText("Não há notificações");
                }
                progressBar.setVisibility(View.GONE);
                notificacaoAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void configRV() {
        rvNotificacoes.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvNotificacoes.setHasFixedSize(true);
        notificacaoAdapter = new NotificacaoAdapter(notificacaoList,this);
        rvNotificacoes.setAdapter(notificacaoAdapter);
    }


    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Notificações");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        rvNotificacoes = findViewById(R.id.rvNotificacoes);

        textInfo = findViewById(R.id.textInfo);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(Notificacao notificacao) {

    }
}