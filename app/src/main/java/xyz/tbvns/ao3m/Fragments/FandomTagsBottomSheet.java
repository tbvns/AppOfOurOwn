package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import xyz.tbvns.ao3m.AO3.SearchAPI;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FandomTagsBottomSheet extends BottomSheetDialogFragment {

    private List<String> fandomButtons;
    private List<String> tagsButtons;

    // Constructor to pass button data
    public FandomTagsBottomSheet(List<String> fandomButtons, List<String> tagsButtons) {
        this.fandomButtons = fandomButtons;
        this.tagsButtons = tagsButtons;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        // Get references to the containers
        LinearLayout fandomContainer = view.findViewById(R.id.fandomButtonsContainer);
        LinearLayout tagsContainer = view.findViewById(R.id.tagsButtonsContainer);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.Theme_AO3M);

        // Add Fandom buttons
        for (String buttonText : fandomButtons) {
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
        for (String buttonText : tagsButtons) {
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