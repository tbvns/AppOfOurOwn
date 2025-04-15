package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import xyz.tbvns.ao3m.Api.WorkAPI;
import xyz.tbvns.ao3m.Activity.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Views.WorkView;

public class LibrairyFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_librairy, container, false);

        MainActivity.bar.setTitle("Library");
        MainActivity.navigationBar.getMenu().findItem(R.id.navigation_librairy).setChecked(true);

        LinearLayout layout = view.findViewById(R.id.mainLibrairy);
        for (WorkAPI.Work work : ConfigManager.getLibraryConf(getContext()).getWorks()) {
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