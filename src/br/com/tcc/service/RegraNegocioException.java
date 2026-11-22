package br.com.tcc.service;

/** Erro de validação que deve ser mostrado ao usuário como está. */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
