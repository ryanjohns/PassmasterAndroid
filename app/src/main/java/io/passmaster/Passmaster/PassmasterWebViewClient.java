package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PassmasterWebViewClient extends WebViewClient {

  private final WeakReference<Activity> activityRef;

  public PassmasterWebViewClient(Activity activity) {
    activityRef = new WeakReference<>(activity);
  }

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    String errorString =
        "<html>" +
        "<head>" +
          "<style type='text/css'>" +
            "body { background-color: #8b99ab; color: #fff; text-align: center; font-family: arial, sans-serif; }" +
          "</style>" +
        "</head>" +
        "<body>" +
          "<div>" +
            "<h2>Passmaster</h2>" +
            "<h4>We're sorry, but something went wrong.</h4>" +
            "<h4>" + description + "</h4>" +
          "</div>" +
        "</body>" +
        "</html>";
    view.loadData(errorString, "text/html", null);
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url.startsWith("mailto:")) {
      final Activity activity = activityRef.get();
      if (activity != null) {
        MailTo mailTo = MailTo.parse(url);
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
    } else if (url.startsWith("bitcoin:")) {
      final Activity activity = activityRef.get();
      if (activity != null) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
          activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
          showLoadErrorAlert(activity, "No bitcoin applications found.");
        }
        return true;
      }
    } else if ((url.startsWith("http:") || url.startsWith("https:")) && !url.startsWith(PassmasterActivity.PASSMASTER_URL)) {
      final Activity activity = activityRef.get();
      if (activity != null) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
          activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
          showLoadErrorAlert(activity, "No web browsers found.");
        }
        return true;
      }
    }
    return false;
  }

  private void showLoadErrorAlert(Activity activity, String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);
    builder.setTitle("Load Error");
    builder.setMessage(message);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        // do nothing
      }
    });
    builder.create();
    builder.show();
  }
}
