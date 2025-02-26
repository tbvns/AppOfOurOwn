package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import xyz.tbvns.ao3m.R;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class WorkFragment extends Fragment {
    private String name;
    private String author;
    private String mainTag;
    private List<String> tags;
    private String desc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work, container, false);
    }
}