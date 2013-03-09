package io.passmaster.Passmaster;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public final class PassmasterJsInterface {

  public static final String JS_NAMESPACE = "AndroidJs";
  private WebView webView;

  public PassmasterJsInterface(WebView webView) {
    this.webView = webView;
  }

  @JavascriptInterface
  public void loadPassmaster() {
    webView.loadUrl(PassmasterActivity.PASSMASTER_URL);
  }
}
