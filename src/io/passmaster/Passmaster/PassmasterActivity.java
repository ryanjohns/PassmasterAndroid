package io.passmaster.Passmaster;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class PassmasterActivity extends Activity {

  public static final String PASSMASTER_URL = "https://passmaster.io/";

  @SuppressLint("SetJavaScriptEnabled") @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_passmaster);
    WebView webView = (WebView) findViewById(R.id.webView);
    WebSettings webSettings = webView.getSettings();
    String cachePath = getApplicationContext().getCacheDir().getAbsolutePath();
    webSettings.setAppCachePath(cachePath);
    webSettings.setAppCacheEnabled(true);
    webSettings.setDatabasePath(cachePath);
    webSettings.setDatabaseEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setJavaScriptEnabled(true);
    PassmasterWebViewClient webViewClient = new PassmasterWebViewClient();
    webView.setWebViewClient(webViewClient);
    webView.setWebChromeClient(new WebChromeClient());
    webView.addJavascriptInterface(new PassmasterJsInterface(webView), PassmasterJsInterface.JS_NAMESPACE);
    webView.loadUrl(PASSMASTER_URL);
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    WebView webView = (WebView) findViewById(R.id.webView);
    webView.loadUrl("javascript:MobileApp.updateAppCache();");
  }

}
