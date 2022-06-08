package com.example.bancodigital.deposito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Deposito;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class DepositoFormActivity extends AppCompatActivity {

    private CurrencyEditText editValor;
    private AlertDialog dialog;
    private ProgressBar progressBar;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito_form);

        recuperaUsuario();
        iniciaComponentes();

        configToolbar();
        configCliques();
    }


    private void validaDeposito() {
        double valorDeposito = (double) editValor.getRawValue() / 100;

        if (valorDeposito > 0) {
            ocultarTeclado();
            progressBar.setVisibility(View.VISIBLE);

            salvarExtrato(valorDeposito);
        } else {
            showDialog("Atenção", "Digite um valor maior que 0.");
        }
    }

    private void salvarExtrato(double valorDeposito) {
        Extrato extrato = new Extrato();
        extrato.setValor(valorDeposito);
        extrato.setOperacao("DEPOSITO");
        extrato.setTipo("ENTRADA");


        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase())
                .child(extrato.getId());
        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                extratoRef.child("data").setValue(ServerValue.TIMESTAMP);
                salvarDeposito(extrato);

            } else {
                showDialog("Atenção", "Não foi possível efetuar o depósito. Tente mais tarde.");
            }
        });


    }

    private void salvarDeposito(Extrato extrato) {

        Deposito deposito = new Deposito();
        deposito.setValor(extrato.getValor());
        deposito.setId(extrato.getId());

        DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                .child("depositos")
                .child(extrato.getId());

        depositoRef.setValue(deposito).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                depositoRef.child("data").setValue(ServerValue.TIMESTAMP);

                usuario.setSaldo(usuario.getSaldo() + extrato.getValor());
                usuario.atualizarSaldo();

                Intent intent = new Intent(this, DepositoReciboActivity.class);
                intent.putExtra("idDeposito", deposito.getId());
                startActivity(intent);
            } else {
                showDialog("Atenção", "Não foi possível efetuar o depósito. Tente mais tarde.");
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(FirebaseHelper.getIdFirebase());
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    usuario = snapshot.getValue(Usuario.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    private void configCliques() {
        findViewById(R.id.btnConfirmar).setOnClickListener(v -> validaDeposito());
    }

    private void ocultarTeclado() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                editValor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS
        );
    }

    private void iniciaComponentes() {
        progressBar = findViewById(R.id.progressBar);
        editValor = findViewById(R.id.editValor);
        editValor.setLocale(new Locale("PT", "br"));
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Depositar");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }


}