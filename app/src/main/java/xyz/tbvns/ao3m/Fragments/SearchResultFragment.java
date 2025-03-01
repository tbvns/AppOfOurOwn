package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.AllArgsConstructor;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Views.WorkView;

import java.util.List;

@AllArgsConstructor
public class SearchResultFragment extends Fragment {

    private List<WorkAPI.Work> works;

    public static void showResults(FragmentManager manager, String url) {
        new Thread(() -> {
            new Handler((Looper.getMainLooper())).post(() -> {
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new LoadingFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            });

            List<WorkAPI.Work> works = WorkAPI.fetchWorks(url);
            new Handler((Looper.getMainLooper())).post(() -> {
                manager.beginTransaction()
                        .replace(R.id.fragment_container, new SearchResultFragment(works))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            });
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        LinearLayout layout = view.findViewById(R.id.mainSearch);
        works.forEach(w -> {
            layout.addView(new WorkView(getContext(), w));
        });
        return view;
    }
}