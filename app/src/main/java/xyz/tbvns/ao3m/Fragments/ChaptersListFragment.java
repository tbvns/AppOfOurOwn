package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.ReaderActivity;

import java.util.List;

@AllArgsConstructor
public class ChaptersListFragment extends Fragment {
    private List<ChaptersAPI.Chapter> chapters;

    public static void show(FragmentManager manager, String url) {
        new Thread(() -> {
            new Handler((Looper.getMainLooper())).post(() -> {
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new LoadingFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            });
            List<ChaptersAPI.Chapter> chapters = ChaptersAPI.fetchChapters(url);
            new Handler((Looper.getMainLooper())).post(() -> {
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new ChaptersListFragment(chapters))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            });
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapters_list, container, false);

        LinearLayout layout = view.findViewById(R.id.chaptersList);
        for (ChaptersAPI.Chapter chapter : chapters) {
            layout.addView(new TextView(getContext()){{
                setText(chapter.getTitle());
                setOnClickListener(l -> {
                    ReaderActivity.showFullscreen(getParentFragmentManager(), getContext(), chapter);
                });
            }});
        }

        return view;
    }
}