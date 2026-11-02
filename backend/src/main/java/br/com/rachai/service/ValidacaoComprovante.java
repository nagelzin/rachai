package br.com.rachai.service;

import br.com.rachai.exception.ArquivoInvalidoException;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.imageio.ImageIO;

public final class ValidacaoComprovante {
    public static final int LIMITE = 5 * 1024 * 1024;

    private ValidacaoComprovante() {}

    public record Arquivo(String nome, String tipo, byte[] conteudo) {}

    public static Arquivo validar(MultipartFile arquivo) {
        if (arquivo.isEmpty() || arquivo.getSize() > LIMITE)
            throw new ArquivoInvalidoException("Escolha um comprovante de até 5 MB, não vazio.");
        String original = arquivo.getOriginalFilename();
        String nome = original == null ? "comprovante" : original.replace('\\', '/');
        nome =
                nome.substring(nome.lastIndexOf('/') + 1)
                        .replaceAll("[^\\p{L}\\p{N} ._-]", "_")
                        .strip();
        if (nome.length() > 120 || nome.isBlank())
            throw new ArquivoInvalidoException("Use um nome de arquivo com até 120 caracteres.");
        String lower = nome.toLowerCase(Locale.ROOT);
        try (var entrada = arquivo.getInputStream()) {
            byte[] bytes = entrada.readNBytes(LIMITE + 1);
            if (bytes.length == 0 || bytes.length > LIMITE)
                throw new ArquivoInvalidoException(
                        "Escolha um comprovante de até 5 MB, não vazio.");
            String tipo;
            if (lower.endsWith(".pdf")) {
                String inicio =
                        new String(
                                bytes, 0, Math.min(8, bytes.length), StandardCharsets.ISO_8859_1);
                String fim =
                        new String(
                                bytes,
                                Math.max(0, bytes.length - 2048),
                                Math.min(bytes.length, 2048),
                                StandardCharsets.ISO_8859_1);
                if (!inicio.matches("%PDF-[12]\\.[0-9].*") || !fim.contains("%%EOF"))
                    throw invalido();
                tipo = "application/pdf";
            } else {
                if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg"))
                    throw invalido();
                // Não depende do Content-Type enviado pelo cliente. Limita dimensões antes de
                // decodificar.
                try (var imagem = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                    var leitores = ImageIO.getImageReaders(imagem);
                    if (!leitores.hasNext()) throw invalido();
                    var leitor = leitores.next();
                    try {
                        leitor.setInput(imagem);
                        String formato = leitor.getFormatName().toLowerCase(Locale.ROOT);
                        boolean png = formato.equals("png") && lower.endsWith(".png");
                        boolean jpeg =
                                formato.equals("jpeg")
                                        && (lower.endsWith(".jpg") || lower.endsWith(".jpeg"));
                        if (!png && !jpeg) throw invalido();
                        long pixels = (long) leitor.getWidth(0) * leitor.getHeight(0);
                        if (pixels <= 0 || pixels > 16_000_000)
                            throw new ArquivoInvalidoException(
                                    "Use uma imagem de até 16 megapixels.");
                        var decodificada = leitor.read(0);
                        if (decodificada == null) throw invalido();
                        decodificada.flush();
                        tipo = png ? "image/png" : "image/jpeg";
                    } finally {
                        leitor.dispose();
                    }
                }
            }
            return new Arquivo(nome, tipo, bytes);
        } catch (IOException e) {
            throw invalido();
        }
    }

    private static ArquivoInvalidoException invalido() {
        return new ArquivoInvalidoException(
                "Arquivo inválido. Envie uma imagem JPG/PNG ou um PDF válido.");
    }
}
