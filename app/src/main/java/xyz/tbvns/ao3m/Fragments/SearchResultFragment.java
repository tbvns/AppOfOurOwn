package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.Api.WorkAPI;
import xyz.tbvns.ao3m.Activity.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Utils;
import xyz.tbvns.ao3m.Views.WorkView;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class SearchResultFragment extends Fragment {
    private List<WorkAPI.Work> works;
    private boolean isEditable;
    private boolean showEditIcon;
    private Map<String, String> params;
    public SearchResultFragment(List<WorkAPI.Work> works, boolean isEditable, boolean showEditIcon) {
        this.works = works;
        this.isEditable = isEditable;
        this.showEditIcon = showEditIcon;
    }

    public SearchResultFragment(List<WorkAPI.Work> works, boolean isEditable, boolean showEditIcon, Map<String, String> params) {
        this.works = works;
        this.isEditable = isEditable;
        this.showEditIcon = showEditIcon;
        this.params = params;
    }


    private int amount = 0;
    private int page = 1;
    private boolean fetching = false;
    @Setter private String url;


    public static void showResults(FragmentManager manager, String url, boolean backStack) {
        showResults(manager, url, backStack, false, true, null);
    }

    public static void showResults(FragmentManager manager, String url, boolean backStack, boolean isEditable, boolean showEditIcon, Map<String, String> editParam) {
        new Thread(() -> {
            new Handler((Looper.getMainLooper())).post(() -> {
                FragmentTransaction transaction = manager.beginTransaction()
                        .replace(R.id.fragment_container, new LoadingFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                if (backStack) {
                    transaction.addToBackStack("Result");
                }
                transaction.commit();
            });

            //TODO: This may cause error (And will cause them). To fix when the error fragment is created
            List<WorkAPI.Work> works = WorkAPI.fetchWorks(url).getObject();
            SearchResultFragment fragment = new SearchResultFragment(works, isEditable, showEditIcon, editParam);
            fragment.setUrl(url);
            new Handler((Looper.getMainLooper())).post(() -> {
                if (backStack) {
                    manager.popBackStack("Result", FragmentManager.POP_BACK_STACK_INCLUSIVE); // Prevent stacking multiple times
                }
                FragmentTransaction transaction= manager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                if (backStack) {
                    transaction.addToBackStack("Result");
                }
                transaction.commit();
            });
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        LinearLayout layout = view.findViewById(R.id.mainSearch);
        amount = works.size();
        works.forEach(w -> {
            layout.addView(new WorkView(getContext(), w));
        });
        if (works.isEmpty()) {
            layout.addView(new TextView(getContext()){{
                setText("No result found !");
                setGravity(Gravity.CENTER);
                layout.setGravity(Gravity.CENTER);
                setPadding(0, 30, 0, 0);
                setTextSize(20);
            }});
        }

        ActionBar actionBar = MainActivity.bar;
        if (actionBar != null) {
            if (showEditIcon) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // Add MenuProvider to show the edit button
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(Menu menu, MenuInflater menuInflater) {
                // Clear any existing items, optional
                menu.clear();
                if (showEditIcon) {
                    menuInflater.inflate(R.menu.search_result_menu, menu);
                }
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_edit) {
                    if (isEditable && params != null) {
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new AdvancedSearchFragment(params))
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.addToBackStack("Result");
                        transaction.commit();
                    } else {
                        getParentFragmentManager().popBackStack();
                    }
                    return true;
                } else {
                    getParentFragmentManager().popBackStack();
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        ScrollView scrollView = view.findViewById(R.id.resultScrollView);
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
            if (maxScroll - scrollY <= 1000) {
                addPage(layout);
            }
        });

        return view;
    }

    @SneakyThrows
    public void addPage(LinearLayout layout) {
        if (amount % 20 == 0 && !fetching) {
            fetching = true;
            new Thread(() -> {
                page++;
                ProgressBar progressBar = new ProgressBar(getContext());

                new Handler(Looper.getMainLooper()).post(() -> {
                    layout.addView(progressBar);
                });

                try {
                    String localUrl = url + "&page=" + page;

                    if (new URL(url).getQuery() == null) {
                        localUrl = url + "?page=" + page;
                    }

                    //TODO: This may cause error (And will cause them). To fix when the error fragment is created
                    List<WorkAPI.Work> fetched = WorkAPI.fetchWorks(localUrl).getObject();

                    new Handler(Looper.getMainLooper()).post(() -> layout.removeView(progressBar));
                    fetched.forEach(w -> {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            layout.addView(new WorkView(getContext(), w));
                        });
                        Utils.sleep(50);
                    });

                    fetching = false;
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        layout.removeView(progressBar);
                        TextView error = new TextView(getContext()){{
                            setText("Failed to load more works: " + e.getMessage());
                        }};
                        layout.addView(error);
                        layout.addView(new Button(getContext()){{
                            setText("Retry");
                            setOnClickListener(l -> {
                                page--;
                                layout.removeView(this);
                                layout.removeView(error);
                                addPage(layout);
                            });
                        }});
                    });
                }
            }).start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ActionBar actionBar = MainActivity.bar;
        if (actionBar != null && showEditIcon) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
}