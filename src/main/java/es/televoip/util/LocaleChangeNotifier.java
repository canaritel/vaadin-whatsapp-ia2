package es.televoip.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que notifica a los componentes registrados cuando cambia el idioma.
 */
public class LocaleChangeNotifier {
    private static List<Translatable> listeners = new ArrayList<>();

    /**
     * Añade un listener que implementa la interfaz Translatable.
     *
     * @param listener El componente que necesita actualizarse.
     */
    public static void addListener(Translatable listener) {
        listeners.add(listener);
    }

    /**
     * Notifica a todos los listeners registrados que el idioma ha cambiado.
     */
    public static void notifyListeners() {
        for (Translatable listener : listeners) {
            listener.updateTexts();
        }
    }
    
}
