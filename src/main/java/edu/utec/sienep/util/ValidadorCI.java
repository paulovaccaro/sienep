package edu.utec.sienep.util;

public class ValidadorCI {

    public static boolean validarCI(String ci) {
        ci = ci.replace(".", "").replace("-", "");

        if (!ci.matches("\\d{7,8}")) return false;

        if (ci.length() == 7) {
            ci = "0" + ci;
        }

        int[] coef = {2, 9, 8, 7, 6, 3, 4};
        int suma = 0;

        for (int i = 0; i < 7; i++) {
            suma += Character.getNumericValue(ci.charAt(i)) * coef[i];
        }

        int verificador = (10 - (suma % 10)) % 10;
        return verificador == Character.getNumericValue(ci.charAt(7));
    }
}
