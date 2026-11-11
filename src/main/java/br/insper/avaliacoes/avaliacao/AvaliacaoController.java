package br.insper.avaliacoes.avaliacao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/avaliacoes", "/api/avaliacoes"})
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Avaliacao cadastrarAvaliacao(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Avaliacao avaliacao
    ) {
        List<String> roles = extractRoles(jwt);
        boolean isAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: apenas usuários com perfil ADMIN podem cadastrar avaliações."
            );
        }

        String emailToken = extractEmail(jwt);
        return avaliacaoService.cadastrarAvaliacao(avaliacao, emailToken);
    }

    @GetMapping
    public List<Avaliacao> listarAvaliacoes(@AuthenticationPrincipal Jwt jwt) {
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
        return avaliacaoService.listarAvaliacoes();
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
