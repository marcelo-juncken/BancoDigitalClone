package com.example.bancodigital.notificacoes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.NotificacaoAdapter;
import com.example.bancodigital.cobranca.CobrancaReciboActivity;
import com.example.bancodigital.cobranca.PagarCobrancaActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.transferencia.TransferenciaReciboActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificacoesActivity extends AppCompatActivity implements NotificacaoAdapter.OnClickListener {

    private SwipeableRecyclerView rvNotificacoes;
    private NotificacaoAdapter notificacaoAdapter;
    private final List<Notificacao> notificacaoList = new ArrayList<>();

    private TextView textInfo;
    private ProgressBar progressBar;

    private AlertDialog dialog;

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

    private void recuperaNotificacoes() {
        DatabaseReference notificacoesRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(FirebaseHelper.getIdFirebase());
        notificacoesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificacaoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Notificacao notificacao = ds.getValue(Notificacao.class);
                        notificacaoList.add(0, notificacao);
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
        notificacaoAdapter = new NotificacaoAdapter(notificacaoList, this);
        rvNotificacoes.setAdapter(notificacaoAdapter);

        rvNotificacoes.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {
                if (notificacaoList.get(position).isLida()) {
                    notificacaoList.get(position).switchNotificacao();
                } else if (!notificacaoList.get(position).isLida()) {
                    notificacaoList.get(position).switchNotificacao();
                }
                notificacaoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSwipedRight(int position) {
                showDialog("Aperte em sim para confirmar a\nremoção da notificação.", position);

            }
        });
    }

    private void showDialog(String mensagem, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this, R.style.CustomAlertDialog
        );

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_delete, null);
        builder.setView(view);

        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText("Deseja remover esta notificação?");

        TextView textMensagem = view.findViewById(R.id.textMensagem);
        textMensagem.setText(mensagem);

        view.findViewById(R.id.btnNao).setOnClickListener(v -> {
            dialog.dismiss();
            notificacaoAdapter.notifyDataSetChanged();
        });

        view.findViewById(R.id.btnSim).setOnClickListener(v -> {
            notificacaoList.get(position).deletar();
            dialog.dismiss();
        });

        dialog = builder.create();
        dialog.show();

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
        Intent intent = null;
        switch (notificacao.getOperacao()) {
            case "COBRANCA": {
                intent = new Intent(this, PagarCobrancaActivity.class);
                intent.putExtra("notificacaoSelecionada", notificacao);
                break;
            }
            case "TRANSFERENCIA": {
                intent = new Intent(this, TransferenciaReciboActivity.class);
                intent.putExtra("idTransferencia", notificacao.getIdOperacao());
                break;
            }
            case "PAGAMENTO": {
                intent = new Intent(this, CobrancaReciboActivity.class);
                intent.putExtra("idPagamento", notificacao.getIdOperacao());
                break;
            }
        }
        startActivity(intent);

    }
}