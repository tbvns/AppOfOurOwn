package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import xyz.tbvns.ao3m.Api.FandomAPI;
import xyz.tbvns.ao3m.Api.FandomCategoryApi;
import xyz.tbvns.ao3m.Api.FandomCategoryObject;
import xyz.tbvns.ao3m.Activity.MainActivity;
import xyz.tbvns.ao3m.R;

import java.util.List;

public class FandomMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.bar.setTitle("Search by fandom");
        MainActivity.navigationBar.getMenu().findItem(R.id.navigation_browse).setChecked(true);

        View view = inflater.inflate(R.layout.fragment_fandom_main, container, false);

        LinearLayout list = view.findViewById(R.id.fandomMainList);
        new Thread(() -> {
            //TODO: This may cause error (And will cause them). To fix when the error fragment is created
            FandomAPI.getCategories().getObject().forEach(c -> {
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
                                            ft.addToBackStack("fandomList");
                                            ft.commit();
                                        });

                                        //TODO: This may cause error (And will cause them). To fix when the error fragment is created
                                        List<FandomCategoryObject> stream = FandomCategoryApi.getCategoryList("https://archiveofourown.org" + c.getUrl()).getObject();

                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            manager.popBackStack("fandomList", FragmentManager.POP_BACK_STACK_INCLUSIVE); // Prevent stacking multiple times
                                            FragmentTransaction ft = manager.beginTransaction();
                                            ft.replace(R.id.fragment_container, new FandomSubFragment(stream));
                                            ft.addToBackStack("fandomList");
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