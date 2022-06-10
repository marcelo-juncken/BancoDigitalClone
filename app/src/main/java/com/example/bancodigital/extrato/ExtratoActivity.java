package com.example.bancodigital.extrato;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.ExtratoAdapter;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Extrato;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExtratoActivity extends AppCompatActivity {

    private RecyclerView rvExtrato;
    private ExtratoAdapter extratoAdapter;
    private final List<Extrato> extratoList = new ArrayList<>();

    private TextView textInfo;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extrato);

        configToolbar();
        iniciaComponentes();
        recuperaExtrato();
        configRV();
    }

    private void recuperaExtrato() {
        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase());
        extratoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                extratoList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Extrato extrato = ds .getValue(Extrato.class);
                        extratoList.add(0,extrato);
                    }
                    extratoAdapter.notifyDataSetChanged();
                    textInfo.setText("");
                }else{
                    textInfo.setText("Você não possui nenhuma operação feita");
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void configRV() {
        rvExtrato.setLayoutManager(new LinearLayoutManager(this));
        rvExtrato.setHasFixedSize(true);
        extratoAdapter = new ExtratoAdapter(extratoList, this);
        rvExtrato.setAdapter(extratoAdapter);
    }


    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Extrato");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        rvExtrato = findViewById(R.id.rvExtrato);

        textInfo = findViewById(R.id.textInfo);
        progressBar = findViewById(R.id.progressBar);
    }
}