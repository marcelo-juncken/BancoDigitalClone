package com.example.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class TransferenciaFormActivity extends AppCompatActivity {

    private CurrencyEditText editValor;
    private AlertDialog dialog;

    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_form);

        configToolbar();
        iniciaComponentes();
        configCliques();
        recuperaUsuario();

    }

    private void validaValor() {
        double valorTransferencia = (double) editValor.getRawValue() / 100;

        if (valorTransferencia > 0) {
            if (usuario.getSaldo() - valorTransferencia >= 0) {
                ocultarTeclado();
                Intent intent = new Intent(this, SelecionarUsuarioActivity.class);
                intent.putExtra("valorTransferencia", valorTransferencia);
                startActivity(intent);
            } else{
                showDialog("Atenção", "Não há saldo suficiente na conta.");
            }
        } else {
            showDialog("Atenção", "O valor tem que ser maior que 0.");
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

    private void recuperaUsuario() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(FirebaseHelper.getIdFirebase());
            usuarioRef.addValueEventListener(new ValueEventListener() {
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
    }

    private void configCliques() {
        findViewById(R.id.btnContinuar).setOnClickListener(v -> validaValor());
    }

    private void ocultarTeclado() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                editValor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS
        );
    }

    private void iniciaComponentes() {
        editValor = findViewById(R.id.editValor);
        editValor.setLocale(new Locale("PT", "br"));

    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Transferência");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }
}