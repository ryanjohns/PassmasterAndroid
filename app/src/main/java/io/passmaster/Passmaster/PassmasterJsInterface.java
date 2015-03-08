package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public final class PassmasterJsInterface {

  public static final String JS_NAMESPACE = "AndroidJs";
  private WebView webView;
  private final WeakReference<Activity> activityRef;
  private long lockTime;

  public PassmasterJsInterface(Activity activity, WebView webView) {
    activityRef = new WeakReference<>(activity);
    this.webView = webView;
    lockTime = 0;
  }

  @JavascriptInterface
  public void loadPassmaster() {
    final Activity activity = activityRef.get();
    activity.runOnUiThread(new Runnable() {
      public void run() {
        webView.loadUrl(PassmasterActivity.PASSMASTER_URL);
      }
    });
  }

  @JavascriptInterface
  public void copyToClipboard(String text) {
    final Activity activity = activityRef.get();
    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Passmaster Data", text);
    clipboard.setPrimaryClip(clip);
  }

  @JavascriptInterface
  public void checkLockTime() {
    if (lockTime > 0 && lockTime < System.currentTimeMillis() / 1000) {
      final Activity activity = activityRef.get();
      activity.runOnUiThread(new Runnable() {
        public void run() {
          webView.loadUrl("javascript:MobileApp.lock();");
        }
      });
    }
  }

  @JavascriptInterface
  public void saveLockTime(int minutes) {
    if (minutes == 0) {
      lockTime = 0;
    } else {
      lockTime = (System.currentTimeMillis() / 1000) + (minutes * 60);
    }
  }
}
