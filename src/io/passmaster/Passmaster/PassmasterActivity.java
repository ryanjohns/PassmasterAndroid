package io.passmaster.Passmaster;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.view.Gravity;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PassmasterActivity extends Activity {

  public static final String PASSMASTER_URL = "https://passmaster.io/";
  private static Context context;
  private final Activity passmasterActivity = this;
  private FrameLayout webViewPlaceholder;
  private WebView webView;

  public static Context getAppContext() {
    return PassmasterActivity.context;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_passmaster);
    initUI();
    PassmasterActivity.context = getApplicationContext();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    webView.loadUrl("javascript:MobileApp.updateAppCache();");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    if (webView != null) {
      webViewPlaceholder.removeView(webView);
    }
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.activity_passmaster);
    initUI();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    webView.restoreState(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    webView.saveState(outState);
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void initUI() {
    webViewPlaceholder = (FrameLayout) findViewById(R.id.webViewPlaceholder);
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
      webSettings.setDatabasePath(cachePath);
      webSettings.setDatabaseEnabled(true);
      webSettings.setDomStorageEnabled(true);
      webSettings.setJavaScriptEnabled(true);
      webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
      webSettings.setUserAgentString(webSettings.getUserAgentString() + " PassmasterAndroid/" + pInfo.versionName);
      webView.setWebViewClient(new PassmasterWebViewClient());
      webView.setWebChromeClient(new PassmasterWebChromeClient());
      webView.addJavascriptInterface(new PassmasterJsInterface(webView), PassmasterJsInterface.JS_NAMESPACE);
      webView.loadUrl(PASSMASTER_URL);
    }
    webViewPlaceholder.addView(webView);
  }

  // inner class for handling alert and confirmation dialogs
  private class PassmasterWebChromeClient extends WebChromeClient {
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
      result.confirm();
      Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();
      return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
      AlertDialog.Builder builder = new AlertDialog.Builder(passmasterActivity);
      builder.setMessage(message);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          result.confirm();
        }
      });
      builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          result.cancel();
        }
      });
      builder.create();
      builder.show();
      return true;
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
      quotaUpdater.updateQuota(requiredStorage * 2);
    }
  }

}
