package com.example.bancodigital.cobranca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Pagamento;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CobrancaReciboActivity extends AppCompatActivity {

    private TextView textCodigo, textUser, textData, textValor,textTipoPagamento, textCorpoPagamento, textTituloPagamento;
    private ImageView imgUser;
    private ProgressBar progressBar;

    private Usuario usuario;
    private String idPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranca_recibo);


        configToolbar();
        iniciaComponentes();
        configCliques();

        Intent intent = new Intent();
        setResult(RESULT_OK,intent);

        getPagamento();

    }


    private void getPagamento() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            idPagamento = (String) bundle.getSerializable("idPagamento");

            DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                    .child("pagamentos")
                    .child(idPagamento);
            depositoRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Pagamento pagamento = snapshot.getValue(Pagamento.class);
                        if (pagamento != null) {
                            if (pagamento.getIdUserDestino().equals(FirebaseHelper.getIdFirebase())) {
                                recuperaUsuario(pagamento, pagamento.getIdUserOrigem());
                            } else {
                                recuperaUsuario(pagamento,pagamento.getIdUserDestino());
                            }
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

    private void recuperaUsuario(Pagamento pagamento, String idUsuario) {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(idUsuario);
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuario = snapshot.getValue(Usuario.class);
                        config(pagamento);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void configCliques() {
        findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void config(Pagamento pagamento) {
        if (usuario.getUrlImagem() != null) {
            Picasso.get().load(usuario.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_user)
                    .into(imgUser);
        }else{
            imgUser.setImageResource(R.drawable.ic_user);
        }

        textCodigo.setText(idPagamento);
        textUser.setText(usuario.getNome());
        textData.setText(GetMask.getDate(pagamento.getData(), 3));
        textValor.setText(getString(R.string.valor, GetMask.getValor(pagamento.getValor())));
        progressBar.setVisibility(View.GONE);

        if (pagamento.getIdUserDestino().equals(FirebaseHelper.getIdFirebase())){
            textTituloPagamento.setText("Pagamento recebido\n com sucesso!");
            textCorpoPagamento.setText("A previsão para que o dinheiro entre na sua conta é de até 30 minutos.");
            textTipoPagamento.setText("Pagamento recebido de:");
        }else{
            textTituloPagamento.setText("Pagamento efetuado\n com sucesso!");
            textCorpoPagamento.setText("Pagamento realizado com sucesso. A previsão de entrada na conta de destino é de até 30 minutos.");
            textTipoPagamento.setText("Receberá o pagamento:");
        }
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
        textTipoPagamento = findViewById(R.id.textTipoPagamento);
        textCorpoPagamento = findViewById(R.id.textCorpoPagamento);
        textTituloPagamento = findViewById(R.id.textTituloPagamento);
    }
}