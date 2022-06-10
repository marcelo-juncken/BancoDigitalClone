package com.example.bancodigital.model;

import com.example.bancodigital.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

public class Notificacao {
    private String id;
    private String idRemetente;
    private String idDestinatario;
    private String idOperacao;
    private long data;
    private String operacao;
    private boolean lida = false;

    public Notificacao() {
        DatabaseReference notificacaoRef = FirebaseHelper.getDatabaseReference();
        setId(notificacaoRef.push().getKey());
    }

    public void enviar() {
        DatabaseReference notificacaoRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(getIdDestinatario())
                .child(getId());
        notificacaoRef.setValue(this).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notificacaoRef.child("data").setValue(ServerValue.TIMESTAMP);
            }
        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getIdOperacao() {
        return idOperacao;
    }

    public void setIdOperacao(String idOperacao) {
        this.idOperacao = idOperacao;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }
}
