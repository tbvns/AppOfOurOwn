package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Views.WorkView;

import java.time.LocalDate;
import java.util.ArrayList;

public class LibrairyFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_librairy, container, false);

        MainActivity.bar.setTitle("Library");

        LinearLayout layout = view.findViewById(R.id.mainLibrairy);
        for (WorkAPI.Work work : ConfigManager.getLibraryConf().getWorks()) {
            layout.addView(new WorkView(getContext(), work));
        }

        if (layout.getChildCount() == 0) {
            layout.addView(new TextView(getContext()) {{
                setText("No works in library, try adding some !");
                setPadding(0, 200, 0, 200);
                setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }});
        }

        return view;
    }
}