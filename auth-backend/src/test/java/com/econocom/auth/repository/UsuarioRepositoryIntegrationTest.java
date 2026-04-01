package com.econocom.auth.repository;

import com.econocom.auth.model.Usuario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final String TEST_EMAIL = "repository@test.com";
    private static final String TEST_PASSWORD = "repopass123";
    private static final String TEST_NAME = "Repository Test User";

    private Usuario createTestUsuario(String email, String password, String nombre, boolean activo) {
        
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setNombre(nombre);
        usuario.setActivo(activo);
        
        return usuario;
    }

    @Test
    public void findByEmail_ExistingUser_ReturnsUser() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        
        usuarioRepository.save(usuario);

        Optional<Usuario> result = usuarioRepository.findByEmail(TEST_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(TEST_EMAIL, result.get().getEmail());
        assertEquals(TEST_NAME, result.get().getNombre());
        assertEquals(TEST_PASSWORD, result.get().getPassword());
        assertTrue(result.get().isActivo());
    }

    @Test
    public void findByEmail_NonExistingUser_ReturnsEmpty() {
        
        String nonExistingEmail = "nonexistent@repo.com";

        Optional<Usuario> result = usuarioRepository.findByEmail(nonExistingEmail);

        assertFalse(result.isPresent());
    }

    @Test
    public void findByEmail_DifferentCase_ReturnsEmpty() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        
        usuarioRepository.save(usuario);

        Optional<Usuario> result = usuarioRepository.findByEmail(TEST_EMAIL.toUpperCase());

        assertFalse(result.isPresent());
    }

    @Test
    public void existsByEmail_ExistingUser_ReturnsTrue() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        
        usuarioRepository.save(usuario);

        boolean exists = usuarioRepository.existsByEmail(TEST_EMAIL);

        assertTrue(exists);
    }

    @Test
    public void existsByEmail_NonExistingUser_ReturnsFalse() {
        
        boolean exists = usuarioRepository.existsByEmail("nonexistent@repo.com");

        assertFalse(exists);
    }

    @Test
    public void existsByEmail_AfterDelete_ReturnsFalse() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        Usuario saved = usuarioRepository.save(usuario);
        
        assertTrue(usuarioRepository.existsByEmail(TEST_EMAIL));

        usuarioRepository.delete(saved);

        assertFalse(usuarioRepository.existsByEmail(TEST_EMAIL));
    }

    @Test
    public void save_NewUser_GeneratesId() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        
        assertNull(usuario.getId());

        Usuario saved = usuarioRepository.save(usuario);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals(TEST_EMAIL, saved.getEmail());
    }

    @Test
    public void save_UpdateExistingUser_KeepsId() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        Usuario saved = usuarioRepository.save(usuario);
        
        Long originalId = saved.getId();

        saved.setNombre("Updated Name");
        
        Usuario updated = usuarioRepository.save(saved);

        assertEquals(originalId, updated.getId());
        assertEquals("Updated Name", updated.getNombre());
    }

    @Test
    public void delete_User_RemovesFromDatabase() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        Usuario saved = usuarioRepository.save(usuario);
        
        Long id = saved.getId();

        usuarioRepository.delete(usuario);

        Optional<Usuario> result = usuarioRepository.findById(id);
        
        assertFalse(result.isPresent());
    }

    @Test
    public void deleteById_User_RemovesFromDatabase() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        Usuario saved = usuarioRepository.save(usuario);
        
        Long id = saved.getId();

        usuarioRepository.deleteById(id);

        Optional<Usuario> result = usuarioRepository.findById(id);
        
        assertFalse(result.isPresent());
    }

    @Test
    public void deleteByEmail_User_RemovesFromDatabase() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
        usuarioRepository.save(usuario);
        
        assertTrue(usuarioRepository.existsByEmail(TEST_EMAIL));

        usuarioRepository.deleteByEmail(TEST_EMAIL);

        assertFalse(usuarioRepository.existsByEmail(TEST_EMAIL));
    }

    @Test
    public void findAll_ReturnsAllUsers() {
        
        long initialCount = usuarioRepository.count();

        Usuario user1 = createTestUsuario("user1@repo.com", "pass1", "User 1", true);
        Usuario user2 = createTestUsuario("user2@repo.com", "pass2", "User 2", true);
        
        usuarioRepository.save(user1);
        usuarioRepository.save(user2);

        List<Usuario> allUsers = usuarioRepository.findAll();

        assertEquals(initialCount + 2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("user1@repo.com")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("user2@repo.com")));
    }

    @Test
    public void findAll_AfterDelete_ReturnsUpdatedList() {
        
        Usuario user1 = createTestUsuario("delete1@repo.com", "pass1", "User 1", true);
        Usuario saved = usuarioRepository.save(user1);
        
        long countBefore = usuarioRepository.count();

        usuarioRepository.deleteById(saved.getId());
        List<Usuario> allUsers = usuarioRepository.findAll();

        assertEquals(countBefore - 1, allUsers.size());
        assertFalse(allUsers.stream().anyMatch(u -> u.getEmail().equals("delete1@repo.com")));
    }
}