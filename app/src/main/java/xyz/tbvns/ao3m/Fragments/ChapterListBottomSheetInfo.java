package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import lombok.AllArgsConstructor;
import xyz.tbvns.ao3m.AO3.SearchAPI;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Utils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ChapterListBottomSheetInfo extends BottomSheetDialogFragment {
    private WorkAPI.Work work;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_list_bottom_sheet_info, container, false);

        ((TextView) view.findViewById(R.id.hits)).setText(Utils.simplifyNumber(work.hits));
        ((TextView) view.findViewById(R.id.words)).setText(Utils.simplifyNumber(work.wordCount));
        if (work.chapterMax != -1) {
            ((TextView) view.findViewById(R.id.chapters)).setText(work.chapterCount + "/" + work.chapterMax);
        } else {
            ((TextView) view.findViewById(R.id.chapters)).setText(work.chapterCount + "/?");
        }
        ((TextView) view.findViewById(R.id.kudos)).setText(Utils.simplifyNumber(work.kudos));
        ((TextView) view.findViewById(R.id.bookmark)).setText(Utils.simplifyNumber(work.bookmarks));
        ((TextView) view.findViewById(R.id.update)).setText(work.publishedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        ((TextView) view.findViewById(R.id.language)).setText(work.language);


        // Get references to the containers
        LinearLayout fandomContainer = view.findViewById(R.id.fandomButtonsContainer);
        LinearLayout tagsContainer = view.findViewById(R.id.tagsButtonsContainer);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.Theme_AO3M);

        // Add Fandom buttons
        for (String buttonText : work.fandoms) {
            Button button = new Button(contextThemeWrapper, null, R.style.Theme_AO3M);
            button.setText(buttonText);
            button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            button.setBackground(null);
            button.setOnClickListener(v -> {
                Map<String, String> searchParams = new HashMap<>();
                searchParams.put("work_search[fandom_names]", buttonText);
                SearchResultFragment.showResults(getParentFragmentManager(), SearchAPI.generateSearchUrl(searchParams), true);
                dismiss();
            });
            fandomContainer.addView(button);
        }

        // Add Tags buttons
        for (String buttonText : work.tags) {
            Button button = new Button(contextThemeWrapper, null, R.style.Theme_AO3M);
            button.setText(buttonText);
            button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            button.setBackground(null);
            button.setOnClickListener(v -> {
                Map<String, String> searchParams = new HashMap<>();
                searchParams.put("work_search[freeform_names]", buttonText);
                SearchResultFragment.showResults(getParentFragmentManager(), SearchAPI.generateSearchUrl(searchParams), true);
                dismiss();
            });
            tagsContainer.addView(button);
        }


        return view;
    }
}