package com.example.bancodigital.autenticacao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DatabaseReference;

public class CadastroActivity extends AppCompatActivity {

    private EditText edit_nome;
    private EditText edit_email;
    private EditText edit_telefone;
    private EditText edit_senha;
    private EditText edit_confirmar_senha;
    private Button btn_criar_conta;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        iniciaComponentes();
        configCliques();
    }

    private void validaDados() {
        String nome = edit_nome.getText().toString().trim();
        String email = edit_email.getText().toString().trim();
        String telefone = edit_telefone.getText().toString().trim();
        String senha = edit_senha.getText().toString().trim();
        String confirma_senha = edit_confirmar_senha.getText().toString().trim();

        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (!senha.isEmpty()) {
                        if (!confirma_senha.isEmpty()) {
                            if (senha.equals(confirma_senha)) {
                                progressBar.setVisibility(View.VISIBLE);
                                Usuario usuario = new Usuario();
                                usuario.setNome(nome);
                                usuario.setEmail(email);
                                usuario.setTelefone(telefone);
                                usuario.setSenha(senha);
                                usuario.setSaldo(0);

                                cadastrarUsuario(usuario);
                            } else {
                                edit_confirmar_senha.requestFocus();
                                edit_senha.setError("Senhas não batem.");
                                edit_confirmar_senha.setError("Senhas não batem.");
                            }
                        } else {
                            edit_confirmar_senha.requestFocus();
                            edit_confirmar_senha.setError("Coloque a confirmação de senha.");
                        }

                    } else {
                        edit_senha.requestFocus();
                        edit_senha.setError("Coloque uma senha.");
                    }
                } else {
                    edit_telefone.requestFocus();
                    edit_telefone.setError("Preencha seu telefone.");
                }
            } else {
                edit_email.requestFocus();
                edit_email.setError("Preencha seu e-mail.");
            }
        } else {
            edit_nome.requestFocus();
            edit_nome.setError("Preencha seu nome.");
        }
    }

    private void cadastrarUsuario(Usuario usuario) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(task -> {
           if (task.isSuccessful()){

                String id = task.getResult().getUser().getUid();
                usuario.setId(id);

                salvarDadosUsuario(usuario);

           } else{
                progressBar.setVisibility(View.GONE);

               // TODO: 6/6/2022
               Toast.makeText(this,  FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
           }
        });
    }

    private void salvarDadosUsuario(Usuario usuario){
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(usuario.getId());
        usuarioRef.setValue(usuario).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                finish();
                startActivity(new Intent(this, MainActivity.class));
            }else{

                progressBar.setVisibility(View.GONE);
                // TODO: 6/6/2022
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configCliques() {
        btn_criar_conta.setOnClickListener(v -> validaDados());
    }

    private void iniciaComponentes() {
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_telefone = findViewById(R.id.edit_telefone);
        edit_senha = findViewById(R.id.edit_senha);
        edit_confirmar_senha = findViewById(R.id.edit_confirmar_senha);
        btn_criar_conta = findViewById(R.id.btn_criar_conta);
        progressBar = findViewById(R.id.progressBar);
    }
}