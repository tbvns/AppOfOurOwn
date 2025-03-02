package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Views.WorkView;

import java.time.LocalDate;
import java.util.ArrayList;

public class LibrairyFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_librairy, container, false);

        LinearLayout layout = view.findViewById(R.id.mainLibrairy);
        layout.addView(new WorkView(getContext(), new WorkAPI.Work(
                "",
                "Hello",
                "World",
                "Go brrrrrr",
                new ArrayList<String>(){{
                    add("Fandom test");
                }},
                new ArrayList<String>(){{
                    add("Tag test");
                }},
                new WorkAPI.Classification(){{
                    status = WorkAPI.Status.completed;
                    relationship = WorkAPI.Relationship.mm;
                    warning = WorkAPI.Warning.none;
                    contentRating = WorkAPI.ContentRating.none;
                }},
                "That's a sumarry",
                "English",
                0, 0, 0, 0, 0, 0,
                LocalDate.now()
        )));
        layout.addView(new WorkView(getContext(), new WorkAPI.Work(
                "",
                "Hello 2",
                "Me",
                "Go brrrrrr",
                new ArrayList<String>(){{
                    add("Fandom test 2");
                }},
                new ArrayList<String>(){{
                    add("Tag test");
                }},
                new WorkAPI.Classification(){{
                    status = WorkAPI.Status.incomplete;
                    relationship = WorkAPI.Relationship.multi;
                    warning = WorkAPI.Warning.warning;
                    contentRating = WorkAPI.ContentRating.mature;
                }},
                "That's a sumarry vesrion 2 and ths time it is long long long long long long long long long long long long long long long long long long long long long long",
                "English",
                0, 0, 0, 0, 0, 0,
                LocalDate.now()
        )));
        layout.addView(new WorkView(getContext(), new WorkAPI.Work(
                "",
                "Hello 2",
                "Me",
                "Go brrrrrr",
                new ArrayList<String>(){{
                    add("Fandom test 2");
                }},
                new ArrayList<String>(){{
                    add("Tag test");
                }},
                new WorkAPI.Classification(){{
                    status = WorkAPI.Status.completed;
                    relationship = WorkAPI.Relationship.gen;
                    warning = WorkAPI.Warning.none;
                    contentRating = WorkAPI.ContentRating.general;
                }},
                "That's a sumarry vesrion 3 and ths time it is long long long long long long long long long long long long long long long long long long long long long long",
                "English",
                0, 0, 0, 0, 0, 0,
                LocalDate.now()
        )));


        return view;
    }
}