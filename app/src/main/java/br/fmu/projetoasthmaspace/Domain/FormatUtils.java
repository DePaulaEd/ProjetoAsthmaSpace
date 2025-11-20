package br.fmu.projetoasthmaspace.Domain;

public class FormatUtils {

    public static String formatTelefone(String telefone) {
        if (telefone == null) return null;

        // Remove tudo que não for número
        telefone = telefone.replaceAll("[^0-9]", "");

        // Se tiver 11 dígitos (celular com DDD)
        if (telefone.length() == 11) {
            return "(" + telefone.substring(0, 2) + ") " +
                    telefone.substring(2, 7) + "-" +
                    telefone.substring(7);
        }

        // Se tiver 10 dígitos (fixo com DDD)
        if (telefone.length() == 10) {
            return "(" + telefone.substring(0, 2) + ") " +
                    telefone.substring(2, 6) + "-" +
                    telefone.substring(6);
        }

        // Se não bater, retorna como veio
        return telefone;
    }
}
