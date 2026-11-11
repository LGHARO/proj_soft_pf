package br.insper.avaliacoes.avaliacao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTest {

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Test
    void deveCadastrarAvaliacaoComSucesso() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliado("funcionario@empresa.com");
        avaliacao.setComentarios("Excelente entrega no projeto!");
        avaliacao.setNota(9);

        Mockito.when(avaliacaoRepository.save(Mockito.any(Avaliacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Avaliacao salvo = avaliacaoService.cadastrarAvaliacao(avaliacao, "admin@empresa.com");

        Assertions.assertEquals("admin@empresa.com", salvo.getEmailAvaliador());
        Assertions.assertEquals("funcionario@empresa.com", salvo.getEmailAvaliado());
        Assertions.assertEquals(9, salvo.getNota());
        Assertions.assertNotNull(salvo.getDataAvaliacao());
    }

    @Test
    void deveLancarErroQuandoNotaForInvalida() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliado("funcionario@empresa.com");
        avaliacao.setNota(11);

        Assertions.assertThrows(ResponseStatusException.class, () ->
                avaliacaoService.cadastrarAvaliacao(avaliacao, "admin@empresa.com")
        );
    }

    @Test
    void deveListarAvaliacoes() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliador("admin@empresa.com");
        avaliacao.setEmailAvaliado("colab@empresa.com");
        avaliacao.setNota(10);

        Mockito.when(avaliacaoRepository.findAll()).thenReturn(List.of(avaliacao));

        List<Avaliacao> resultado = avaliacaoService.listarAvaliacoes();
        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals(10, resultado.get(0).getNota());
    }
}
