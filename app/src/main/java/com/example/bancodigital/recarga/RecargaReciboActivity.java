package com.example.bancodigital.recarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Recarga;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RecargaReciboActivity extends AppCompatActivity {

    private TextView textCodigo, textData, textValor, textTelefone;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga_recibo);

        configToolbar();
        iniciaComponentes();
        getRecarga();
        configCliques();
    }

    private void iniciaComponentes() {
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);
        textTelefone = findViewById(R.id.textTelefone);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getRecarga() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String idRecarga = (String) bundle.getSerializable("idRecarga");
            DatabaseReference recargaRef = FirebaseHelper.getDatabaseReference()
                    .child("recargas")
                    .child(idRecarga);
            recargaRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Recarga recarga = snapshot.getValue(Recarga.class);
                        if (recarga != null) {
                            config(recarga);
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

    private void configCliques() {
        findViewById(R.id.btnConfirmar).setOnClickListener(v -> finish());
    }

    private void config(Recarga recarga) {
        textCodigo.setText(recarga.getId());
        textData.setText(GetMask.getDate(recarga.getData(), 3));
        textValor.setText(getString(R.string.valor, GetMask.getValor(recarga.getValor())));
        textTelefone.setText(recarga.getNumero());

        progressBar.setVisibility(View.GONE);
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Comprovante");

    }
}