package es.televoip.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LanguageService {

	 private Locale currentLocale = Locale.forLanguageTag("es"); // Idioma por defecto: Español
	  
    private final List<LanguageChangeListener> listeners = new ArrayList<>();

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setLocale(Locale locale) {
        if (!locale.equals(this.currentLocale)) {
            this.currentLocale = locale;
            notifyListeners();
        }
    }

    public void addLanguageChangeListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        listeners.forEach(LanguageChangeListener::onLanguageChange);
    }

    public interface LanguageChangeListener {
        void onLanguageChange();
    }
    
}
