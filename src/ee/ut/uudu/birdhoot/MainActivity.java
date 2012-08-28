package ee.ut.uudu.birdhoot;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import ee.ut.uudu.birdhoot.UpdateStatusDialogFragment.UpdateStatusDialogListener;

public class MainActivity extends FragmentActivity implements UpdateStatusDialogListener {

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
        // User already has access token.
        try {
            this.twitter = getTwitter();
            if (hasAccessToken()) {
                // Show log out button.
                View buttonLogout = findViewById(R.id.button_logout);
                buttonLogout.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Access Token found..logged in", Toast.LENGTH_SHORT).show();
            } else {
                RequestToken requestToken = twitter.getOAuthRequestToken(Util.CALLBACK_URL);
                Intent intent = new Intent(this, LoginWebViewActivity.class);
                intent.putExtra("URL", requestToken.getAuthenticationURL());
                startActivityForResult(intent, REQUEST_CODE.AUTHORIZATION.ordinal());
            }
        } catch (TwitterException e) {
            Toast.makeText(this, "FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * When returned from twitter authorization page.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE.AUTHORIZATION.ordinal()) {
            switch (resultCode) {
            case RESULT_OK:
                String oauthVerifier = (String) data.getExtras().get(LoginWebViewActivity.OAUTH_VERIFIER_KEY);
                AccessToken at = null;
                try {
                    // Get access token and secret
                    at = twitter.getOAuthAccessToken(oauthVerifier);
                    storeAccessToken(at);
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
     * OnClick handler for logout button.
     * 
     * @param logoutButton
     */
    public void onClickLogout(View logoutButton) {
        getTwitter().shutdown();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        editor.remove(Util.ACCESS_TOKEN);
        editor.remove(Util.ACCESS_TOKEN_SECRET);
        editor.commit();
        logoutButton.setVisibility(View.INVISIBLE);
    }

    public void onClickUpdateStatus(View tweetButton) {
        FragmentManager fm = getSupportFragmentManager();
        UpdateStatusDialogFragment usdf = UpdateStatusDialogFragment.newInstance(TextDialogRole.UPDATE_STATUS);
        usdf.show(fm, "fragment_update_status");
    }

    public void onClickHome(View homeButton) {
        new getHomeTimelineAsyncTask().execute();
    }

    public void onClickSearchTweets(View searchButton) {
        FragmentManager fm = getSupportFragmentManager();
        UpdateStatusDialogFragment usdf = UpdateStatusDialogFragment.newInstance(TextDialogRole.SEARCH_TWEETS);
        usdf.show(fm, "fragment_update_status");
    }

    private void showHomeTimeline() throws TwitterException {
        ResponseList<Status> statusList = twitter.getHomeTimeline();
        ListView list = (ListView) findViewById(R.id.list_tweets);
        TweetArrayAdapter<Status> statusAdapter = new TweetArrayAdapter<Status>(this, statusList);
        list.setAdapter(statusAdapter);
    }

    /**
     * Checks if access token and secret has been stored in preferences.
     * 
     * @return true, if access token and secret is set.
     */
    public boolean hasAccessToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = prefs.getString(Util.ACCESS_TOKEN, null);
        String accessSecret = prefs.getString(Util.ACCESS_TOKEN_SECRET, null);
        return accessToken != null && accessSecret != null;
    }

    @Override
    public void onDialogTextEnter(String text, TextDialogRole role) {
        try {
            switch (role) {
            case UPDATE_STATUS:
                updateStatus(text);
                break;
            case SEARCH_TWEETS:
                // TODO: Search tweets
                searchTweets(text);
                break;
            }
        } catch (TwitterException e) {
            Toast.makeText(this, "FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }

    private void updateStatus(String tweet) throws TwitterException {
        Toast.makeText(this, "Tweeted: " + tweet, Toast.LENGTH_LONG).show();
        if (!twitter4j.util.CharacterUtil.isExceedingLengthLimitation(tweet)) {
            twitter.updateStatus(tweet);
            // Refresh tweet list or add status on top of tweets list
            showHomeTimeline();
        }
    }

    private void searchTweets(String text) throws TwitterException {
        Toast.makeText(this, "search for: " + text, Toast.LENGTH_LONG).show();
        Query q = new Query();
        q.setQuery(text);
        QueryResult qr = twitter.search(q);
        List<Tweet> tweetList = qr.getTweets();
        Toast.makeText(this, "results: " + tweetList.size(), Toast.LENGTH_LONG).show();
        ListView list = (ListView) findViewById(R.id.list_tweets);
        TweetArrayAdapter<Tweet> tweetAdapter = new TweetArrayAdapter<Tweet>(this, tweetList);
        list.setAdapter(tweetAdapter);
    }

    private Twitter getTwitter() {
        if (this.twitter == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            ConfigurationBuilder b = new ConfigurationBuilder();
            b.setOAuthConsumerKey(Util.CONSUMER_KEY);
            b.setOAuthConsumerSecret(Util.CONSUMER_SECRET);
            if (prefs.contains(Util.ACCESS_TOKEN) && prefs.contains(Util.ACCESS_TOKEN_SECRET)) {
                b.setOAuthAccessToken(prefs.getString(Util.ACCESS_TOKEN, null));
                b.setOAuthAccessTokenSecret(prefs.getString(Util.ACCESS_TOKEN_SECRET, null));
            }
            this.twitter = new TwitterFactory(b.build()).getInstance();
        }
        return this.twitter;
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

    private class getHomeTimelineAsyncTask extends AsyncTask<Void, Void, ResponseList<Status>> {

        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                return getTwitter().getHomeTimeline();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> result) {
            ListView list = (ListView) findViewById(R.id.list_tweets);
            TweetArrayAdapter<twitter4j.Status> statusAdapter = new TweetArrayAdapter<twitter4j.Status>(
                    MainActivity.this, result);
            list.setAdapter(statusAdapter);

        }

    }

}
