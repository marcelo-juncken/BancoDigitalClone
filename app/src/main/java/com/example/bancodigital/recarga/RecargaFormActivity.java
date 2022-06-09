package com.example.bancodigital.recarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Recarga;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;

import java.util.Locale;

public class RecargaFormActivity extends AppCompatActivity {

    private CurrencyEditText editValor;
    private MaskEditText editTelefone;
    private AlertDialog dialog;
    private ProgressBar progressBar;

    private Usuario usuario;

    private String telefone;

    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga_form);

        iniciaComponentes();
        configToolbar();
        configCliques();
        recuperaUsuario();
    }


    private void validaRecarga() {
        double valorRecarga = (double) editValor.getRawValue() / 100;
        telefone = editTelefone.getMasked();

        if (valorRecarga >= 15) {
            if (editTelefone.isDone()) {
                ocultarTeclado();
                if (usuario != null) {
                    if (usuario.getSaldo() - valorRecarga >= 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        btnConfirmar.setEnabled(false);
                        salvarExtrato(valorRecarga);
                    } else {
                        showDialog("Atenção", "Não há saldo suficiente.");
                    }
                } else {
                    showDialog("Atenção", "Usuário não reconhecido. Tente novamente mais tarde.");
                }
            } else {
                showDialog("Atenção", "Insira o número para a recarga");
            }
        } else {
            showDialog("Atenção", "O valor mínimo para realizar a recarga é de R$ 15,00");
        }
    }

    private void salvarExtrato(double valorRecarga) {
        Extrato extrato = new Extrato();
        extrato.setValor(valorRecarga);
        extrato.setOperacao("RECARGA");
        extrato.setTipo("SAIDA");

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase())
                .child(extrato.getId());
        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                extratoRef.child("data").setValue(ServerValue.TIMESTAMP);
                salvarRecarga(extrato);
            } else {
                btnConfirmar.setEnabled(false);
                showDialog("Atenção", "Não foi possível efetuar a recarga. Tente mais tarde.");
            }
        });


    }

    private void salvarRecarga(Extrato extrato) {

        Recarga recarga = new Recarga();
        recarga.setValor(extrato.getValor());
        recarga.setId(extrato.getId());
        recarga.setNumero(telefone);

        DatabaseReference recargaRef = FirebaseHelper.getDatabaseReference()
                .child("recargas")
                .child(extrato.getId());

        recargaRef.setValue(recarga).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                recargaRef.child("data").setValue(ServerValue.TIMESTAMP);

                usuario.setSaldo(usuario.getSaldo() - extrato.getValor());
                usuario.atualizarSaldo();

                Intent intent = new Intent(this, RecargaReciboActivity.class);
                intent.putExtra("idRecarga", recarga.getId());
                finish();
                startActivity(intent);
            } else {
                showDialog("Atenção", "Não foi possível efetuar a recarga. Tente mais tarde.");
                btnConfirmar.setEnabled(true);
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void recuperaUsuario() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(FirebaseHelper.getIdFirebase());
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuario = snapshot.getValue(Usuario.class);
                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
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
        btnConfirmar.setOnClickListener(v -> validaRecarga());
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
        editTelefone = findViewById(R.id.editTelefone);
        btnConfirmar = findViewById(R.id.btnConfirmar);
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Recarga");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }
}