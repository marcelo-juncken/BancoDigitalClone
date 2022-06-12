package com.example.bancodigital.usuario;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.santalu.maskara.widget.MaskEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MinhaContaActivity extends AppCompatActivity {

    private static final int REQUEST_GALERIA = 100;
    private ImageView imgUser;
    private EditText editNome, editEmail;
    private MaskEditText editTelefone;
    private Button btnSalvar;
    private ProgressBar progressBar;
    Usuario usuario;

    private String caminhoImagem;
    private Bitmap imagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minha_conta);

        configToolbar();
        iniciaComponentes();
        configDados();
        configCliques();

    }

    private void configDados() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuario = (Usuario) bundle.getSerializable("usuario");
            editNome.setText(usuario.getNome());
            editTelefone.setText(usuario.getTelefone().trim());
            editEmail.setText(usuario.getEmail());
            if (usuario.getUrlImagem() != null) {
                Picasso.get()
                        .load(usuario.getUrlImagem())
                        .placeholder(R.drawable.loading)
                        .into(imgUser, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            } else {
                progressBar.setVisibility(View.GONE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void validaDados() {
        String nome = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString();

        if (!nome.isEmpty()) {
            if (telefone.replace("_","").trim().length() == 15) {
                btnSalvar.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                ocultarTeclado();

                usuario.setNome(nome);
                usuario.setTelefone(telefone);

                if (caminhoImagem != null) {
                    salvarImagemPerfil();
                } else {
                    salvarDadosUsuario();
                }
            } else {
                editTelefone.requestFocus();
                editTelefone.setError("Informe seu telefone");
            }
        } else {
            editNome.requestFocus();
            editNome.setError("Informe seu nome");
        }
    }

    private void salvarDadosUsuario() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(FirebaseHelper.getIdFirebase());
            usuarioRef.setValue(usuario).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Informações salvas com sucesso.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Não foi possível salvar as informações. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                }
                btnSalvar.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            });
        } else {
            Toast.makeText(this, "Falha de conexão com o servidor.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnSalvar.setEnabled(true);
        }
    }

    private void salvarImagemPerfil() {
        StorageReference storageReference = FirebaseHelper.getStorageReference()
                .child("imagens")
                .child("perfil")
                .child(FirebaseHelper.getIdFirebase() + ".jpeg");

        UploadTask uploadTask = storageReference.putFile(Uri.parse(caminhoImagem));
        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(task -> {

            String urlImagem = task.getResult().toString();
            usuario.setUrlImagem(urlImagem);
            salvarDadosUsuario();

        })).addOnFailureListener(e -> {

            Toast.makeText(this, "Erro no upload, tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);

        });

    }


    public void verificaPermissaoGaleria(View view) {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                abrirGaleria();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getBaseContext(), "Permissão negada.", Toast.LENGTH_SHORT).show();
            }
        };
        showDialogPermissaoGaleria(permissionListener, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
    }


    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALERIA);
    }


    private void showDialogPermissaoGaleria(PermissionListener listener, String[] permissoes) {
        TedPermission.create()
                .setPermissionListener(listener)
                .setDeniedTitle("Permissões negadas.")
                .setDeniedMessage("Você negou as permissões para acessar a galeria do dispositivo, deseja permitir?")
                .setDeniedCloseButtonText("Não")
                .setGotoSettingButtonText("Sim")
                .setPermissions(permissoes)
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                Uri localImagemSelecionada = data.getData();
                caminhoImagem = localImagemSelecionada.toString();

                try {
                    if (Build.VERSION.SDK_INT < 31) {
                        imagem = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), localImagemSelecionada);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(getBaseContext().getContentResolver(), localImagemSelecionada);
                        imagem = ImageDecoder.decodeBitmap(source);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgUser.setImageBitmap(imagem);
            }
        }
    }


    private void ocultarTeclado() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                editNome.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS
        );
    }

    private void configToolbar() {
        TextView text_titulo = findViewById(R.id.text_titulo);
        text_titulo.setText("Perfil");

        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());
    }


    private void configCliques() {
        btnSalvar.setOnClickListener(v -> validaDados());
    }

    private void iniciaComponentes() {

        imgUser = findViewById(R.id.imgUser);
        editNome = findViewById(R.id.editNome);
        editTelefone = findViewById(R.id.editTelefone);
        editEmail = findViewById(R.id.editEmail);
        btnSalvar = findViewById(R.id.btnSalvar);
        progressBar = findViewById(R.id.progressBar);

    }
}