package com.example.bancodigital.deposito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Deposito;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DepositoReciboActivity extends AppCompatActivity {

    private TextView textCodigo, textData, textValor;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito_recibo);

        configToolbar();
        iniciaComponentes();
        getDeposito();
        configCliques();
    }

    private void iniciaComponentes() {
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getDeposito() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String idDeposito = (String) bundle.getSerializable("idDeposito");
            DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                    .child("depositos")
                    .child(idDeposito);
            depositoRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Deposito deposito = snapshot.getValue(Deposito.class);
                        if (deposito != null) {
                            config(deposito);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void configCliques(){
        findViewById(R.id.btnConfirmar).setOnClickListener(v ->  finish());
    }

    private void config(Deposito deposito) {
        textCodigo.setText(deposito.getId());
        textData.setText(GetMask.getDate(deposito.getData(), 3));
        textValor.setText(getString(R.string.valor, GetMask.getValor(deposito.getValor())));
        progressBar.setVisibility(View.GONE);
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Comprovante");

    }

}