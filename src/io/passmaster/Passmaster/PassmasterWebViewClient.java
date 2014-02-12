package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PassmasterWebViewClient extends WebViewClient {

  private final String passmasterErrorHTML =
      "<html>" +
      "<head>" +
        "<style type='text/css'>" +
          "body { background-color: #8b99ab; color: #fff; text-align: center; font-family: arial, sans-serif; }" +
        "</style>" +
        "<script type='text/javascript'>" +
          "function MobileApp() {};" +
          "MobileApp.updateAppCache = function() {" +
            PassmasterJsInterface.JS_NAMESPACE + ".loadPassmaster();" +
          "};" +
        "</script>" +
      "</head>" +
      "<body>" +
        "<div>" +
          "<h2>Passmaster</h2>" +
          "<h4>We're sorry, but something went wrong.</h4>" +
          "<h4>%s</h4>" +
        "</div>" +
      "</body>" +
      "</html>";

  private final WeakReference<Activity> activityRef;

  public PassmasterWebViewClient(Activity activity) {
    activityRef = new WeakReference<Activity>(activity);
  }

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    String errorString = String.format(passmasterErrorHTML, description);
    view.loadData(errorString, "text/html", null);
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url.startsWith("mailto:")) {
      final Activity activity = activityRef.get();
      if (activity != null) {
        MailTo mailTo = MailTo.parse(url);
        Intent intent = newEmailIntent(activity, mailTo.getTo(), mailTo.getSubject(), mailTo.getBody(), mailTo.getCc());
        activity.startActivity(intent);
        view.reload();
      }
    } else {
      view.loadUrl(url);
    }
    return true;
  }

  private Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
    intent.putExtra(Intent.EXTRA_TEXT, body);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(Intent.EXTRA_CC, cc);
    intent.setType("message/rfc822");
    return intent;
  }

}
