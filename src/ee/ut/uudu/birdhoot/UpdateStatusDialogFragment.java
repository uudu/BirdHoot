package ee.ut.uudu.birdhoot;

import twitter4j.util.CharacterUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import ee.ut.uudu.birdhoot.UpdateStatusDialogFragment.UpdateStatusDialogListener.TextDialogRole;

public class UpdateStatusDialogFragment extends DialogFragment implements OnEditorActionListener {

    private static final String ROLE_KEY = "key_role";

    private EditText editText;
    private TextDialogRole role;

    public interface UpdateStatusDialogListener {

        public enum TextDialogRole {
            UPDATE_STATUS, SEARCH_TWEETS
        };

        void onDialogTextEnter(String tweet, TextDialogRole what);
    }

    static UpdateStatusDialogFragment newInstance(TextDialogRole role) {
        UpdateStatusDialogFragment f = new UpdateStatusDialogFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(ROLE_KEY, role.name());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        role = TextDialogRole.valueOf(getArguments().getString(ROLE_KEY));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.fragment_update_status, container);
        editText = (EditText) frag.findViewById(R.id.update_status_text);
        switch (role) {
        case SEARCH_TWEETS:
            getDialog().setTitle(R.string.title_search_tweets_dialog);
            break;
        case UPDATE_STATUS:
            getDialog().setTitle(R.string.title_update_status_dialog);
            break;
        }
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        editText.setOnEditorActionListener(this);
        return frag;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            String text = editText.getText().toString();

            switch (role) {
            case UPDATE_STATUS:
                int length = CharacterUtil.count(text);
                if (CharacterUtil.isExceedingLengthLimitation(text)) {
                    String message = getString(R.string.warn_tweet_length_exceeded, Integer.valueOf(length - 140));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    return false;
                } else if (length == 0) {
                    Toast.makeText(getActivity(), "Zero length tweets not allowed", Toast.LENGTH_LONG).show();
                    return false;
                }
                break;
            case SEARCH_TWEETS:
                break;
            }

            UpdateStatusDialogListener activity = (UpdateStatusDialogListener) getActivity();
            activity.onDialogTextEnter(text, role);
            this.dismiss();
            return true;
        }
        return false;
    }
}
