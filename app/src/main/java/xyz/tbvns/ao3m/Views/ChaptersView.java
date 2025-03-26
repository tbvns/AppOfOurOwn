package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import xyz.tbvns.ao3m.AO3.ChaptersAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.ReaderActivity;

public class ChaptersView extends LinearLayout {
    private ChaptersAPI.Chapter chapter;
    private FragmentManager manager;
    public ChaptersView(Context context, ChaptersAPI.Chapter chapter, FragmentManager manager) {
        super(context);
        this.chapter = chapter;
        this.manager = manager;
        innit();
    }

    public void innit() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_chapters, this, true);

        TextView chapterTitle = findViewById(R.id.ChapterTitle);
        TextView chapterDate = findViewById(R.id.ChapterDate);

        chapterTitle.setText(chapter.getTitle());
        chapterDate.setText(chapter.getDate());

        setOnClickListener(l -> {
            ReaderActivity.showFullscreen(manager, getContext(), chapter);
        });
    }
}