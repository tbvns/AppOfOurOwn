package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.Fragments.ChaptersListFragment;
import xyz.tbvns.ao3m.R;

import java.text.SimpleDateFormat;

public class KudoHistoryEntryView extends LinearLayout {
    private String title;
    private String url;
    private FragmentManager manager;
    private long date;
    public KudoHistoryEntryView(Context context, String title, String url, long date, FragmentManager manager) {
        super(context);
        this.title = title;
        this.url = url;
        this.manager = manager;
        this.date = date;
        innit();
    }

    public void innit() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_history_entry, this, true);

        TextView chapterTitle = findViewById(R.id.ChapterTitle);
        TextView chapterDate = findViewById(R.id.ChapterDate);

        chapterTitle.setText(title);
        String text = new SimpleDateFormat("HH:mm MM/dd/YYYY").format(date * 10e2);
        chapterDate.setText(text);

        setOnClickListener(l -> {
            new Thread(() -> {
                //TODO: This will causes issue, to fix in the future.
                WorkAPI.Work work = WorkAPI.fetchWork(url).getObject();
                ChaptersListFragment.show(manager, work);
            }).start();
        });
    }
}