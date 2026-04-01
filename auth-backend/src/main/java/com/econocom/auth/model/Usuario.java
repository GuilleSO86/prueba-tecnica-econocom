package com.econocom.auth.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * JPA entity for User. Maps the USUARIO table in the H2 database.
 */
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Invalid email format.")
    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password cannot be empty.")
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "ACTIVO", nullable = false)
    private boolean activo;

    // Default constructor (required by JPA)
    public Usuario() {}

    public Usuario(String email, String password, String nombre, boolean activo) {
        
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.activo = activo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}
