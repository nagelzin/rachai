package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.model.*;
import br.com.rachai.repository.*;
import br.com.rachai.security.UsuarioAtual;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GrupoService {
    private final GrupoRepository grupos;
    private final GrupoParticipanteRepository membros;
    private final UsuarioAtual atual;

    public GrupoService(
            GrupoRepository grupos, GrupoParticipanteRepository membros, UsuarioAtual atual) {
        this.grupos = grupos;
        this.membros = membros;
        this.atual = atual;
    }

    @Transactional
    public GrupoResponse criar(CriarGrupoRequest r) {
        var usuario = atual.obter();
        var grupo =
                grupos.save(
                        new Grupo(
                                r.nome().strip(),
                                r.descricao() == null ? null : r.descricao().strip(),
                                usuario));
        membros.saveAndFlush(new GrupoParticipante(grupo, usuario));
        return GrupoResponse.from(grupo);
    }

    public List<GrupoResponse> listar() {
        return grupos.listarDoUsuario(atual.obter().getId()).stream()
                .map(GrupoResponse::from)
                .toList();
    }

    public GrupoResponse consultar(Long id) {
        return GrupoResponse.from(buscar(id));
    }

    public Grupo buscar(Long id) {
        if (!membros.existsByGrupoIdAndUsuarioId(id, atual.obter().getId()))
            throw new NaoEncontradoException("Grupo não encontrado.");
        return grupos.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Grupo não encontrado."));
    }

    public Grupo administrador(Long id) {
        var grupo = buscar(id);
        if (!grupo.getResponsavel().getId().equals(atual.obter().getId()))
            throw new AccessDeniedException("Somente o administrador pode realizar esta ação.");
        return grupo;
    }

    // Exige transação com lock habilitado. Serializa mutações e a leitura consistente de saldos.
    public Grupo bloquear(Long id) {
        return grupos.bloquear(id)
                .orElseThrow(() -> new NaoEncontradoException("Grupo não encontrado."));
    }
}
