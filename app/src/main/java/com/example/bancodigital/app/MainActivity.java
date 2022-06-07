package com.example.bancodigital.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.bancodigital.R;
import com.example.bancodigital.deposito.DepositoFormActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configCliques();
    }

    private void configCliques() {
        findViewById(R.id.cardDeposito).setOnClickListener(v ->
                startActivity(new Intent(this, DepositoFormActivity.class)));
    }
}