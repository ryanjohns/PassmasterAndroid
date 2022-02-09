package io.passmaster.Passmaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;

import java.lang.ref.WeakReference;

public class PassmasterWebViewClient extends WebViewClient {

  private final WeakReference<Activity> activityRef;

  PassmasterWebViewClient(Activity activity) {
    activityRef = new WeakReference<>(activity);
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    if (url.equals(PassmasterActivity.PASSMASTER_URL)) {
      final Activity activity = activityRef.get();
      String javascript =
              "if (typeof(MobileApp) == 'object' && typeof(MobileApp.appLoaded) == 'function' && MobileApp.appLoaded() == 'YES') {" +
                "MobileApp.clickUnlockWithTouchID();" +
              "}";
      activity.runOnUiThread(() -> view.evaluateJavascript(javascript, null));
    }
  }

  @Override
  public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
    if (request.getUrl().toString().equals(PassmasterActivity.PASSMASTER_URL)) {
      String errorHTML =
          "<html>" +
          "<head>" +
            "<style type='text/css'>" +
              "body { background-color: #8b99ab; color: #fff; text-align: center; font-family: arial, sans-serif; }" +
            "</style>" +
            "<script>" +
              "function loadPassmaster() {" +
                PassmasterJsInterface.JS_NAMESPACE + ".loadPassmaster();" +
              "}" +
            "</script>" +
          "</head>" +
          "<body>" +
            "<div>" +
              "<h2>Passmaster</h2>" +
              "<h4>We're sorry, but something went wrong.</h4>" +
              "<h4>" + error.getDescription().toString() + "</h4>" +
              "<button onclick='loadPassmaster();'>Try again</button>" +
            "</div>" +
          "</body>" +
          "</html>";
      final Activity activity = activityRef.get();
      activity.runOnUiThread(() -> view.loadDataWithBaseURL(null, errorHTML, "text/html", "UTF-8", null));
    }
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    Uri uri = request.getUrl();
    if ("mailto".equals(uri.getScheme())) {
      final Activity activity = activityRef.get();
      if (activity != null) {
        MailTo mailTo = MailTo.parse(uri.toString());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { mailTo.getTo() });
        intent.setType("message/rfc822");
        try {
          activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
          showLoadErrorAlert(activity, "No mail applications found.");
        }
        return true;
      }
    } else if (request.hasGesture() && !request.getUrl().toString().equals(PassmasterActivity.PASSMASTER_URL)) {
      final Activity activity = activityRef.get();
      if (activity != null) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        try {
          activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
          showLoadErrorAlert(activity, "No application for " + uri);
        }
        return true;
      }
    }
    return false;
  }

  private void showLoadErrorAlert(Activity activity, String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle("Load Error");
    builder.setMessage(message);
    builder.setPositiveButton("OK", (dialog, id) -> {
      // do nothing
    });
    builder.create();
    builder.show();
  }
}
