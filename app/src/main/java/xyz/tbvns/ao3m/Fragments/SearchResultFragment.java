package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.AllArgsConstructor;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Views.WorkView;

import java.util.List;

@AllArgsConstructor
public class SearchResultFragment extends Fragment {

    private List<WorkAPI.Work> works;

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