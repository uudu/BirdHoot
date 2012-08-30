package ee.ut.uudu.birdhoot;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import ee.ut.uudu.birdhoot.LoginWebViewActivity.WEBVIEW_REQUEST_CODE;

public class AuthActivity extends Activity {

    private Twitter twitter;
    private AUTH_REQUEST_CODE action;

    public enum AUTH_REQUEST_CODE {
        LOGIN, LOGOUT;
        public static AUTH_REQUEST_CODE valueOf(int ordinal) {
            for (AUTH_REQUEST_CODE code : AUTH_REQUEST_CODE.values()) {
                if (code.ordinal() == ordinal) {
                    return code;
                }
            }
            return null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "OnCreate()", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_auth);
        this.action = AUTH_REQUEST_CODE.valueOf(getIntent().getStringExtra("AUTH_ACTION"));
        Button b = (Button) findViewById(R.id.button_auth_login);
        switch (this.action) {
        case LOGIN:
            b.setText("TODO: authenticate");
            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        RequestToken requestToken = getTwitter().getOAuthRequestToken(Util.CALLBACK_URL);
                        Intent intent = new Intent(AuthActivity.this, LoginWebViewActivity.class);
                        intent.putExtra("URL", requestToken.getAuthenticationURL());
                        startActivityForResult(intent, WEBVIEW_REQUEST_CODE.AUTHORIZATION.ordinal());
                    } catch (TwitterException e) {
                        Toast.makeText(AuthActivity.this, "FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
            break;
        case LOGOUT:
            b.setText("TODO: unauthenticate");
            b.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    onClickLogout();
                }
            });
            break;
        }
    }

    /**
     * When returned from twitter authorization page.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WEBVIEW_REQUEST_CODE.AUTHORIZATION.ordinal()) {
            switch (resultCode) {
            case RESULT_OK:
                String oauthVerifier = (String) data.getExtras().get(LoginWebViewActivity.OAUTH_VERIFIER_KEY);
                AccessToken at = null;
                try {
                    // Get access token and secret
                    at = twitter.getOAuthAccessToken(oauthVerifier);
                    storeAccessToken(at);
                    // close this activity and return back to main view
                    finish();
                } catch (TwitterException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Getting access token failed!", Toast.LENGTH_LONG).show();
                }
                break;
            case RESULT_CANCELED:
                Toast.makeText(this, "Credentials cancelled!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    /**
     * Just close this activity and return.
     * 
     * @param cancelButton Cancel button
     */
    public void onClickCancel(View cancelButton) {
        finish();
    }

    /**
     * OnClick handler for logout button.
     * 
     * @param logoutButton
     */
    // TODO: do this
    public void onClickLogout() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        editor.remove(Util.ACCESS_TOKEN);
        editor.remove(Util.ACCESS_TOKEN_SECRET);
        editor.commit();
        finish();
    }

    /**
     * Persist access token and access token secret.
     * 
     * @param at
     */
    private void storeAccessToken(AccessToken at) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Util.ACCESS_TOKEN, at.getToken());
        editor.putString(Util.ACCESS_TOKEN_SECRET, at.getTokenSecret());
        editor.commit();
    }

    private Twitter getTwitter() {
        //if (twitter == null) {
            ConfigurationBuilder b = new ConfigurationBuilder();
            b.setOAuthConsumerKey(Util.CONSUMER_KEY);
            b.setOAuthConsumerSecret(Util.CONSUMER_SECRET);
            twitter = new TwitterFactory(b.build()).getInstance();
        //}
        return twitter;
    }

}
