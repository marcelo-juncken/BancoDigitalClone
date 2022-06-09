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

    private Transferencia transferencia;

    private AlertDialog dialog;


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

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void salvarExtratoUsuarioOrigem() {
        Extrato extratoUsuarioOrigem = new Extrato();
        extratoUsuarioOrigem.setValor(valorTransferencia);
        extratoUsuarioOrigem.setOperacao("TRANSFERENCIA");
        extratoUsuarioOrigem.setTipo("SAIDA");
        String idTransferencia = extratoUsuarioOrigem.getId();

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase())
                .child(idTransferencia);
        extratoRef.setValue(extratoUsuarioOrigem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                extratoRef.child("data").setValue(ServerValue.TIMESTAMP);
                salvarTransferencia(idTransferencia);

            } else {
                showDialog("Atenção", "Não foi possível efetuar a transferência. Tente mais tarde.");
                btnConfirmar.setEnabled(true);
            }
        });


    }

    private void salvarTransferencia(String idTransferencia) {

        transferencia = new Transferencia();
        transferencia.setValor(valorTransferencia);
        transferencia.setIdUserOrigem(usuarioOrigem.getId());
        transferencia.setIdUserDestino(usuarioDestino.getId());
        transferencia.setId(idTransferencia);

        DatabaseReference transferenciaRef = FirebaseHelper.getDatabaseReference()
                .child("transferencias")
                .child(idTransferencia);

        transferenciaRef.setValue(transferencia).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                transferenciaRef.child("data").setValue(ServerValue.TIMESTAMP);

                usuarioOrigem.setSaldo(usuarioOrigem.getSaldo() - valorTransferencia);
                usuarioOrigem.atualizarSaldo();

                salvarExtratoUsuarioDestino(idTransferencia);

            } else {
                showDialog("Atenção", "Não foi possível efetuar a transferência. Tente mais tarde.");
            }

        });
    }

    private void salvarExtratoUsuarioDestino(String idTransferencia) {
        Extrato extratoUsuarioDestino = new Extrato();
        extratoUsuarioDestino.setValor(valorTransferencia);
        extratoUsuarioDestino.setOperacao("TRANSFERENCIA");
        extratoUsuarioDestino.setTipo("ENTRADA");
        extratoUsuarioDestino.setId(idTransferencia);


        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(usuarioDestino.getId())
                .child(idTransferencia);
        extratoRef.setValue(extratoUsuarioDestino).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                extratoRef.child("data").setValue(ServerValue.TIMESTAMP);


                usuarioDestino.setSaldo(usuarioDestino.getSaldo() + valorTransferencia);
                usuarioDestino.atualizarSaldo();


                Intent intent = new Intent(this, TransferenciaReciboActivity.class);
                intent.putExtra("usuarioSelecionado", usuarioDestino);
                intent.putExtra("idTransferencia", idTransferencia);
                startActivityForResult(intent, REQUEST_TRANSFERENCIA);
                btnConfirmar.setEnabled(true);

            } else {
                showDialog("Atenção", "Não foi possível efetuar a transferência. Tente mais tarde.");
                btnConfirmar.setEnabled(true);
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
        btnConfirmar.setOnClickListener(v -> {
            if (usuarioOrigem.getSaldo() - valorTransferencia >= 0) {
                btnConfirmar.setEnabled(false);
                salvarExtratoUsuarioOrigem();
            }
        });
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
        if (resultCode == RESULT_OK){
            if(requestCode == REQUEST_TRANSFERENCIA){
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

    }
}