package br.com.rachai.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ErroResponse(String mensagem, Map<String, String> campos) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new TreeMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErroResponse("Confira os campos informados.", campos));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ErroResponse> formatoInvalido(Exception exception) {
        return erro(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos. Confira os campos, o valor e a data informados.");
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException exception) {
        return erro(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResponse> conflito(ConflitoException exception) {
        return erro(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    ResponseEntity<ErroResponse> arquivoInvalido(ArquivoInvalidoException exception) {
        return erro(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<ErroResponse> arquivoGrande(Exception exception) {
        return erro(HttpStatus.PAYLOAD_TOO_LARGE, "O comprovante deve ter até 5 MB.");
    }

    @ExceptionHandler(
            org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    ResponseEntity<ErroResponse> arquivoAusente(Exception exception) {
        return erro(HttpStatus.BAD_REQUEST, "Selecione o arquivo do comprovante.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException exception) {
        return erro(
                HttpStatus.CONFLICT,
                "Não foi possível salvar. Verifique registros duplicados ou vínculos existentes.");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ErroResponse> acessoNegado(Exception exception) {
        return erro(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(mensagem, Map.of()));
    }
}
