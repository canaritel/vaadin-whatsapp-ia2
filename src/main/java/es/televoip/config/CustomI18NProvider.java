package es.televoip.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class CustomI18NProvider implements I18NProvider {
	
    private static final long serialVersionUID = 1L;
    
    public static final String BUNDLE_PREFIX = "messages";

    @Override
    public List<Locale> getProvidedLocales() {
        return Arrays.asList(
                Locale.forLanguageTag("es"),  // Español (idioma por defecto)
                Locale.ENGLISH,               // Inglés
                Locale.FRENCH,                // Francés
                Locale.GERMAN                 // Alemán
                // Agrega más idiomas según sea necesario
        );
    }

    public ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_PREFIX, locale);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        ResourceBundle bundle = getResourceBundle(locale);
        if (bundle.containsKey(key)) {
            String value = bundle.getString(key);
            if (params != null && params.length > 0) {
                return MessageFormat.format(value, params);
            }
            return value;
        } else {
            // Si la clave no existe, devuelve la clave misma o un valor por defecto
            return key;
        }
    }
    
}
