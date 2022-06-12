package com.example.bancodigital.autenticacao;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;

public class RecuperarContaActivity extends AppCompatActivity {
    
    private EditText edit_email;
    private Button btn_recuperar_conta;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_conta);
        
        iniciaComponentes();
        configCliques();
    }

  
    private void validaDados() {
        String email = edit_email.getText().toString().trim();
        
        if (!email.isEmpty()){
            ocultarTeclado();
            progressBar.setVisibility(View.VISIBLE);
            recuperaConta(email);
        }else{
            edit_email.requestFocus();
            edit_email.setError("Informe seu email");
        }
    }

    private void recuperaConta(String email) {
        FirebaseHelper.getAuth().sendPasswordResetEmail(
                email
                ).addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       Toast.makeText(this, "Email enviado", Toast.LENGTH_SHORT).show();
                   }else{
                       Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                   }
            progressBar.setVisibility(View.GONE);
                });
    }

    private void ocultarTeclado() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                edit_email.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS
        );
    }

    private void configCliques() {
        btn_recuperar_conta.setOnClickListener(v -> validaDados());
    }

    private void iniciaComponentes() {
        edit_email = findViewById(R.id.edit_email);
        btn_recuperar_conta = findViewById(R.id.btn_recuperar_conta);
        progressBar = findViewById(R.id.progressBar);
    }

}