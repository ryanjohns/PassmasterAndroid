package io.passmaster.Passmaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class PassmasterActivity extends Activity {

  public static final String PASSMASTER_URL = "https://passmaster.io/";
  private FrameLayout webViewPlaceholder;
  private WebView webView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_passmaster);
    initUI();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    String reloadFunction = "javascript:" +
        "if (typeof(MobileApp) == 'object' && typeof(MobileApp.appLoaded) == 'function' && MobileApp.appLoaded() == 'YES') {" +
          PassmasterJsInterface.JS_NAMESPACE + ".checkLockTime();" +
          "MobileApp.updateAppCache();" +
        "} else {" +
          PassmasterJsInterface.JS_NAMESPACE + ".loadPassmaster();" +
        "}";
    webView.loadUrl(reloadFunction);
  }

  @Override
  protected void onPause() {
    super.onPause();
    String pauseFunction = "javascript:" +
        "if (typeof(MobileApp) == 'object' && typeof(MobileApp.appLoaded) == 'function' && MobileApp.appLoaded() == 'YES') {" +
          PassmasterJsInterface.JS_NAMESPACE + ".saveLockTime(MobileApp.getTimeoutMinutes());" +
        "}";
    webView.loadUrl(pauseFunction);
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    if (webView != null) {
      webViewPlaceholder.removeView(webView);
    }
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.activity_passmaster);
    initUI();
  }

  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    webView.restoreState(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    webView.saveState(outState);
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void initUI() {
    webViewPlaceholder = findViewById(R.id.webViewPlaceholder);
    if (webView == null) {
      webView = new WebView(this);
      PackageInfo pInfo = null;
      try {
        pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      } catch (NameNotFoundException e) {
        // do nothing because this will never happen
      }
      String cachePath = getApplicationContext().getCacheDir().getAbsolutePath();
      WebSettings webSettings = webView.getSettings();
      webSettings.setAppCachePath(cachePath);
      webSettings.setAppCacheEnabled(true);
      webSettings.setDatabaseEnabled(true);
      webSettings.setDomStorageEnabled(true);
      webSettings.setJavaScriptEnabled(true);
      webSettings.setUserAgentString(webSettings.getUserAgentString() + " PassmasterAndroid/" + (pInfo != null ? pInfo.versionName : "unknown"));
      webView.setWebViewClient(new PassmasterWebViewClient(this));
      webView.setWebChromeClient(new PassmasterWebChromeClient(this));
      webView.addJavascriptInterface(new PassmasterJsInterface(this, webView), PassmasterJsInterface.JS_NAMESPACE);
      webView.loadUrl(PASSMASTER_URL);
    }
    webViewPlaceholder.addView(webView);
  }

}
