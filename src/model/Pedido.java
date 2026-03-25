package model;

public class Pedido {
    private int id;
    private int codigoVerificacao;
    private String criadoPor;
    private StatusPedido status;

    public Pedido(int id, int codigoVerificacao ,String criadoPor, StatusPedido status){
        this.id=id;
        this.codigoVerificacao=codigoVerificacao;
        this.criadoPor=criadoPor;
        this.status=status;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodigoVerificacao() {
        return codigoVerificacao;
    }

    public void setCodigoVerificacao(int codigoVerificacao) {
        this.codigoVerificacao = codigoVerificacao;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}
