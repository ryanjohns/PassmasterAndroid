package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public final class PassmasterJsInterface {

  public static final String JS_NAMESPACE = "AndroidJs";
  private final String copiedText = "Copied to Clipboard";
  private WebView webView;
  private final WeakReference<Activity> activityRef;

  public PassmasterJsInterface(Activity activity, WebView webView) {
    activityRef = new WeakReference<Activity>(activity);
    this.webView = webView;
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
    ClipboardManager clipboard = (ClipboardManager) activity.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Passmaster Data", text);
    clipboard.setPrimaryClip(clip);
    Toast toast = Toast.makeText(activity.getApplicationContext(), copiedText, Toast.LENGTH_SHORT);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }
}
