package xyz.tbvns.ao3m.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import lombok.AllArgsConstructor;
import xyz.tbvns.ao3m.Activity.ReaderActivity;
import xyz.tbvns.ao3m.Api.APIResponse;
import xyz.tbvns.ao3m.Api.ChaptersAPI;
import xyz.tbvns.ao3m.Api.KudosAPI;
import xyz.tbvns.ao3m.Api.WorkAPI;
import xyz.tbvns.ao3m.*;
import xyz.tbvns.ao3m.Activity.LoginActivity;
import xyz.tbvns.ao3m.Activity.MainActivity;
import xyz.tbvns.ao3m.Activity.WebActivity;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Storage.Data.LibraryData;
import xyz.tbvns.ao3m.Storage.Database.ChapterProgressManager;
import xyz.tbvns.ao3m.Storage.Database.KudosManager;
import xyz.tbvns.ao3m.Views.ChaptersView;
import xyz.tbvns.ao3m.Views.ErrorView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ChaptersListFragment extends Fragment {
    private List<ChaptersAPI.Chapter> chapters;
    private WorkAPI.Work work;

    public static void show(FragmentManager manager, WorkAPI.Work work) {
        new Thread(() -> {
            new Handler((Looper.getMainLooper())).post(() -> {
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new LoadingFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack("ChaptersList")
                        .commit();
            });

            APIResponse<List<ChaptersAPI.Chapter>> chaptersResponce = ChaptersAPI.fetchChapters(work);
            if (!chaptersResponce.isSuccess()) {
                new Handler((Looper.getMainLooper())).post(() -> {
                    manager.popBackStack("ChaptersList", FragmentManager.POP_BACK_STACK_INCLUSIVE); // Prevent stacking multiple times
                    manager.beginTransaction()
                            .replace(R.id.fragment_container, new ErrorScreenFragment(new ErrorView(MainActivity.main.getApplicationContext(), chaptersResponce.getMessage(), true)))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .addToBackStack("ChaptersList")
                            .commit();
                });
                return;
            }
            List<ChaptersAPI.Chapter> chapters = ChaptersAPI.fetchChapters(work).getObject();


            new Handler((Looper.getMainLooper())).post(() -> {
                manager.popBackStack("ChaptersList", FragmentManager.POP_BACK_STACK_INCLUSIVE); // Prevent stacking multiple times
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new ChaptersListFragment(chapters, work))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack("ChaptersList")
                        .commit();
            });
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapters_list, container, false);

        LinearLayout layout = view.findViewById(R.id.chaptersList);
        Collections.reverse(chapters);
        for (ChaptersAPI.Chapter chapter : chapters) {
            layout.addView(new ChaptersView(getContext(), chapter, getParentFragmentManager()));
        }

        TextView textView = view.findViewById(R.id.titleChapterList);
        textView.setText(work.title);
        textView = view.findViewById(R.id.descChapterList);
        textView.setText(work.summary);
        textView = view.findViewById(R.id.authorChapterList);
        textView.setText(work.author);

        ImageButton button = view.findViewById(R.id.libraryButton);
        if (ConfigManager.getLibraryConf(getContext()).isContained(work)) {
            button.setImageDrawable(getResources().getDrawable(R.drawable.library_filled_icon));
            ((TextView) view.findViewById(R.id.libraryText)).setText("In library");
        }

        button.setOnClickListener(l -> {
            if (ConfigManager.getLibraryConf(getContext()).isContained(work)) {
                LibraryData conf = ConfigManager.getLibraryConf(getContext());
                conf.removeWork(work);
                ConfigManager.saveLibraryConf(conf, getContext());
                button.setImageDrawable(getResources().getDrawable(R.drawable.librairy_icon));
                ((TextView) view.findViewById(R.id.libraryText)).setText("Add to library");
            } else {
                LibraryData conf = ConfigManager.getLibraryConf(getContext());
                conf.addWork(work);
                ConfigManager.saveLibraryConf(conf, getContext());
                button.setImageDrawable(getResources().getDrawable(R.drawable.library_filled_icon));
                ((TextView) view.findViewById(R.id.libraryText)).setText("In library");
            }
        });

        ImageButton button1 = view.findViewById(R.id.moreButton);
        button1.setOnClickListener(v -> {
            ChapterListBottomSheetInfo sheetInfo = new ChapterListBottomSheetInfo(work);
            sheetInfo.show(MainActivity.main.getSupportFragmentManager(), sheetInfo.getTag());
        });

        ImageButton button2 = view.findViewById(R.id.kudoButton);
        if (KudosManager.workExists(getContext(), work.workId)) {
            button2.setImageDrawable(getResources().getDrawable(R.drawable.likes_icon_filled));
        } else {
            button2.setOnClickListener(v -> {
                new Thread(() -> {
                    try {
                        new Handler(Looper.getMainLooper()).post(() -> button2.setImageDrawable(getResources().getDrawable(R.drawable.likes_icon_filled)));
                        if (!KudosAPI.giveKudos("https://archiveofourown.org/works/" + work.workId))
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Already kudoed !", Toast.LENGTH_LONG).show());
                        KudosManager.addWork(getContext(), new KudosManager.KudosWork(work.workId, work.title, Instant.now().getEpochSecond(), "{}"));
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {

                                if (e.getMessage().contains("/users/login")) {
                                    LoginActivity.show(getContext());
                                }

                                Toast.makeText(getContext(), "Kudo error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                button2.setImageDrawable(getResources().getDrawable(R.drawable.likes_icon));
                            } catch (Exception ignored) {}
                        });
                    }
                }).start();
            });
        }

        ImageButton button3 = view.findViewById(R.id.webButton);
        button3.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), WebActivity.class);
            intent.putExtra("url", "https://archiveofourown.org/works/" + work.workId);
            startActivity(intent);
        });

        FloatingActionButton playButton = view.findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> {
            List<ChaptersAPI.Chapter> temp = chapters;
            Collections.reverse(temp);
            List<ChaptersAPI.Chapter> chapterList = temp.stream().filter(c -> {
                if (ChapterProgressManager.chapterExists(getContext(), Integer.parseInt(c.getWork().workId), c.getNumber()))
                    return ChapterProgressManager.getChapterProgress(getContext(), Integer.parseInt(c.getWork().workId), c.getNumber()).getProgress() <= 0.99;
                return true;
            }).collect(Collectors.toList());
            if (!chapterList.isEmpty()) {
                ReaderActivity.showFullscreen(getContext(), chapterList.get(0), true);
            } else {
                Toast.makeText(getContext(), "No available chapter !", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}