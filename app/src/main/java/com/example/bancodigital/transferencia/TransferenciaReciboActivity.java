package com.example.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Deposito;
import com.example.bancodigital.model.Transferencia;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class TransferenciaReciboActivity extends AppCompatActivity {

    private TextView textCodigo, textUser, textData, textValor;
    private ImageView imgUser;
    private ProgressBar progressBar;

    private Usuario usuario;
    private String idTransferencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_recibo);

        configToolbar();
        iniciaComponentes();
        configCliques();

        Intent intent = new Intent();
        setResult(RESULT_OK,intent);

        getTransferencia();

    }


    private void getTransferencia() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            idTransferencia = (String) bundle.getSerializable("idTransferencia");
            usuario = (Usuario) bundle.getSerializable("usuarioSelecionado");

            DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                    .child("transferencias")
                    .child(idTransferencia);
            depositoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Transferencia transferencia = snapshot.getValue(Transferencia.class);
                        config(transferencia);
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
        findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void config(Transferencia transferencia) {
        if (usuario.getUrlImagem() != null) {
            Picasso.get().load(usuario.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_user)
                    .into(imgUser);
        }else{
            imgUser.setImageResource(R.drawable.ic_user);
        }

        textCodigo.setText(idTransferencia);
        textUser.setText(usuario.getNome());
        textData.setText(GetMask.getDate(transferencia.getData(), 3));
        textValor.setText(getString(R.string.valor, GetMask.getValor(transferencia.getValor())));
        progressBar.setVisibility(View.GONE);
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Comprovante");

    }

    private void iniciaComponentes() {
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);
        progressBar = findViewById(R.id.progressBar);
        imgUser = findViewById(R.id.imgUser);
        textUser = findViewById(R.id.textUser);
    }
}