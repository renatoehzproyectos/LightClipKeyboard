package rkr.simplekeyboard.inputmethod.translator;

/**
 * Abstraction for translation providers.
 * Keeps the core keyboard free of network / heavy engine dependencies.
 */
public interface Translator {
    interface Callback {
        void onSuccess(String translated);
        void onError(String message);
    }

    void translate(String text, String sourceLang, String targetLang, Callback callback);

    /** Human-readable list of supported language codes, e.g. "en", "es", "pt", "fr" */
    String[] getSupportedLanguages();
}
