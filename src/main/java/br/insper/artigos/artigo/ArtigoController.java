package br.insper.artigos.artigo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/artigos", "/api/artigos"})
public class ArtigoController {

    private final ArtigoService artigoService;

    public ArtigoController(ArtigoService artigoService) {
        this.artigoService = artigoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Artigo cadastrarArtigo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Artigo artigo
    ) {
        List<String> roles = extractRoles(jwt);
        boolean isAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: apenas usuários com perfil ADMIN podem cadastrar artigos."
            );
        }

        String emailToken = extractEmail(jwt);
        return artigoService.cadastrarArtigo(artigo, emailToken);
    }

    @GetMapping
    public List<Artigo> listarArtigos(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = extractRoles(jwt);
        // Aceita usuários ADMIN e USER (caso haja roles no token, valida se possui ADMIN ou USER)
        if (!roles.isEmpty()) {
            boolean isAllowed = roles.stream()
                    .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("USER"));
            if (!isAllowed) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Acesso negado: necessário perfil ADMIN ou USER."
                );
            }
        }
        return artigoService.listarArtigos();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void deletarArtigo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        List<String> roles = extractRoles(jwt);
        boolean isAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: apenas usuários com perfil ADMIN podem deletar artigos."
            );
        }

        String emailToken = extractEmail(jwt);
        artigoService.deletarArtigo(id);
    }

    private List<String> extractRoles(Jwt jwt) {
        List<String> roles = new ArrayList<>();
        if (jwt == null) {
            return roles;
        }
        List<String> defaultInsperRoles = jwt.getClaimAsStringList("https://musica-insper.com/roles");
        if (defaultInsperRoles != null) {
            roles.addAll(defaultInsperRoles);
        }
        for (Map.Entry<String, Object> entry : jwt.getClaims().entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.endsWith("/roles") || key.equals("roles") || key.equals("permissions")) {
                List<String> claimRoles = jwt.getClaimAsStringList(entry.getKey());
                if (claimRoles != null) {
                    for (String role : claimRoles) {
                        if (!roles.contains(role)) {
                            roles.add(role);
                        }
                    }
                }
            }
        }
        return roles;
    }

    private String extractEmail(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        String email = jwt.getClaimAsString("https://musica-insper.com/email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        for (Map.Entry<String, Object> entry : jwt.getClaims().entrySet()) {
            if (entry.getKey().toLowerCase().endsWith("/email") && entry.getValue() instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return jwt.getSubject();
    }
}
