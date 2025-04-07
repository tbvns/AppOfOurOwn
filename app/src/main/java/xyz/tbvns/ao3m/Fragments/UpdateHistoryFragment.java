package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Storage.Data.UpdatesHistoryData;
import xyz.tbvns.ao3m.Storage.Database.HistoryManager;
import xyz.tbvns.ao3m.Views.HistoryEntryView;

import java.util.ArrayList;
import java.util.List;


public class UpdateHistoryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_no_date, container, false);

        MainActivity.bar.setTitle("History");

        List<HistoryManager.HistoryEntry> entries = new ArrayList<>();
        for (UpdatesHistoryData.Entry entry : ConfigManager.getUpdateHistoryData(getContext()).getEntries()) {
            HistoryManager.HistoryEntry historyEntry = new HistoryManager.HistoryEntry(Integer.parseInt(entry.getWork().workId), entry.getWork().title, entry.getDate(), entry.getChapterID() - 1, 0);
            entries.add(historyEntry);
        }

        LinearLayout layout = view.findViewById(R.id.HistoryList);
        addEntries(entries, layout);

        if (entries.size() == 0) {
            layout.addView(new TextView(getContext()){{
                setText("Wow, no update ?");
                setPadding(0, 200, 0, 200);
                setWidth(layout.getWidth());
                setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }});
        }

        return view;
    }

    public void addEntries(List<HistoryManager.HistoryEntry> entries, LinearLayout view) {
        new Thread(() -> {
            for (HistoryManager.HistoryEntry entry : entries) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    view.addView(new HistoryEntryView(getContext(), entry, getParentFragmentManager()));
                });
            }
        }){{
            setName("AddEntriesThread");
            start();
        }};
    }
}