package br.com.rachai.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.rachai.exception.ArquivoInvalidoException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

class ValidacaoComprovanteTest {
    static byte[] imagem(String tipo) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), tipo, bytes);
        return bytes.toByteArray();
    }

    @Test
    void validaConteudoRealENormalizaNomeSemConfiarNoMimeDoCliente() throws Exception {
        var png =
                ValidacaoComprovante.validar(
                        new MockMultipartFile(
                                "arquivo", "C:\\temp\\foto.PNG", "text/html", imagem("png")));
        assertEquals("foto.PNG", png.nome());
        assertEquals("image/png", png.tipo());
        var jpg =
                ValidacaoComprovante.validar(
                        new MockMultipartFile(
                                "arquivo", "foto.jpg", "application/octet-stream", imagem("jpg")));
        assertEquals("image/jpeg", jpg.tipo());
    }

    @Test
    void bloqueiaVazioExcessoEArquivoDisfarcado() throws Exception {
        for (var arquivo :
                new MockMultipartFile[] {
                    new MockMultipartFile("arquivo", "a.png", "image/png", new byte[0]),
                    new MockMultipartFile(
                            "arquivo",
                            "a.png",
                            "image/png",
                            new byte[ValidacaoComprovante.LIMITE + 1]),
                    new MockMultipartFile(
                            "arquivo",
                            "a.png",
                            "image/png",
                            "<script>alert(1)</script>".getBytes()),
                    new MockMultipartFile(
                            "arquivo", "a.pdf", "application/pdf", "<html>fake</html>".getBytes()),
                    new MockMultipartFile("arquivo", "a.svg", "image/svg+xml", imagem("png")),
                    new MockMultipartFile("arquivo", "a.jpg", "image/jpeg", imagem("png"))
                })
            assertThrows(
                    ArquivoInvalidoException.class, () -> ValidacaoComprovante.validar(arquivo));
    }
}
