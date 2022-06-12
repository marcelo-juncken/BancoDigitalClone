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

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Cobranca;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Pagamento;
import com.example.bancodigital.model.Transferencia;
import com.example.bancodigital.model.Usuario;
import com.example.bancodigital.transferencia.TransferenciaReciboActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PagarCobrancaActivity extends AppCompatActivity {

    private static final int REQUEST_PAGAMENTO = 150;

    private ImageView imgUser;
    private TextView textUser, textValor, textData;
    private Button btnConfirmar;

    private Usuario usuarioOrigem;
    private Usuario usuarioDestino;
    private double valorCobranca;

    private Notificacao notificacao;

    private AlertDialog dialog;

    private Cobranca cobranca;

    private String pagamentoOrigem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar_cobranca);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            notificacao = (Notificacao) bundle.getSerializable("notificacaoSelecionada");

            configToolbar();
            iniciaComponentes();
            configCliques();

            recuperaDados();
        }

    }

    private void recuperaDados() {
        recuperaUsuarioOrigem();
        recuperaUsuarioDestino();
        recuperaCobranca();
    }


    private void enviaNotificacao(String idOperacao) {
        Notificacao notificacao = new Notificacao();
        notificacao.setOperacao("PAGAMENTO");
        notificacao.setIdDestinatario(usuarioDestino.getId());
        notificacao.setIdRemetente(usuarioOrigem.getId());
        notificacao.setIdOperacao(idOperacao);
        notificacao.enviar();
    }

    private void salvaInformacoes() {
        if (FirebaseHelper.getAutenticado()) {
            if (usuarioOrigem != null && usuarioDestino != null) {
                if (!cobranca.isPaga()) {
                    if (usuarioOrigem.getSaldo() >= valorCobranca) {
                        salvaDados();
                        btnConfirmar.setEnabled(false);
                    } else {
                        showDialog("Atenção", "Não há saldo suficiente.");
                    }
                } else {
                    showDialog("Atenção", "Esse pagamento já foi efetuado");
                }
            } else {
                showDialog("Atenção", "Falha na autenticação");
            }
        }
    }

    private void salvaDados() {
        salvarExtrato(usuarioOrigem, "SAIDA");
        salvarExtrato(usuarioDestino, "ENTRADA");
    }

    private void salvarExtrato(Usuario usuario, String tipo) {
        Extrato extrato = new Extrato();
        extrato.setValor(valorCobranca);
        extrato.setOperacao("PAGAMENTO");
        extrato.setTipo(tipo);

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(usuario.getId())
                .child(extrato.getId());
        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                extratoRef.child("data").setValue(ServerValue.TIMESTAMP);

                if (tipo.equals("SAIDA")) pagamentoOrigem = extrato.getId();

                salvarPagamento(extrato);

            } else {
                showDialog("Atenção", "Erro ao salvar o extrato, contate um administrador.");
                btnConfirmar.setEnabled(true);
            }
        });


    }

    private void salvarPagamento(Extrato extrato) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(extrato.getId());
        pagamento.setValor(valorCobranca);
        pagamento.setIdUserOrigem(FirebaseHelper.getIdFirebase());
        pagamento.setIdUserDestino(usuarioDestino.getId());
        pagamento.setIdCobranca(cobranca.getId());

        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference()
                .child("pagamentos")
                .child(extrato.getId());
        pagamentoRef.setValue(pagamento).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                pagamentoRef.child("data").setValue(ServerValue.TIMESTAMP);

                if (extrato.getTipo().equals("SAIDA")) {
                    usuarioOrigem.setSaldo(usuarioOrigem.getSaldo() - valorCobranca);
                    usuarioOrigem.atualizarSaldo();
                    cobranca.setPaga(true);
                    cobranca.salvar();
                } else if (extrato.getTipo().equals("ENTRADA")) {
                    usuarioDestino.setSaldo(usuarioDestino.getSaldo() + valorCobranca);
                    usuarioDestino.atualizarSaldo();

                    enviaNotificacao(extrato.getId());

                    Intent intent = new Intent(this, CobrancaReciboActivity.class);
                    intent.putExtra("usuarioSelecionado", usuarioDestino);
                    intent.putExtra("idPagamento", pagamentoOrigem);
                    startActivityForResult(intent, REQUEST_PAGAMENTO);
                }

            } else {
                showDialog("Atenção", "Erro ao salvar o extrato, contate um administrador.");
                btnConfirmar.setEnabled(true);
            }
        });
    }

    private void recuperaUsuarioOrigem() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(FirebaseHelper.getIdFirebase());
            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuarioOrigem = snapshot.getValue(Usuario.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showDialog("Atenção", "Erro com a conexão do servidor.");
                }
            });
        } else {
            showDialog("Atenção", "Erro na autenticação com o servidor. Tente novamente mais tarde");
        }
    }

    private void recuperaUsuarioDestino() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(notificacao.getIdRemetente());
            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuarioDestino = snapshot.getValue(Usuario.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showDialog("Atenção", "Erro com a conexão do servidor.");
                }
            });
        } else {
            showDialog("Atenção", "Erro na autenticação com o servidor. Tente novamente mais tarde");
        }
    }

    private void recuperaCobranca() {
        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference()
                .child("cobrancas")
                .child(FirebaseHelper.getIdFirebase())
                .child(notificacao.getIdOperacao());
        cobrancaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    cobranca = snapshot.getValue(Cobranca.class);
                    if (cobranca != null) {
                        configDados(cobranca);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configDados(Cobranca cobranca) {
        textUser.setText(usuarioDestino.getNome());

        valorCobranca = cobranca.getValor();
        textValor.setText(getString(R.string.valor, GetMask.getValor(valorCobranca)));

        textData.setText(GetMask.getDate(cobranca.getData(), 1));

        if (usuarioDestino.getUrlImagem() != null) {
            Picasso.get().load(usuarioDestino.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_user)
                    .into(imgUser);
        } else {
            imgUser.setImageResource(R.drawable.ic_user);
        }
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

    private void configCliques() {
        btnConfirmar.setOnClickListener(v -> salvaInformacoes());
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Pagar cobrança");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        imgUser = findViewById(R.id.imgUser);
        textUser = findViewById(R.id.textUser);
        textValor = findViewById(R.id.textValor);
        textData = findViewById(R.id.textData);

        btnConfirmar = findViewById(R.id.btnConfirmar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PAGAMENTO) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

    }

}