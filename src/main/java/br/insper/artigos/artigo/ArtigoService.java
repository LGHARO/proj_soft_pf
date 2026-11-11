package br.insper.artigos.artigo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArtigoService {

    private final ArtigoRepository artigoRepository;

    public ArtigoService(ArtigoRepository artigoRepository) {
        this.artigoRepository = artigoRepository;
    }

    public Artigo cadastrarArtigo(Artigo artigo, String emailToken) {
        if (artigo.getPrioridade() == null || artigo.getPrioridade() < 0 || artigo.getPrioridade() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota deve estar entre 0 e 10");
        }
        if (artigo.getEmail() == null || artigo.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O email do avaliado é obrigatório");
        }
        if (artigo.getEmail() == null || artigo.getEmail().isBlank()) {
            artigo.setEmail(emailToken);
        }

        return artigoRepository.save(artigo);
    }

    public Artigo buscarPorId(Long id) {
        return artigoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artigo não encontrado(a)"));
    }

    public void deletarArtigo(Long id) {
        buscarPorId(id);
        artigoRepository.deleteById(id);
    }



    public List<Artigo> listarArtigos() {
        return artigoRepository.findAll();
    }
}
