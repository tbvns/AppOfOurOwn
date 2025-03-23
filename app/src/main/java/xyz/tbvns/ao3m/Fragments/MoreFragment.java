package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import xyz.tbvns.ao3m.LoginActivity;
import xyz.tbvns.ao3m.R;

public class MoreFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        view.findViewById(R.id.LoginButton).setOnClickListener(v -> {
            LoginActivity.show(getContext());
        });

        view.findViewById(R.id.KudoHistoryButton).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new KudoHistoryFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("Settings")
                    .commit();
        });

        return view;
    }
}