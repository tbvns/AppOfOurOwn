package xyz.tbvns.ao3m.Fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import xyz.tbvns.ao3m.AO3.FandomAPI;
import xyz.tbvns.ao3m.AO3.FandomCategoryApi;
import xyz.tbvns.ao3m.AO3.FandomCategoryObject;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;

import java.util.List;
import java.util.stream.Stream;

public class FandomMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.bar.setTitle("Search by fandom");

        View view = inflater.inflate(R.layout.fragment_fandom_main, container, false);

        LinearLayout list = view.findViewById(R.id.fandomMainList);
        new Thread(() -> {
            FandomAPI.getCategories().forEach(c -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Space space = new Space(getContext());
                    space.setMinimumHeight(15);
                    list.addView(space);
                    list.addView(
                            new Button(getContext()){{
                                setText(c.getName());
                                setPadding(0, 0, 0, 0);
                                setBackground(getResources().getDrawable(R.drawable.rounded_searchview));
                                setMaxHeight(30);

                                setOnClickListener(a -> {
                                    new Thread(() -> {
                                        FragmentManager manager = getParentFragmentManager();

                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            FragmentTransaction ft = manager.beginTransaction();
                                            ft.replace(R.id.fragment_container, new LoadingFragment());
                                            ft.commit();
                                        });

                                        System.out.println("https://archiveofourown.org" + c.getUrl());
                                        List<FandomCategoryObject> stream = FandomCategoryApi.getCategoryList("https://archiveofourown.org" + c.getUrl());

                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            FragmentTransaction ft = manager.beginTransaction();
                                            ft.replace(R.id.fragment_container, new FandomSubFragment(stream));
                                            ft.commit();
                                        });

                                    }).start();
                                });
                            }}
                    );
                });
            });
        }).start();

        return view;
    }
}