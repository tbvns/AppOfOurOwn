package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import xyz.tbvns.ao3m.Api.ChaptersAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Activity.ReaderActivity;
import xyz.tbvns.ao3m.Storage.Data.ChapterProgress;
import xyz.tbvns.ao3m.Storage.Database.ChapterProgressManager;

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

        if (ChapterProgressManager.chapterExists(getContext(), Integer.parseInt(chapter.getWork().workId), chapter.getNumber())) {
            findViewById(R.id.SeenIcon).setVisibility(View.GONE);

            ChapterProgress progress = ChapterProgressManager.getChapterProgress(getContext(), Integer.parseInt(chapter.getWork().workId), chapter.getNumber());

            chapterTitle.setText(chapter.getTitle());
            chapterDate.setText(chapter.getDate() + " | " + Math.round(progress.getProgress() * 100) + "%");

            if (Math.round(progress.getProgress() * 100) == 100) {
                chapterTitle.setTextColor(getResources().getColor(R.color.midnightdusk_onSurfaceVariant));
            }
        } else {
            chapterTitle.setText(chapter.getTitle());
            chapterDate.setText(chapter.getDate());
        }

        setOnClickListener(l -> {
            ReaderActivity.showFullscreen(getContext(), chapter, true);
        });
    }
}