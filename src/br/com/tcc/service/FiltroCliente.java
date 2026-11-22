package br.com.tcc.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Monta a consulta da lista de clientes.
 *
 * Antes (copiado em 18 telas):
 *   sql += " where upper(" + ordem + ") like '" + filtro.toUpperCase() + "%' ";
 * O texto digitado entrava direto na JPQL. Um nome como D'Ávila quebrava a
 * consulta, e qualquer usuário podia alterar o comando enviado ao banco.
 *
 * Agora o campo só pode ser um dos nomes da lista abaixo e o texto
 * digitado vai como parâmetro (:filtro).
 */
public class FiltroCliente {

    private static final Map<String, String> CAMPO_POR_ROTULO = new HashMap<>();

    static {
        CAMPO_POR_ROTULO.put("Código", "id");
        CAMPO_POR_ROTULO.put("Nome", "nome");
        CAMPO_POR_ROTULO.put("CPF", "cpf");
        CAMPO_POR_ROTULO.put("RG", "rg");
        CAMPO_POR_ROTULO.put("Endereço", "endereco");
        CAMPO_POR_ROTULO.put("e-mail", "email");
        CAMPO_POR_ROTULO.put("Nascimento", "dataNascimento");
        CAMPO_POR_ROTULO.put("Admissão", "clienteDesde");
    }

    private final String campoOrdem;
    private final String texto;

    public FiltroCliente(String rotuloOrdem, String texto) {
        String campo = CAMPO_POR_ROTULO.get(rotuloOrdem);
        this.campoOrdem = campo != null ? campo : "id";
        this.texto = texto == null ? "" : texto.trim();
    }

    public boolean temFiltro() {
        return !texto.isEmpty();
    }

    /**
     * Com a ordem em "Código" o legado procurava o texto dentro do código
     * (buscar "Jarbas" não achava nada). Nesse caso o filtro passa a ser pelo nome.
     */
    public String campoFiltro() {
        return "id".equals(campoOrdem) ? "nome" : campoOrdem;
    }

    public String campoOrdem() {
        return campoOrdem;
    }

    public String jpql() {
        StringBuilder sb = new StringBuilder("SELECT c FROM Cliente c");
        if (temFiltro()) {
            sb.append(" WHERE UPPER(c.").append(campoFiltro()).append(") LIKE :filtro");
        }
        sb.append(" ORDER BY c.").append(campoOrdem);
        return sb.toString();
    }

    /** Procura o texto em qualquer parte do campo (o legado só olhava o começo). */
    public String valorParametro() {
        return "%" + texto.toUpperCase(new Locale("pt", "BR")) + "%";
    }
}
