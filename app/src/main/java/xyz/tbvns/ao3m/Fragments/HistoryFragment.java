package xyz.tbvns.ao3m.Fragments;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import xyz.tbvns.ao3m.DateSearchGUI;
import xyz.tbvns.ao3m.Activity.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.Database.HistoryManager;
import xyz.tbvns.ao3m.Views.HistoryEntryView;

import java.util.List;


public class HistoryFragment extends Fragment {
    public static HistoryManager.HistoryEntry first;

    private int page = 0;
    private int entriesCount = 0;

    private LinearLayout fabDateRangeContainer, fabSingleDateContainer;
    private FloatingActionButton fabCalendar;
    private boolean isFabOpen = false;
    private boolean isFiltered = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        List<HistoryManager.HistoryEntry> entries = HistoryManager.getHistoryEntriesPaginated(getContext(), page);
        entriesCount += entries.size();

        if (!entries.isEmpty()) {
            first = entries.getFirst();
        }

        MainActivity.bar.setTitle("History");

        LinearLayout layout = view.findViewById(R.id.HistoryList);
        addEntries(entries, layout);

        fabCalendar = view.findViewById(R.id.fab_calendar);
        fabDateRangeContainer = view.findViewById(R.id.fab_date_range_container);
        fabSingleDateContainer = view.findViewById(R.id.fab_single_date_container);

        fabDateRangeContainer.setOnClickListener(l -> {
            DateSearchGUI.showDateRangePicker(MainActivity.main, timestamps -> {
                List<HistoryManager.HistoryEntry> entryList = HistoryManager.getHistoryEntriesByEpocheDate(getContext(), timestamps[0]/1000, (timestamps[1]/1000)+86400);
                layout.removeAllViews();
                isFiltered = false;
                addEntries(entryList, layout);
                isFiltered = true;
                fabCalendar.setImageDrawable(getResources().getDrawable(R.drawable.close_icon));
                fabCalendar.setOnClickListener(v -> {
                    resetAll(layout);
                });
            });
        });

        fabSingleDateContainer.setOnClickListener(l -> {
            DateSearchGUI.showDatePicker(MainActivity.main, timestamp -> {
                List<HistoryManager.HistoryEntry> entryList = HistoryManager.getHistoryEntriesByEpocheDate(getContext(), timestamp/1000, null);
                layout.removeAllViews();
                isFiltered = false;
                addEntries(entryList, layout);
                isFiltered = true;
                fabCalendar.setImageDrawable(getResources().getDrawable(R.drawable.close_icon));
                fabCalendar.setOnClickListener(v -> {
                    resetAll(layout);
                });
            });
        });

        fabCalendar.setOnClickListener(v -> toggleFabMenu());

        if (entriesCount == 0) {
            layout.addView(new TextView(getContext()){{
                setText("Looks quite empty doesn't it ?");
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
                        addEntries(HistoryManager.getHistoryEntriesPaginated(getContext(), page), layout);
                    }
        });

        return view;
    }

    public void addEntries(List<HistoryManager.HistoryEntry> entries, LinearLayout view) {
        new Thread(() -> {
            if (isFiltered) return;
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

    private void toggleFabMenu() {
        if (isFabOpen) {
            animateContainer(fabDateRangeContainer, false);
            animateContainer(fabSingleDateContainer, false);
        } else {
            animateContainer(fabDateRangeContainer, true);
            animateContainer(fabSingleDateContainer, true);
        }
        isFabOpen = !isFabOpen;
    }

    private void animateContainer(View container, boolean show) {
        if (show) {
            container.setVisibility(View.VISIBLE);
            ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(container,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 100f, 0f));
            animation.setDuration(300).start();
        } else {
            ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(container,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, 100f));
            animation.setDuration(200).start();
            container.postDelayed(() -> container.setVisibility(View.INVISIBLE), 200);
        }
    }

    public void resetAll(LinearLayout layout) {
        layout.removeAllViews();
        fabCalendar.setImageDrawable(getResources().getDrawable(R.drawable.calendar_icon));
        fabCalendar.setOnClickListener(v -> toggleFabMenu());
        page = 0;
        List<HistoryManager.HistoryEntry> entries = HistoryManager.getHistoryEntriesPaginated(getContext(), page);
        entriesCount = entries.size();
        isFiltered = false;
        addEntries(entries, layout);
    }
}