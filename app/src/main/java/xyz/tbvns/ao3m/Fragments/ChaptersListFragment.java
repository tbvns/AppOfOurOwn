package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.AllArgsConstructor;
import xyz.tbvns.ao3m.AO3.ChaptersAPI;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.ChaptersView;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.ReaderActivity;
import xyz.tbvns.ao3m.Storage.Config.LibraryConf;
import xyz.tbvns.ao3m.Storage.ConfigManager;

import java.util.Collections;
import java.util.List;

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
            List<ChaptersAPI.Chapter> chapters = ChaptersAPI.fetchChapters(work);
            new Handler((Looper.getMainLooper())).post(() -> {
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new ChaptersListFragment(chapters, work))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
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
        if (ConfigManager.getLibraryConf().isContained(work)) {
            button.setImageDrawable(getResources().getDrawable(R.drawable.library_filled_icon));
            ((TextView) view.findViewById(R.id.libraryText)).setText("In library");
        }

        button.setOnClickListener(l -> {
            if (ConfigManager.getLibraryConf().isContained(work)) {
                LibraryConf conf = ConfigManager.getLibraryConf();
                conf.removeWork(work);
                ConfigManager.saveLibraryConf(conf);
                button.setImageDrawable(getResources().getDrawable(R.drawable.librairy_icon));
                ((TextView) view.findViewById(R.id.libraryText)).setText("Add to library");
            } else {
                LibraryConf conf = ConfigManager.getLibraryConf();
                conf.addWork(work);
                ConfigManager.saveLibraryConf(conf);
                button.setImageDrawable(getResources().getDrawable(R.drawable.library_filled_icon));
                ((TextView) view.findViewById(R.id.libraryText)).setText("In library");
            }
        });

        return view;
    }
}