package edu.utec.sienep.util;

import java.time.LocalDate;
import java.time.Period;

public class ValidadorEdad {

    public static boolean esMayorDe18(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }
}
