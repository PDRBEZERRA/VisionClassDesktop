package br.com.undb.visionclass.visionclassdesktop.model;

import java.util.List;

public class User {
    private String id;
    private String nome;
    private String email;
    private String matricula;
    private UserRole role;
    private String foto;
    private String cpf;
    private List<String> turmasIds;
    private String senha;

    public User() {
    }

    public User(String id, String nome, String email, String matricula, UserRole role, String foto, String cpf, List<String> turmasIds, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.role = role;
        this.foto = foto;
        this.cpf = cpf;
        this.turmasIds = turmasIds;
        this.senha = senha;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public List<String> getTurmasIds() {
        return turmasIds;
    }

    public void setTurmasIds(List<String> turmasIds) {
        this.turmasIds = turmasIds;
    }


    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}