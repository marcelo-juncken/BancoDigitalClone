package com.example.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.UsuarioAdapter;
import com.example.bancodigital.cobranca.CobrancaConfirmaActivity;
import com.example.bancodigital.cobranca.CobrancaFormActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelecionarUsuarioActivity extends AppCompatActivity implements UsuarioAdapter.OnClickListener {

    private RecyclerView rvUsuarios;
    private UsuarioAdapter usuarioAdapter;

    private final List<Usuario> usuarioListCompleta = new ArrayList<>();
    private final List<Usuario> usuarioListFiltro = new ArrayList<>();
    private final List<Usuario> usuarioListAdapter = new ArrayList<>();

    private EditText searchView;
    private ImageButton ibClose;

    private TextView textInfo;
    private ProgressBar progressBar;

    private String pesquisa = "";

    private double valorTransferencia = 0;
    private double valorCobranca = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_usuario);

        iniciaComponentes();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (getIntent().hasExtra("valorTransferencia")) {
                valorTransferencia = bundle.getDouble("valorTransferencia");
            } else if (getIntent().hasExtra("valorCobranca")) {
                valorCobranca = bundle.getDouble("valorCobranca");
            }
            configToolbar();

            configCliques();
            configRV();

            recuperaUsuarios();
            configSearchView();
        } else {
            progressBar.setVisibility(View.GONE);
            textInfo.setText("Erro no carregamento. Tente novamente mais tarde.");
        }


    }


    private void recuperaUsuarios() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios");
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usuarioListAdapter.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Usuario usuario = ds.getValue(Usuario.class);
                            if (usuario != null) {
                                if (!usuario.getId().equals(FirebaseHelper.getIdFirebase())) {
                                    usuarioListAdapter.add(usuario);
                                }
                            }

                        }
                        progressBar.setVisibility(View.GONE);
                        textInfo.setText("");
                    } else {
                        progressBar.setVisibility(View.GONE);
                        textInfo.setText("Nenhum usuário cadastrado");
                    }
                    usuarioListCompleta.addAll(usuarioListAdapter);
                    usuarioAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    textInfo.setText("Erro ao carregar os dados");
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            textInfo.setText("Falha na autenticação");
        }
    }

    private void configRV() {
        rvUsuarios.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvUsuarios.setHasFixedSize(true);
        usuarioAdapter = new UsuarioAdapter(usuarioListAdapter, this);

        rvUsuarios.setAdapter(usuarioAdapter);

    }

    private void configSearchView() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pesquisa = s.toString();
                pesquisaUsuarios();
                if (!pesquisa.isEmpty()) {
                    ibClose.setVisibility(View.VISIBLE);
                } else {
                    ibClose.setVisibility(View.GONE);
                }
            }
        });

        searchView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                ocultarTeclado();
                pesquisa = v.getText().toString();
                pesquisaUsuarios();
            }
            return false;
        });
    }

    private void pesquisaUsuarios() {
        usuarioListFiltro.clear();
        usuarioListAdapter.clear();
        for (Usuario usuario : usuarioListCompleta) {
            if (usuario.getNome().toLowerCase().contains(pesquisa.toLowerCase().trim())) {
                usuarioListFiltro.add(usuario);
            }
        }

        if (usuarioListFiltro.isEmpty()) {
            textInfo.setText("Nenhum usuário encontrado com este nome.");
        } else {
            textInfo.setText("");
        }
        progressBar.setVisibility(View.GONE);
        usuarioListAdapter.addAll(usuarioListFiltro);
        usuarioAdapter.notifyDataSetChanged();

    }


    private void limparPesquisa() {
        pesquisa = "";
        ocultarTeclado();
        searchView.setText("");
        searchView.clearFocus();
    }

    private void configCliques() {
        ibClose.setOnClickListener(v -> limparPesquisa());
    }


    private void ocultarTeclado() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                searchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS
        );
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Selecione o usuário");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        rvUsuarios = findViewById(R.id.rvUsuarios);
        searchView = findViewById(R.id.searchView);
        ibClose = findViewById(R.id.ibClose);

        textInfo = findViewById(R.id.textInfo);
        progressBar = findViewById(R.id.progressBar);

    }

    @Override
    public void OnClick(Usuario usuario) {
        Intent intent = null;

        if (valorTransferencia != 0 && valorCobranca == 0) {
            intent = new Intent(this, TransferenciaConfirmaActivity.class);
            intent.putExtra("valorTransferencia", valorTransferencia);

        } else if (valorTransferencia == 0 && valorCobranca != 0) {
            intent = new Intent(this, CobrancaConfirmaActivity.class);
            intent.putExtra("valorCobranca", valorCobranca);
        }
        if (intent != null) {
            intent.putExtra("usuarioSelecionado", usuario);
            startActivity(intent);
        }

    }
}