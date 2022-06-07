package com.example.bancodigital.deposito;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bancodigital.R;

public class DepositoFormActivity extends AppCompatActivity {

    private EditText editValor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito_form);

        configToolbar();
        configCliques();
    }

    private void configCliques() {

    }

    private void configToolbar(){
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Depositar");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }


}