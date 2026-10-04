package ies.belgrano.turnos.security;

import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTimeMs;

    public JwtService(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.expiration:86400000}") long expirationTimeMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        this.expirationTimeMs = expirationTimeMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationTimeMs);

        var builder = Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId", usuario.getId())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(expiracion);

        if (usuario.getPaciente() != null) {
            builder.claim("pacienteId", usuario.getPaciente().getId());
        }
        if (usuario.getProfesional() != null) {
            builder.claim("profesionalId", usuario.getProfesional().getId());
        }

        return builder.signWith(secretKey).compact();
    }

    public Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    public Long extraerUsuarioId(String token) {
        Number id = obtenerClaims(token).get("usuarioId", Number.class);
        return id != null ? id.longValue() : null;
    }

    public Rol extraerRol(String token) {
        String rolStr = obtenerClaims(token).get("rol", String.class);
        return Rol.valueOf(rolStr);
    }

    public Long extraerPacienteId(String token) {
        Number id = obtenerClaims(token).get("pacienteId", Number.class);
        return id != null ? id.longValue() : null;
    }

    public Long extraerProfesionalId(String token) {
        Number id = obtenerClaims(token).get("profesionalId", Number.class);
        return id != null ? id.longValue() : null;
    }

    public boolean validarToken(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
