package io.passmaster.Passmaster;

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

  public PassmasterJsInterface(WebView webView) {
    this.webView = webView;
  }

  @JavascriptInterface
  public void loadPassmaster() {
    webView.loadUrl(PassmasterActivity.PASSMASTER_URL);
  }

  @JavascriptInterface
  public void copyToClipboard(String text) {
    ClipboardManager clipboard = (ClipboardManager) PassmasterActivity.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Passmaster Data", text);
    clipboard.setPrimaryClip(clip);
    Toast toast = Toast.makeText(PassmasterActivity.getAppContext(), copiedText, Toast.LENGTH_SHORT);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }
}
