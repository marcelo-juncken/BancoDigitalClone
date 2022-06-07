package com.example.bancodigital.autenticacao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.MainActivity;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText edit_email;
    private EditText edit_senha;
    private Button btn_entrar;
    private TextView text_cadastrar;
    private TextView text_recuperar_conta;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        iniciaComponentes();
        configCliques();
    }


    private void configCliques() {
        btn_entrar.setOnClickListener(v -> validaDados());
        text_cadastrar.setOnClickListener(v ->startActivity(new Intent(this, CadastroActivity.class)));
        text_recuperar_conta.setOnClickListener(v ->startActivity(new Intent(this, RecuperarContaActivity.class)));
    }

    private void validaDados() {
        String email = edit_email.getText().toString().trim();
        String senha = edit_senha.getText().toString().trim();

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                logar(email,senha);
            } else {
                edit_senha.requestFocus();
                edit_senha.setError("Informe sua senha");
            }
        } else {
            edit_email.requestFocus();
            edit_email.setError("Informe seu email");
        }

    }

    private void logar(String email, String senha) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(
                email, senha
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                finish();
                startActivity(new Intent(this, MainActivity.class));
            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void iniciaComponentes() {
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        btn_entrar = findViewById(R.id.btn_entrar);
        text_cadastrar = findViewById(R.id.text_cadastrar);
        text_recuperar_conta = findViewById(R.id.text_recuperar_conta);
        progressBar = findViewById(R.id.progressBar);

    }
}