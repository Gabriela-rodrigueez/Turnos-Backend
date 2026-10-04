package ies.belgrano.turnos.security;

import ies.belgrano.turnos.model.Rol;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtService.validarToken(token)) {
                    Claims claims = jwtService.obtenerClaims(token);
                    Rol rolUsuario = jwtService.extraerRol(token);
                    Long usuarioId = jwtService.extraerUsuarioId(token);
                    Long pacienteId = jwtService.extraerPacienteId(token);
                    Long profesionalId = jwtService.extraerProfesionalId(token);

                    if (rolUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        AuthenticatedUser userPrincipal = new AuthenticatedUser(
                                usuarioId,
                                claims.getSubject(),
                                rolUsuario,
                                pacienteId,
                                profesionalId
                        );

                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(rolUsuario.name());

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                List.of(authority)
                        );

                        request.setAttribute("usuarioEmail", claims.getSubject());
                        request.setAttribute("usuarioRol", rolUsuario);
                        request.setAttribute("usuarioId", usuarioId);

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Si el token es inválido o expira, se limpia el contexto de seguridad
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
