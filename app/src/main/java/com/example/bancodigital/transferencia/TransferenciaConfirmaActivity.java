package com.example.bancodigital.transferencia;

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

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.deposito.DepositoReciboActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Deposito;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Transferencia;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class TransferenciaConfirmaActivity extends AppCompatActivity {

    private static final int REQUEST_TRANSFERENCIA = 150;
    private ImageView imgUser;
    private TextView textUser, textValor;
    private Button btnConfirmar;

    private Usuario usuarioOrigem;
    private Usuario usuarioDestino;
    private double valorTransferencia;

    private AlertDialog dialog;

    private final Transferencia transferencia = new Transferencia();

    private String transferenciaOrigem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_confirma);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            valorTransferencia = (double) bundle.getSerializable("valorTransferencia");
            usuarioDestino = (Usuario) bundle.getSerializable("usuarioSelecionado");
        }
        configToolbar();
        iniciaComponentes();
        configDados();
        configCliques();
        recuperaUsuario();
    }

    private void enviaNotificacao(String idOperacao){
        Notificacao notificacao = new Notificacao();
        notificacao.setOperacao("TRANSFERENCIA");
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
        if (usuarioOrigem.getSaldo() >= valorTransferencia) {
            btnConfirmar.setEnabled(false);

            transferencia.setIdUserOrigem(usuarioOrigem.getId());
            transferencia.setIdUserDestino(usuarioDestino.getId());
            transferencia.setValor(valorTransferencia);

            salvarExtrato(usuarioOrigem, "SAIDA");
            salvarExtrato(usuarioDestino, "ENTRADA");
        } else {
            showDialog("Atenção", "Não há saldo suficiente.");
        }
    }

    private void salvarExtrato(Usuario usuario, String tipo) {
        Extrato extrato = new Extrato();
        extrato.setValor(valorTransferencia);
        extrato.setOperacao("TRANSFERENCIA");
        extrato.setTipo(tipo);

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(usuario.getId())
                .child(extrato.getId());
        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                extratoRef.child("data").setValue(ServerValue.TIMESTAMP);

                if (tipo.equals("SAIDA")) transferenciaOrigem = extrato.getId();

                salvarTransferencia(extrato);

            } else {
                showDialog("Atenção", "Erro ao salvar o extrato, contate um administrador.");
                btnConfirmar.setEnabled(true);
            }
        });


    }

    private void salvarTransferencia(Extrato extrato) {
        transferencia.setId(extrato.getId());

        DatabaseReference transferenciaRef = FirebaseHelper.getDatabaseReference()
                .child("transferencias")
                .child(extrato.getId());

        transferenciaRef.setValue(transferencia).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                transferenciaRef.child("data").setValue(ServerValue.TIMESTAMP);

                if (extrato.getTipo().equals("SAIDA")) {
                    usuarioOrigem.setSaldo(usuarioOrigem.getSaldo() - valorTransferencia);
                    usuarioOrigem.atualizarSaldo();
                } else if (extrato.getTipo().equals("ENTRADA")) {
                    usuarioDestino.setSaldo(usuarioDestino.getSaldo() + valorTransferencia);
                    usuarioDestino.atualizarSaldo();

                    enviaNotificacao(extrato.getId());

                    Intent intent = new Intent(this, TransferenciaReciboActivity.class);
                    intent.putExtra("usuarioSelecionado", usuarioDestino);
                    intent.putExtra("idTransferencia", transferenciaOrigem);
                    startActivityForResult(intent, REQUEST_TRANSFERENCIA);
                }

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
        textValor.setText(getString(R.string.valor, GetMask.getValor(valorTransferencia)));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TRANSFERENCIA) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

    }
}