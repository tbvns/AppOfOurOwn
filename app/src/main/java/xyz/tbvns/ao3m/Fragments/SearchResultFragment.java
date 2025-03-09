package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.*;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Utils;
import xyz.tbvns.ao3m.Views.WorkView;

import java.net.URL;
import java.util.List;

public class SearchResultFragment extends Fragment {
    private List<WorkAPI.Work> works;
    public SearchResultFragment(List<WorkAPI.Work> works) {
        this.works = works;
    }

    private int amount = 0;
    private int page = 1;
    private boolean fetching = false;
    @Setter private String url;

    public static void showResults(FragmentManager manager, String url, boolean backStack) {
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


            List<WorkAPI.Work> works = WorkAPI.fetchWorks(url);
            SearchResultFragment fragment = new SearchResultFragment(works);
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

                    List<WorkAPI.Work> fetched = WorkAPI.fetchWorks(localUrl);

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
}