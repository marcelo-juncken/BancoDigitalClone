package com.example.bancodigital.cobranca;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Cobranca;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CobrancaConfirmaActivity extends AppCompatActivity {

    private ImageView imgUser;
    private TextView textUser, textValor;
    private Button btnConfirmar;

    private Usuario usuarioOrigem;
    private Usuario usuarioDestino;
    private double valorCobranca;

    private AlertDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranca_confirma);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            valorCobranca = (double) bundle.getSerializable("valorCobranca");
            usuarioDestino = (Usuario) bundle.getSerializable("usuarioSelecionado");
        }
        configToolbar();
        iniciaComponentes();
        configDados();
        configCliques();
        recuperaUsuario();
    }


    private void enviaNotificacao(String idOperacao) {
        Notificacao notificacao = new Notificacao();
        notificacao.setOperacao("COBRANCA");
        notificacao.setIdDestinatario(usuarioDestino.getId());
        notificacao.setIdRemetente(usuarioOrigem.getId());
        notificacao.setIdOperacao(idOperacao);
        notificacao.enviar();
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
                        usuarioOrigem = snapshot.getValue(Usuario.class);
                    } else {
                        showDialog("Atenção", "Erro com a conexão do servidor.");
                        btnConfirmar.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showDialog("Atenção", "Erro com a conexão do servidor.");
                    btnConfirmar.setEnabled(false);
                }
            });
        } else {
            showDialog("Atenção", "Erro na autenticação com o servidor. Tente novamente mais tarde");
            btnConfirmar.setEnabled(false);
        }
    }

    private void salvaInformacoes() {
        if (FirebaseHelper.getAutenticado()) {
            btnConfirmar.setEnabled(false);

            Cobranca cobranca = new Cobranca();
            cobranca.setIdRemetente(usuarioOrigem.getId());
            cobranca.setIdDestinatario(usuarioDestino.getId());
            cobranca.setValor(valorCobranca);

            salvarCobranca(cobranca);
        } else {
            showDialog("Atenção", "Falha na autenticação");
        }
    }


    private void salvarCobranca(Cobranca cobranca) {
        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference()
                .child("cobrancas")
                .child(cobranca.getIdDestinatario())
                .child(cobranca.getId());

        cobrancaRef.setValue(cobranca).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cobrancaRef.child("data").setValue(ServerValue.TIMESTAMP);

                enviaNotificacao(cobranca.getId());

                Snackbar.make(textUser,"Cobrança enviada com sucesso!", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else {
                showDialog("Atenção", "Erro ao salvar a transferência, contate um administrador");
            }

        });
    }


    private void showDialog(String titulo, String mensagem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this, R.style.CustomAlertDialog
        );

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);
        builder.setView(view);

        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText(titulo);

        TextView textMensagem = view.findViewById(R.id.textMensagem);
        textMensagem.setText(mensagem);

        Button btnOK = view.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(v -> dialog.dismiss());

        dialog = builder.create();
        dialog.show();

    }

    private void configDados() {
        textUser.setText(usuarioDestino.getNome());
        textValor.setText(getString(R.string.valor, GetMask.getValor(valorCobranca)));
        if (usuarioDestino.getUrlImagem() != null) {
            Picasso.get().load(usuarioDestino.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_user)
                    .into(imgUser);
        } else {
            imgUser.setImageResource(R.drawable.ic_user);
        }
    }

    private void configCliques() {
        btnConfirmar.setOnClickListener(v -> salvaInformacoes());
    }


    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Selecione o usuário");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        imgUser = findViewById(R.id.imgUser);
        textUser = findViewById(R.id.textUser);
        textValor = findViewById(R.id.textValor);

        btnConfirmar = findViewById(R.id.btnConfirmar);
    }

}