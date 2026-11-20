package br.com.tcc.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Geração e conferência de hash de senha.
 *
 * Substitui a antiga classe EncriptaSenha, que usava MD5 sem salt
 * (e sun.misc.BASE64Encoder, removido do JDK a partir do Java 9).
 *
 * Formato gravado no banco: pbkdf2$ITERACOES$SALT_BASE64$HASH_BASE64
 *
 * As senhas antigas (MD5 em Base64) continuam sendo aceitas no login
 * para permitir a migração gradual: ver AutenticacaoService.
 */
public final class SenhaService {

    private static final String PREFIXO = "pbkdf2";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaService() {
    }

    public static String gerarHash(String senha) {
        validar(senha);
        byte[] salt = new byte[TAMANHO_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(senha.toCharArray(), salt, ITERACOES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIXO + "$" + ITERACOES + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    public static boolean confere(String senhaDigitada, String hashGravado) {
        if (senhaDigitada == null || hashGravado == null) {
            return false;
        }
        if (ehHashSeguro(hashGravado)) {
            String[] partes = hashGravado.split("\\$");
            if (partes.length != 4) {
                return false;
            }
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = pbkdf2(senhaDigitada.toCharArray(), salt, iteracoes);
            return MessageDigest.isEqual(esperado, calculado);
        }
        // senha ainda no formato do sistema legado
        byte[] esperado = hashGravado.getBytes(StandardCharsets.US_ASCII);
        byte[] calculado = md5Legado(senhaDigitada).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(esperado, calculado);
    }

    public static boolean ehHashSeguro(String valor) {
        return valor != null && valor.startsWith(PREFIXO + "$");
    }

    /**
     * Prepara o conteúdo do campo senha antes de gravar o funcionário.
     * Se o campo já contém um hash (novo ou legado), mantém como está;
     * caso contrário é uma senha digitada e precisa virar hash.
     */
    public static String prepararParaGravar(String valorDoCampo) {
        if (ehHashSeguro(valorDoCampo) || pareceMd5Legado(valorDoCampo)) {
            return valorDoCampo;
        }
        return gerarHash(valorDoCampo);
    }

    static boolean pareceMd5Legado(String valor) {
        return valor != null && valor.length() == 24 && valor.endsWith("==");
    }

    /** Mesmo cálculo da antiga EncriptaSenha, usado só para migração. */
    static String md5Legado(String senha) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return Base64.getEncoder().encodeToString(md5.digest(senha.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 indisponível", e);
        }
    }

    private static byte[] pbkdf2(char[] senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // antes o sistema devolvia a senha em texto puro nesse caso
            throw new IllegalStateException("Não foi possível gerar o hash da senha", e);
        }
    }

    private static void validar(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
    }
}
