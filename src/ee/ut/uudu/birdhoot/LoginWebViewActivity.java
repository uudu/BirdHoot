package ee.ut.uudu.birdhoot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginWebViewActivity extends Activity {
    private Intent intent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = getIntent();
        String url = (String) intent.getExtras().get("URL");
        WebView webView = (WebView) findViewById(R.id.login_view);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Util.CALLBACK_URL)) {
                    Uri uri = Uri.parse(url);
                    String oauthVerifier = uri.getQueryParameter("oauth_verifier");
                    intent.putExtra("oauth_verifier", oauthVerifier);
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(url);
    }
}
