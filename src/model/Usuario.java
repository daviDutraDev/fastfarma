package model;

public class Usuario {
    private int id;
    private String nome;
    private String login;
    private String senha;
    private TipoUsuario tipo;

    private Usuario(int id, String nome, String login, String senha, TipoUsuario tipo){
        this.id=id;
        this.nome=nome;
        this.login=login;
        this.senha=senha;
        this.tipo=tipo;


    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }


}
