package rkr.simplekeyboard.inputmethod.translator;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal online translator using a public LibreTranslate-compatible endpoint.
 * Completely lazy — nothing is loaded until translate() is called.
 * Network permission is NOT declared by default; add INTERNET if you enable this.
 */
public final class LibreTranslateProvider implements Translator {
    private static final String TAG = "LibreTranslate";
    // Public demo endpoint (rate-limited). Replace with your own instance for production.
    private static final String ENDPOINT = "https://libretranslate.com/translate";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMain = new Handler(Looper.getMainLooper());

    private static final String[] LANGS = {
            "en", "es", "pt", "fr", "de", "it", "ru", "zh", "ja", "ko", "ar", "hi", "tr", "nl", "pl"
    };

    @Override
    public String[] getSupportedLanguages() {
        return LANGS.clone();
    }

    @Override
    public void translate(final String text, final String sourceLang, final String targetLang,
                          final Callback callback) {
        if (text == null || text.isEmpty()) {
            if (callback != null) callback.onError("Empty text");
            return;
        }
        mExecutor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("q", text);
                body.put("source", sourceLang == null ? "auto" : sourceLang);
                body.put("target", targetLang);
                body.put("format", "text");

                URL url = new URL(ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(15000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                if (code >= 200 && code < 300) {
                    JSONObject resp = new JSONObject(sb.toString());
                    final String translated = resp.optString("translatedText", "");
                    mMain.post(() -> {
                        if (callback != null) callback.onSuccess(translated);
                    });
                } else {
                    final String err = "HTTP " + code + ": " + sb;
                    mMain.post(() -> {
                        if (callback != null) callback.onError(err);
                    });
                }
            } catch (Exception e) {
                Log.w(TAG, "Translation failed", e);
                mMain.post(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            }
        });
    }
}
