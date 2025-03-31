package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import xyz.tbvns.ao3m.AO3.APIResponse;
import xyz.tbvns.ao3m.AO3.SearchAPI;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Views.ErrorView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        MainActivity.navigationBar.getMenu().findItem(R.id.navigation_browse).setChecked(true);

        Button byFandomButton = view.findViewById(R.id.fandomButtons);
        byFandomButton.setOnClickListener(a -> {
            FragmentManager manager = getParentFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, new FandomMainFragment());
            ft.addToBackStack("fandom");
            ft.commit();
        });
        Button advancedSearch = view.findViewById(R.id.advancedSearchButton);
        advancedSearch.setOnClickListener(a -> {
            FragmentManager manager = getParentFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, new AdvancedSearchFragment());
            ft.addToBackStack("advancedSearch");
            ft.commit();
        });
        SearchView searchView = view.findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Map<String, String> searchParams = new HashMap<>();
                addIfNotEmpty(searchParams, "work_search[query]", searchView.getQuery().toString());
                String searchUrl = SearchAPI.generateSearchUrl(searchParams);
                SearchResultFragment.showResults(getParentFragmentManager(), searchUrl, false);
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {return true;}
        });

        new Thread(() -> System.out.println("yay !!!!" + WorkAPI.fetchWorks("https://archiveofourown.org/works")));

        new Thread(() -> {
            LoadingFragment loadingFragment = new LoadingFragment();
            new Handler(Looper.getMainLooper()).post(() -> {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.workList, loadingFragment)
                        .commitAllowingStateLoss();
            });
            APIResponse<List<WorkAPI.Work>> worksResponce = WorkAPI.fetchWorks("https://archiveofourown.org/works");
            if (!worksResponce.isSuccess()) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        getChildFragmentManager()
                                .beginTransaction()
                                .replace(R.id.workList, new ErrorScreenFragment(new ErrorView(getContext(), worksResponce.getMessage(), true)))
                                .commitAllowingStateLoss();
                    } catch (Exception e) {}
                });
                return;
            }
            List<WorkAPI.Work> works = worksResponce.getObject();
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.workList, new SearchResultFragment(works))
                            .commitAllowingStateLoss();
                } catch (Exception e) {}
            });
        }).start();

        return view;
    }

    private void addIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }
}