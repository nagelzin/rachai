package br.com.rachai.service;

import br.com.rachai.dto.DespesaResponse;
import br.com.rachai.exception.*;
import br.com.rachai.model.Despesa;
import br.com.rachai.repository.DespesaRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@Service
public class ComprovanteService {
    private final GrupoService grupos;
    private final DespesaRepository despesas;
    private final JdbcTemplate jdbc;

    public ComprovanteService(GrupoService grupos, DespesaRepository despesas, JdbcTemplate jdbc) {
        this.grupos = grupos;
        this.despesas = despesas;
        this.jdbc = jdbc;
    }

    private Despesa buscar(Long grupoId, Long id) {
        grupos.buscar(grupoId);
        return despesas.findByIdAndGrupoId(id, grupoId)
                .orElseThrow(
                        () -> new NaoEncontradoException("Despesa não encontrada neste grupo."));
    }

    private Despesa bloquear(Long grupoId, Long id, long versao) {
        grupos.buscar(grupoId);
        grupos.bloquear(grupoId);
        var despesa = buscar(grupoId, id);
        if (despesa.getVersao() != versao)
            throw new ConflitoException(
                    "Esta despesa foi alterada. Recarregue o cartão antes de tentar novamente.");
        return despesa;
    }

    @Transactional
    public DespesaResponse salvar(Long grupoId, Long id, long versao, MultipartFile upload) {
        var despesa = bloquear(grupoId, id, versao);
        var arquivo = ValidacaoComprovante.validar(upload);
        int atualizados =
                jdbc.update(
                        "update comprovantes set conteudo=? where despesa_id=?",
                        ps -> {
                            ps.setBinaryStream(
                                    1,
                                    new ByteArrayInputStream(arquivo.conteudo()),
                                    arquivo.conteudo().length);
                            ps.setLong(2, id);
                        });
        if (atualizados == 0)
            jdbc.update(
                    "insert into comprovantes(conteudo,despesa_id) values (?,?)",
                    ps -> {
                        ps.setBinaryStream(
                                1,
                                new ByteArrayInputStream(arquivo.conteudo()),
                                arquivo.conteudo().length);
                        ps.setLong(2, id);
                    });
        despesa.definirComprovante(
                arquivo.nome(), arquivo.tipo(), (long) arquivo.conteudo().length);
        return DespesaResponse.from(despesas.saveAndFlush(despesa));
    }

    @Transactional
    public DespesaResponse remover(Long grupoId, Long id, long versao) {
        var despesa = bloquear(grupoId, id, versao);
        if (despesa.getComprovanteNome() == null)
            throw new NaoEncontradoException("Esta despesa não tem comprovante.");
        jdbc.update("delete from comprovantes where despesa_id=?", id);
        despesa.definirComprovante(null, null, null);
        return DespesaResponse.from(despesas.saveAndFlush(despesa));
    }

    public record Conteudo(String nome, String tipo, byte[] bytes) {}

    @Transactional(readOnly = true)
    public Conteudo consultar(Long grupoId, Long id) {
        var despesa = buscar(grupoId, id);
        if (despesa.getComprovanteNome() == null)
            throw new NaoEncontradoException("Esta despesa não tem comprovante.");
        // Conteúdo e metadados vêm da mesma leitura, inclusive durante uma substituição.
        var blobs =
                jdbc.query(
                        "select d.comprovante_nome,d.comprovante_tipo,c.conteudo from comprovantes"
                                + " c join despesas d on d.id=c.despesa_id where d.id=? and"
                                + " d.grupo_id=?",
                        (rs, n) -> new Conteudo(rs.getString(1), rs.getString(2), rs.getBytes(3)),
                        id,
                        grupoId);
        if (blobs.isEmpty()) throw new NaoEncontradoException("Comprovante não encontrado.");
        return blobs.getFirst();
    }
}
