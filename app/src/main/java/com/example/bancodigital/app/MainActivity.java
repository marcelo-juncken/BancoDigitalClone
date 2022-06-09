package com.example.bancodigital.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.autenticacao.LoginActivity;
import com.example.bancodigital.deposito.DepositoFormActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Usuario;
import com.example.bancodigital.recarga.RecargaFormActivity;
import com.example.bancodigital.transferencia.TransferenciaFormActivity;
import com.example.bancodigital.usuario.MinhaContaActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private ImageView imgUser;
    private ImageButton ibNotification;
    private TextView textSaldo, textAtividades, textVerTodas, textInfo;
    private ProgressBar progressBar;

    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniciaComponentes();
        configCliques();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaDados();
    }

    private void recuperaDados() {
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
                        progressBar.setVisibility(View.GONE);
                        textInfo.setText("Não há dados sobre o usuário");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    textInfo.setText("Erro ao carregar os dados");
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            textInfo.setText("Falha na autenticação");
        }
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
        findViewById(R.id.cardDeposito).setOnClickListener(v -> comecaActivity(DepositoFormActivity.class));

        imgUser.setOnClickListener(v -> {
            if (usuario != null) {
                Intent intent = new Intent(this, MinhaContaActivity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Suas informações ainda não foram carregadas. Aguarde.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.cardRecarga).setOnClickListener(v -> comecaActivity(RecargaFormActivity.class));

        findViewById(R.id.cardTransferir).setOnClickListener(v -> comecaActivity(TransferenciaFormActivity.class));

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
        textSaldo = findViewById(R.id.textSaldo);
        textAtividades = findViewById(R.id.textAtividades);
        textVerTodas = findViewById(R.id.textVerTodas);

        textInfo = findViewById(R.id.textInfo);
        progressBar = findViewById(R.id.progressBar);


    }
}