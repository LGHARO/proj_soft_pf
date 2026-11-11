package br.insper.avaliacoes.avaliacao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoControllerTest {

    @InjectMocks
    private AvaliacaoController avaliacaoController;

    @Mock
    private AvaliacaoService avaliacaoService;

    @Test
    void devePermitirCadastroParaAdmin() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliado("avaliado@empresa.com");
        avaliacao.setComentarios("Otimo trabalho");
        avaliacao.setNota(9);

        Jwt jwt = Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .claim("https://musica-insper.com/roles", List.of("ADMIN"))
                .claim("https://musica-insper.com/email", "admin@empresa.com")
                .build();

        Mockito.when(avaliacaoService.cadastrarAvaliacao(Mockito.any(), Mockito.eq("admin@empresa.com")))
                .thenReturn(avaliacao);

        Avaliacao resposta = avaliacaoController.cadastrarAvaliacao(jwt, avaliacao);
        Assertions.assertNotNull(resposta);
        Assertions.assertEquals("avaliado@empresa.com", resposta.getEmailAvaliado());
    }

    @Test
    void deveNegarCadastroParaUserComum() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliado("avaliado@empresa.com");
        avaliacao.setComentarios("Tentativa sem permissao");
        avaliacao.setNota(8);

        Jwt jwt = Jwt.withTokenValue("token-user")
                .header("alg", "none")
                .claim("https://musica-insper.com/roles", List.of("USER"))
                .claim("https://musica-insper.com/email", "user@empresa.com")
                .build();

        ResponseStatusException ex = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> avaliacaoController.cadastrarAvaliacao(jwt, avaliacao)
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void devePermitirListagemParaUserEAdmin() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliador("admin@empresa.com");
        avaliacao.setEmailAvaliado("user@empresa.com");
        avaliacao.setComentarios("Bom desempenho");
        avaliacao.setNota(10);

        Jwt jwt = Jwt.withTokenValue("token-user")
                .header("alg", "none")
                .claim("https://musica-insper.com/roles", List.of("USER"))
                .build();

        Mockito.when(avaliacaoService.listarAvaliacoes()).thenReturn(List.of(avaliacao));

        List<Avaliacao> resultado = avaliacaoController.listarAvaliacoes(jwt);
        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("user@empresa.com", resultado.get(0).getEmailAvaliado());
    }
}
