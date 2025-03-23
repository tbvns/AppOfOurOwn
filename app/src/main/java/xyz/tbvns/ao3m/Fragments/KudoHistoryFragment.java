package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.Database.KudosManager;
import xyz.tbvns.ao3m.Views.KudoHistoryEntryView;

import java.util.List;

public class KudoHistoryFragment extends Fragment {
    private int page = 0;
    private int entriesCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kudo_history, container, false);

        List<KudosManager.KudosWork> entries = KudosManager.getKudosWorks(getContext(), page);
        entriesCount += entries.size();

        MainActivity.bar.setTitle("Kudo history");

        LinearLayout layout = view.findViewById(R.id.HistoryList);
        addEntries(entries, layout);

        if (entriesCount == 0) {
            layout.addView(new TextView(getContext()){{
                setText("You haven't kudoed anything yet !");
                setPadding(0, 200, 0, 200);
                setWidth(layout.getWidth());
                setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }});
        }

        view.findViewById(R.id.HistoryScrollView).setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    int maxH = ((ScrollView) v).getChildAt(0).getHeight();
                    if (entriesCount % 100 == 0 && maxH - scrollY <= 200) {
                        page += 1;
                        addEntries(KudosManager.getKudosWorks(getContext(), page), layout);
                    }
                });

        return view;
    }

    public void addEntries(List<KudosManager.KudosWork> entries, LinearLayout view) {
        new Thread(() -> {
            for (KudosManager.KudosWork entry : entries) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    System.out.println("https://archiveofourown.org/works/" + entry.workId);
                    view.addView(new KudoHistoryEntryView(getContext(), entry.workName, entry.workId, entry.kudoDate, getParentFragmentManager()));
                });
            }
        }){{
            setName("AddEntriesThread");
            start();
        }};
    }
}