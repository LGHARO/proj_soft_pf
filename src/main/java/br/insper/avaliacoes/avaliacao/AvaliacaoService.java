package br.insper.avaliacoes.avaliacao;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public Avaliacao cadastrarAvaliacao(Avaliacao avaliacao, String emailToken) {
        if (avaliacao.getNota() == null || avaliacao.getNota() < 0 || avaliacao.getNota() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota deve estar entre 0 e 10");
        }
        if (avaliacao.getEmailAvaliado() == null || avaliacao.getEmailAvaliado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O email do avaliado é obrigatório");
        }
        if (avaliacao.getEmailAvaliador() == null || avaliacao.getEmailAvaliador().isBlank()) {
            avaliacao.setEmailAvaliador(emailToken);
        }
        if (avaliacao.getDataAvaliacao() == null) {
            avaliacao.setDataAvaliacao(LocalDateTime.now());
        }
        return avaliacaoRepository.save(avaliacao);
    }

    public List<Avaliacao> listarAvaliacoes() {
        return avaliacaoRepository.findAll();
    }
}
