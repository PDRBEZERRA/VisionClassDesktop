package br.com.undb.visionclass.visionclassdesktop.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Gera um hash BCrypt a partir de uma senha em texto plano.
     * @param plainPassword A senha a ser encriptada.
     * @return O hash da senha.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifica se uma senha em texto plano corresponde a um hash guardado.
     * @param plainPassword A senha que o utilizador digitou.
     * @param hashedPassword O hash que está guardado na base de dados.
     * @return true se a senha corresponder, false caso contrário.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}