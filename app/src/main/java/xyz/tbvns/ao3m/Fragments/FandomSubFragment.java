package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.tbvns.ao3m.AO3.FandomCategoryApi;
import xyz.tbvns.ao3m.AO3.FandomCategoryObject;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class FandomSubFragment extends Fragment {

    @Getter
    private final List<FandomCategoryObject> stream;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fandom_sub, container, false);
        LinearLayout layout = view.findViewById(R.id.fandomSubList);

        new Thread(() -> {
            AtomicBoolean exit = new AtomicBoolean(false);
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.Theme_AO3M);
            for (int i = 0; i < stream.size(); i++) {
                FandomCategoryObject obj = stream.get(i);
                Button button;
                Space space;
                try {
                     button = new Button(contextThemeWrapper, null, R.style.Theme_AO3M){{
                        setText(obj.getName());
                        setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                        setOnClickListener(l -> {
                            new Thread(() -> {
                                FragmentManager manager = getParentFragmentManager();

                                new Handler(Looper.getMainLooper()).post(() -> {
                                    FragmentTransaction ft = manager.beginTransaction();
                                    ft.replace(R.id.fragment_container, new LoadingFragment());
                                    ft.commit();
                                });

                                System.out.println("https://archiveofourown.org" + obj.getLink());
                                List<WorkAPI.Work> works = WorkAPI.fetchWorks("https://archiveofourown.org" + obj.getLink());

                                new Handler(Looper.getMainLooper()).post(() -> {
                                    FragmentTransaction ft = manager.beginTransaction();
                                    ft.replace(R.id.fragment_container, new SearchResultFragment(works));
                                    ft.commit();
                                });

                            }).start();
                        });
                    }};

                    space = new Space(getContext());
                    space.setMinimumHeight(20);
                } catch (Exception e) {
                    break;
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        layout.addView(button);
                        layout.addView(space);
                    } catch (Exception e) {
                        exit.set(true);
                    }
                });
                Utils.sleep(5);
                if (exit.get()) {
                    break;
                }
            }
        }).start();
        return view;
    }
}