package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Views.ErrorView;

public class ErrorScreenFragment extends Fragment {
    ErrorView ErrorView;

    public ErrorScreenFragment(ErrorView view) {
        this.ErrorView = view;
    }

    public static void show(FragmentManager manager, ErrorView view) {
        ErrorScreenFragment fragment = new ErrorScreenFragment(view);
        manager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("ErrorScreen")
                .commit();
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_error_screen, container, false);
        LinearLayout layout = view.findViewById(R.id.errorScreenMain);
        layout.addView(ErrorView);
        return view;
    }
}