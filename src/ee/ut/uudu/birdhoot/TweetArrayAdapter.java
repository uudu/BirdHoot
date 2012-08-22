package ee.ut.uudu.birdhoot;

import java.util.List;

import twitter4j.Status;
import twitter4j.util.TimeSpanConverter;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TweetArrayAdapter extends ArrayAdapter<Status> {

    private final List<Status> list;
    private final Activity context;
    private final TimeSpanConverter tsc = new TimeSpanConverter();

    public TweetArrayAdapter(Activity context, List<Status> list) {
        super(context, R.layout.list_row_tweets, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView user;
        protected TextView time;
        protected TextView tweet;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            row = inflator.inflate(R.layout.list_row_tweets, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.user = (TextView) row.findViewById(R.id.user);
            viewHolder.time = (TextView) row.findViewById(R.id.time);
            viewHolder.tweet = (TextView) row.findViewById(R.id.tweet);
            row.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) row.getTag();
        }
        Status status = this.list.get(position);
        viewHolder.user.setText(status.getUser().getName());
        viewHolder.time.setText(tsc.toTimeSpanString(status.getCreatedAt()));
        viewHolder.tweet.setText(status.getText());
        return row;
    }
}