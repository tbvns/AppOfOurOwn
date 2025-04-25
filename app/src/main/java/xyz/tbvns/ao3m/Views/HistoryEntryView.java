package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import xyz.tbvns.ao3m.Api.ChaptersAPI;
import xyz.tbvns.ao3m.Activity.ErrorActivity;
import xyz.tbvns.ao3m.Activity.LoadingActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Activity.ReaderActivity;
import xyz.tbvns.ao3m.Storage.Database.HistoryManager;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class HistoryEntryView extends LinearLayout {
    private HistoryManager.HistoryEntry entry;
    private FragmentManager manager;
    public HistoryEntryView(Context context, HistoryManager.HistoryEntry entry, FragmentManager manager) {
        super(context);
        this.entry = entry;
        this.manager = manager;
        innit();
    }

    public void innit() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_history_entry, this, true);

        TextView chapterTitle = findViewById(R.id.ChapterTitle);
        TextView chapterDate = findViewById(R.id.ChapterDate);

        chapterTitle.setText(entry.getName());
        String text = new SimpleDateFormat("HH:mm MM/dd/YYYY").format(entry.getEpocheDate() * 10e2) + " • Chap. " + (entry.getChapter() + 1);
        if (entry.getLength() != 0) {
            text += " to " + (entry.getChapter() + entry.getLength() + 1);
        }
        chapterDate.setText(text);

        findViewById(R.id.chapter_item_root).setOnClickListener(l -> {
            new Thread(() -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent loadingIntent = new Intent(getContext(), LoadingActivity.class);
                    loadingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    getContext().startActivity(loadingIntent);
                });

                try {
                    ChaptersAPI.Chapter chapter = entry.getChapterObj();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        ReaderActivity.showFullscreen(getContext(), chapter, false);
                    });
                } catch (IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        ErrorActivity.show(e.getMessage(), getContext());
                    });
                }
            }).start();
        });
    }
}