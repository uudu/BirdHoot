package ee.ut.uudu.birdhoot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginWebViewActivity extends Activity {
    private Intent intent;

    public static final String OAUTH_VERIFIER_KEY = "oauth_verifier";
    public static final String OAUTH_DENIED_KEY = "denied";
    public static final String URL_KEY = "URL";

    public enum WEBVIEW_REQUEST_CODE {
        AUTHORIZATION
    };
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = getIntent();
        WebView webView = (WebView) findViewById(R.id.login_view);
        webView.setWebViewClient(new WebViewClient() {

            /**
             * If URL to redirect to is callback url then we are done.
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Util.CALLBACK_URL)) {
                    Uri uri = Uri.parse(url);
                    String oauthVerifier = uri.getQueryParameter(OAUTH_VERIFIER_KEY);
                    String denied = uri.getQueryParameter(OAUTH_DENIED_KEY);
                    if (oauthVerifier != null) {
                        intent.putExtra(OAUTH_VERIFIER_KEY, oauthVerifier);
                        setResult(RESULT_OK, intent);
                    } else if (denied != null) {
                        intent.putExtra(OAUTH_DENIED_KEY, denied);
                        setResult(RESULT_CANCELED, intent);
                    }
                    finish();
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl((String) intent.getExtras().get(URL_KEY));
    }
}
