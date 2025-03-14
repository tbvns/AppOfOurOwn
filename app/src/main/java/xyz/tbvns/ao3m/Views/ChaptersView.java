package xyz.tbvns.ao3m;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import xyz.tbvns.ao3m.AO3.ChaptersAPI;

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