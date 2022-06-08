package com.example.bancodigital.deposito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
    }

    private void getDeposito() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String idDeposito = (String) bundle.getSerializable("idDeposito");
            DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                    .child("depositos")
                    .child(idDeposito);
            depositoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Deposito deposito = snapshot.getValue(Deposito.class);
                        config(deposito);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private void configCliques(){
        findViewById(R.id.btnConfirmar).setOnClickListener(v ->  finish());
    }

    private void config(Deposito deposito) {
        textCodigo.setText(deposito.getId());
        textData.setText(GetMask.getDate(deposito.getData(), 3));
        textValor.setText(getString(R.string.valor, GetMask.getValor(deposito.getValor())));
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Comprovante");

    }

}