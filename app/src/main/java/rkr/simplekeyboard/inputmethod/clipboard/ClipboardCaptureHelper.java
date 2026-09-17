package rkr.simplekeyboard.inputmethod.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Captures primary clipboard changes into the persistent history.
 * Lazy: only active while the IME is bound / keyboard visible if desired.
 * Does not replace the system clipboard.
 */
public final class ClipboardCaptureHelper {
    private static final String TAG = "ClipCapture";

    private final ClipboardManager mClipboard;
    private final ClipboardRepository mRepo;
    private boolean mListening;

    private final ClipboardManager.OnPrimaryClipChangedListener mListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    try {
                        if (mClipboard.hasPrimaryClip() && mClipboard.getPrimaryClip() != null
                                && mClipboard.getPrimaryClip().getItemCount() > 0) {
                            CharSequence cs = mClipboard.getPrimaryClip().getItemAt(0).getText();
                            if (cs != null) {
                                String text = cs.toString();
                                if (!text.isEmpty()) {
                                    long id = mRepo.addFromClipboard(text);
                                    if (id > 0) {
                                        Log.d(TAG, "Captured " + text.length() + " chars, id=" + id);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Capture failed", e);
                    }
                }
            };

    public ClipboardCaptureHelper(Context context) {
        mClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        mRepo = ClipboardRepository.getInstance(context);
    }

    public void startListening() {
        if (mListening || mClipboard == null) return;
        try {
            mClipboard.addPrimaryClipChangedListener(mListener);
            mListening = true;
        } catch (Exception e) {
            Log.w(TAG, "Cannot add listener", e);
        }
    }

    public void stopListening() {
        if (!mListening || mClipboard == null) return;
        try {
            mClipboard.removePrimaryClipChangedListener(mListener);
        } catch (Exception ignored) {}
        mListening = false;
    }
}
