package es.televoip.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.springframework.stereotype.Component;

import es.televoip.service.LanguageService;

@Component
public class I18nUtil {

    private ResourceBundle bundle;

    public I18nUtil(LanguageService languageService) {
        updateBundle(languageService.getCurrentLocale());
        // Registrar como listener para actualizar el bundle cuando cambia el idioma
        languageService.addLanguageChangeListener(this::updateBundle);
    }

    private void updateBundle() {
        // Este método será llamado cuando cambie el idioma
        // En el enfoque de recarga de página, esto no es estrictamente necesario,
        // pero se mantiene por si se necesita en el futuro.
    }

    private void updateBundle(Locale locale) {
        this.bundle = ResourceBundle.getBundle("messages", locale);
    }

    /*
    public String get(String key) {
        return bundle.getString(key);
    }
    */

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Missing resource key: {}. Using key as default." + key);
            return key; // O puedes devolver un valor por defecto como "Desconocido"
        }
    }

    public String getFormatted(String key, Object... params) {
        String pattern = get(key);
        return MessageFormat.format(pattern, params);
    }
    
    public boolean containsKey(String key) {
       return bundle.containsKey(key);
   }
    
}
