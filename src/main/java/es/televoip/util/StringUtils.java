package es.televoip.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Clase de utilidades para manejar cadenas de texto.
 */
public class StringUtils {

    /**
     * Elimina los acentos y otros diacríticos de una cadena de texto.
     *
     * @param input La cadena de texto original.
     * @return La cadena de texto sin acentos.
     */
    public static String removeAccents(String input) {
        if (input == null) {
            return null;
        }
        // Normaliza la cadena a la forma decomposed (NFD)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Expresión regular para eliminar los diacríticos
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
    
}
