package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;

public class BrowseFragment extends Fragment {

    public BrowseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        MainActivity.bar.setTitle("Browse");

        Button byFandomButton = view.findViewById(R.id.fandomButtons);
        byFandomButton.setOnClickListener(a -> {
            FragmentManager manager = getParentFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, new FandomMainFragment());
            ft.commit();
        });


        return view;
    }
}