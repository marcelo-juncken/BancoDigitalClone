package com.example.bancodigital.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.ExtratoAdapter;
import com.example.bancodigital.autenticacao.LoginActivity;
import com.example.bancodigital.deposito.DepositoFormActivity;
import com.example.bancodigital.extrato.ExtratoActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Usuario;
import com.example.bancodigital.notificacoes.NotificacoesActivity;
import com.example.bancodigital.recarga.RecargaFormActivity;
import com.example.bancodigital.transferencia.TransferenciaFormActivity;
import com.example.bancodigital.usuario.MinhaContaActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvExtrato;
    private ExtratoAdapter extratoAdapter;
    private List<Extrato> extratoList = new ArrayList<>();

    private ImageView imgUser;
    private ImageButton ibNotification;
    private TextView textNotification, textSaldo, textAtividades, textVerTodas, textInfo;
    private ProgressBar progressBar;

    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniciaComponentes();
        configCliques();
        configRV();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaDados();
    }

    private void recuperaDados() {
        recuperaUsuario();
        recuperaExtratos();
        recuperaNotificacoes();
    }

    private void recuperaNotificacoes() {
        DatabaseReference notificacoesRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(FirebaseHelper.getIdFirebase());
        notificacoesRef.orderByChild("lida").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    textNotification.setText(String.valueOf(snapshot.getChildrenCount()));
                    textNotification.setVisibility(View.VISIBLE);
                    textInfo.setText("");
                } else {
                    textInfo.setText("Não há notificações");
                    textNotification.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperaUsuario() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(FirebaseHelper.getIdFirebase());
            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuario = snapshot.getValue(Usuario.class);
                        configDados();
                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
        }
    }

    private void recuperaExtratos() {
        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase());
        extratoRef.limitToLast(6).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                extratoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Extrato extrato = ds.getValue(Extrato.class);

                        if (extrato != null) {
                            extratoList.add(0, extrato);
                        }
                    }
                    textInfo.setText("");
                } else {
                    textInfo.setText("Erro ao carregar os dados");
                }
                progressBar.setVisibility(View.GONE);
                extratoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRV() {
        rvExtrato.setLayoutManager(new LinearLayoutManager(this));
        rvExtrato.setHasFixedSize(true);
        extratoAdapter = new ExtratoAdapter(extratoList, getBaseContext());
        rvExtrato.setAdapter(extratoAdapter);
    }

    private void configDados() {
        if (usuario != null) {
            if (usuario.getUrlImagem() != null) {
                Picasso.get()
                        .load(usuario.getUrlImagem())
                        .placeholder(R.drawable.loading)
                        .into(imgUser, new Callback() {
                            @Override
                            public void onSuccess() {
                                textSaldo.setText(getString(R.string.valor, GetMask.getValor(usuario.getSaldo())));
                                textInfo.setText("");
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                textSaldo.setText(getString(R.string.valor, GetMask.getValor(usuario.getSaldo())));
                                textInfo.setText("");
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            } else {
                textSaldo.setText(getString(R.string.valor, GetMask.getValor(usuario.getSaldo())));
                textInfo.setText("");
                progressBar.setVisibility(View.GONE);
            }

        } else {
            textSaldo.setText(getString(R.string.valor, GetMask.getValor(0)));
            textInfo.setText("Erro no carregamento da página");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void configCliques() {
        imgUser.setOnClickListener(v -> {
            if (usuario != null) {
                Intent intent = new Intent(this, MinhaContaActivity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Suas informações ainda não foram carregadas. Aguarde.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.textVerTodas).setOnClickListener(v -> comecaActivity(ExtratoActivity.class));

        findViewById(R.id.cardDeposito).setOnClickListener(v -> comecaActivity(DepositoFormActivity.class));

        findViewById(R.id.cardRecarga).setOnClickListener(v -> comecaActivity(RecargaFormActivity.class));

        findViewById(R.id.cardTransferir).setOnClickListener(v -> comecaActivity(TransferenciaFormActivity.class));

        findViewById(R.id.cardExtrato).setOnClickListener(v -> comecaActivity(ExtratoActivity.class));

        findViewById(R.id.ibNotification).setOnClickListener(v -> comecaActivity(NotificacoesActivity.class));

        findViewById(R.id.cardDeslogar).setOnClickListener(v -> {

            FirebaseHelper.getAuth().signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));

        });

    }

    private void comecaActivity(Class<?> classe) {
        if (usuario != null) {
            startActivity(new Intent(this, classe));
        } else {
            Toast.makeText(this, "Suas informações ainda não foram carregadas. Aguarde.", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciaComponentes() {
        imgUser = findViewById(R.id.imgUser);
        ibNotification = findViewById(R.id.ibNotification);
        textNotification = findViewById(R.id.textNotification);
        textSaldo = findViewById(R.id.textSaldo);
        textAtividades = findViewById(R.id.textAtividades);
        textVerTodas = findViewById(R.id.textVerTodas);

        textInfo = findViewById(R.id.textInfo);
        progressBar = findViewById(R.id.progressBar);

        rvExtrato = findViewById(R.id.rvExtrato);

    }
}