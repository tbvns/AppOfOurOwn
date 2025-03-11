package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import xyz.tbvns.ao3m.AO3.ChaptersAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.ReaderActivity;
import xyz.tbvns.ao3m.Storage.HistoryManager;

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
        String text = new SimpleDateFormat("HH:mm MM/dd/YYYY").format(entry.getEpocheDate()) + " • Chap. " + (entry.getChapter() + 1);
        if (entry.getLength() != 0) {
            text += " to " + (entry.getChapter() + entry.getLength() + 1);
        }
        chapterDate.setText(text);

        setOnClickListener(l -> {
//            ReaderActivity.showFullscreen(manager, getContext(), entry.); TODO: make this work
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
            });
        });
    }
}