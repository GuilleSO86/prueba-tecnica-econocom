package com.econocom.auth.repository;

import com.econocom.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for the Usuario entity. It provides CRUD operations and custom queries.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Search for a user by its email address. 
     * Spring Data JPA automatically generates the implementation.
     *
     * @param email The email address of the user to search for.
     * 
     * @return An Optional containing the user if found, or empty otherwise.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Check whether a user with the specified email address exists.
     *
     * @param email The email address to be checked.
     * 
     * @return <strong>true</strong> if a user with that email address exists.
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by its email.
     * 
     * @param email
     */
    void deleteByEmail(String email);
}
