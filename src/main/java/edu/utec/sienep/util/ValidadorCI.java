package edu.utec.sienep.util;

public class ValidadorCI {

    /**
     * Elimina puntos/guiones y rellena con cero a la izquierda si tiene 7 dígitos,
     * de modo que el resultado siempre sea la representación canónica de 8 dígitos.
     * No valida el dígito verificador; usar validarCI() para eso.
     */
    public static String normalizar(String ci) {
        String stripped = ci.replace(".", "").replace("-", "");
        return stripped.length() == 7 ? "0" + stripped : stripped;
    }

    public static boolean validarCI(String ci) {
        ci = normalizar(ci);

        if (!ci.matches("\\d{8}")) return false;

        int[] coef = {2, 9, 8, 7, 6, 3, 4};
        int suma = 0;

        for (int i = 0; i < 7; i++) {
            suma += Character.getNumericValue(ci.charAt(i)) * coef[i];
        }

        int verificador = (10 - (suma % 10)) % 10;
        return verificador == Character.getNumericValue(ci.charAt(7));
    }
}
