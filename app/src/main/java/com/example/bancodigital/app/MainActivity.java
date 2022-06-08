package com.example.bancodigital.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.deposito.DepositoFormActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void configDados() {
        if (usuario != null) {
            textSaldo.setText(getString(R.string.valor, GetMask.getValor(usuario.getSaldo())));
            textInfo.setText("");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void configCliques() {
        findViewById(R.id.cardDeposito).setOnClickListener(v ->
                startActivity(new Intent(this, DepositoFormActivity.class)));
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