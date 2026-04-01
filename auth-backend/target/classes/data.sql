-- =================================
-- Sample data for the USUARIO table
-- =================================

-- Note: Passwords are in plain text (for testing purposes)
-- In production, they must be hashed

INSERT INTO USUARIO (EMAIL, PASSWORD, NOMBRE, ACTIVO) VALUES 
('admin@econocom.com', 'password123', 'Administrador', true),
('usuario@econocom.com', 'password123', 'Usuario Prueba', true),
('inactivo@econocom.com', 'password123', 'Usuario Inactivo', false),
('sso@econocom.com', 'sso-simulation-token', 'Usuario SSO', true);
