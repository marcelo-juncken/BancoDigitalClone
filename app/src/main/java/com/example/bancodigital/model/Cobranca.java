package com.example.bancodigital.model;

import com.example.bancodigital.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

public class Cobranca {

    private String id;
    private String idRemetente;
    private String idDestinatario;
    private double valor;
    private long data;
    private boolean paga = false;

    public Cobranca() {
        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference();
        setId(cobrancaRef.push().getKey());
    }

    public void salvar() {
        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference()
                .child("cobrancas")
                .child(getIdDestinatario())
                .child(getId())
                .child("paga");
        cobrancaRef.setValue(true);
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

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public boolean isPaga() {
        return paga;
    }

    public void setPaga(boolean paga) {
        this.paga = paga;
    }


}
