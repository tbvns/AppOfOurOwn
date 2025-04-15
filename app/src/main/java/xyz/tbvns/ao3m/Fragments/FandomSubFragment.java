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
import xyz.tbvns.ao3m.Api.FandomCategoryObject;
import xyz.tbvns.ao3m.Api.WorkAPI;
import xyz.tbvns.ao3m.Activity.MainActivity;
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

        MainActivity.navigationBar.getMenu().findItem(R.id.navigation_browse).setChecked(true);

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
                                    ft.addToBackStack("subFandom");
                                    ft.commit();
                                });

                                //TODO: This may cause error (And will cause them). To fix when the error fragment is created
                                List<WorkAPI.Work> works = WorkAPI.fetchWorks("https://archiveofourown.org" + obj.getLink()).getObject();
                                SearchResultFragment fragment = new SearchResultFragment(works, true, true);
                                fragment.setUrl("https://archiveofourown.org" + obj.getLink());

                                new Handler(Looper.getMainLooper()).post(() -> {
                                    manager.popBackStack("subFandom", FragmentManager.POP_BACK_STACK_INCLUSIVE); // Prevent stacking multiple times
                                    FragmentTransaction ft = manager.beginTransaction();
                                    ft.replace(R.id.fragment_container, fragment);
                                    ft.addToBackStack("subFandom");
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