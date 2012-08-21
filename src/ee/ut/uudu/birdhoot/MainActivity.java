package ee.ut.uudu.birdhoot;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public enum REQUEST_CODE {
        AUTHORIZATION
    };

    private Twitter twitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasAccessToken()) {
            // Access has already been set.
            Button buttonLogout = (Button)findViewById(R.id.button_logout);
            buttonLogout.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Access Token found..logged in", Toast.LENGTH_SHORT).show();
        } else {
            // Need to acquire access token and secret.
            twitter = new TwitterFactory().getInstance();
            RequestToken requestToken = null;
            twitter.setOAuthConsumer(Util.CONSUMER_KEY, Util.CONSUMER_SECRET);
            try {
                Toast.makeText(this, "getting request token", Toast.LENGTH_SHORT).show();
                requestToken = twitter.getOAuthRequestToken(Util.CALLBACK_URL);
                Toast.makeText(this, "redirecting to web view", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginWebViewActivity.class);
                intent.putExtra("URL", requestToken.getAuthenticationURL());
                startActivityForResult(intent, REQUEST_CODE.AUTHORIZATION.ordinal());

            } catch (TwitterException e) {
                Toast.makeText(this, "FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * When returned from twitter authorization page.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE.AUTHORIZATION.ordinal()) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Got verifier", Toast.LENGTH_SHORT).show();
                String oauthVerifier = (String) data.getExtras().get("oauth_verifier");
                Toast.makeText(this, "Verifier: " + oauthVerifier, Toast.LENGTH_LONG).show();
                AccessToken at = null;
                try {
                    // Get access token and secret
                    at = twitter.getOAuthAccessToken(oauthVerifier);
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                    // Store access token and secret
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Util.ACCESS_TOKEN, at.getToken());
                    editor.putString(Util.ACCESS_TOKEN_SECRET, at.getTokenSecret());
                    editor.commit();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Credentials cancelled!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * OnClick handler for logout button.
     * 
     * @param logoutButton
     */
    public void onClickLogout(View logoutButton) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        editor.remove(Util.ACCESS_TOKEN);
        editor.remove(Util.ACCESS_TOKEN_SECRET);
        editor.commit();
        logoutButton.setVisibility(View.INVISIBLE);
    }
    
    // TODO: get all tweets and show them in the list.
    private void getTweetList() {
        try {
            ResponseList<Status> statusList = twitter.getUserTimeline();
            ListView list =  (ListView)findViewById(R.id.list_tweets);

            
        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    /**
     * Checks if access token and secret has been stored in preferences.
     * 
     * @return true, if access token and secret is set.
     */
    private boolean hasAccessToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = prefs.getString(Util.ACCESS_TOKEN_SECRET, null);
        String accessSecret = prefs.getString(Util.ACCESS_TOKEN_SECRET, null);
        return accessToken != null && accessSecret != null;
    }
}
