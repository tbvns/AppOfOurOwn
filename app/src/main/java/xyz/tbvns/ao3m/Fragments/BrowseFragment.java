package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import xyz.tbvns.ao3m.AO3.FandomAPI;
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
//        LinearLayout list = view.findViewById(R.id.Categories);
//        new Thread(() -> {
//            FandomAPI.getCategories().forEach(c -> {
//                new Handler(Looper.getMainLooper()).post(() -> {
//                    list.addView(
//                            new Button(getContext()){{
//                                setText(c.getName());
//                            }}
//                    );
//                });
//            });
//        }).start();


        return view;
    }
}