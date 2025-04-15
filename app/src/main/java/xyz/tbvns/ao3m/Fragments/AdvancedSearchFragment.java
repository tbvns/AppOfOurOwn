package xyz.tbvns.ao3m.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import lombok.NoArgsConstructor;
import xyz.tbvns.ao3m.Api.SearchAPI;
import xyz.tbvns.ao3m.Activity.MainActivity;
import xyz.tbvns.ao3m.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class AdvancedSearchFragment extends Fragment {
    private Map<String, String> params;

    public AdvancedSearchFragment(Map<String, String> params) {
        super();
        this.params = params;
    }

    // UI Components
    private EditText etAnyField, etTitle, etAuthor, etDate, etWordCount;
    private RadioGroup rgCompletionStatus, rgCrossovers;
    private CheckBox cbSingleChapter;
    private Spinner spinnerLanguage, spinnerRating, spinnerSortColumn, spinnerSortDirection;
    private Button btnSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advanced_search, container, false);

        // Initialize views
        etAnyField = view.findViewById(R.id.et_any_field);
        etTitle = view.findViewById(R.id.et_title);
        etAuthor = view.findViewById(R.id.et_author);
        etDate = view.findViewById(R.id.et_date);
        etWordCount = view.findViewById(R.id.et_word_count);
        rgCompletionStatus = view.findViewById(R.id.rg_completion_status);
        rgCrossovers = view.findViewById(R.id.rg_crossovers);
        cbSingleChapter = view.findViewById(R.id.cb_single_chapter);
        spinnerLanguage = view.findViewById(R.id.spinner_language);
        spinnerRating = view.findViewById(R.id.spinner_rating);
        spinnerSortColumn = view.findViewById(R.id.spinner_sort_column);
        spinnerSortDirection = view.findViewById(R.id.spinner_sort_direction);
        btnSearch = view.findViewById(R.id.btn_search);


        // Fetch and populate parameters in the background
        new Thread(() -> {
            // Update the cached parameters
            SearchAPI.updateAvailableParameters();

            // Update the UI on the main thread
            requireActivity().runOnUiThread(this::populateForm);
        }).start();

        // Set up the search button
        setupSearchButton();

        TextView tvWorkInfo = view.findViewById(R.id.tv_work_info);
        LinearLayout llWorkInfo = view.findViewById(R.id.ll_work_info);

        TextView tvWorkTags = view.findViewById(R.id.tv_work_tags);
        LinearLayout llWorkTags = view.findViewById(R.id.ll_work_tags);

        TextView tvWorkStats = view.findViewById(R.id.tv_work_stats);
        LinearLayout llWorkStats = view.findViewById(R.id.ll_work_stats);

        TextView tvSearchParameters = view.findViewById(R.id.tv_search_parameters);
        LinearLayout llSearchParameters = view.findViewById(R.id.ll_search_parameters);

        // Set up toggle listeners
        tvWorkInfo.setOnClickListener(v -> toggleVisibility(llWorkInfo, tvWorkInfo));
        tvWorkTags.setOnClickListener(v -> toggleVisibility(llWorkTags, tvWorkTags));
        tvWorkStats.setOnClickListener(v -> toggleVisibility(llWorkStats, tvWorkStats));
        tvSearchParameters.setOnClickListener(v -> toggleVisibility(llSearchParameters, tvSearchParameters));

        if (params != null) {
            EditText etAdditionalTags = view.findViewById(R.id.et_additional_tags);
            if (params.containsKey("work_search[freeform_names]")) {
                etAdditionalTags.setText(params.get("work_search[freeform_names]"));
            }

            EditText etFandoms = view.findViewById(R.id.et_fandoms);
            if (params.containsKey("work_search[fandom_names]")) {
                etFandoms.setText(params.get("work_search[fandom_names]"));
            }
        }


        return view;
    }

    /**
     * Populates the UI with the cached parameters.
     */
    private void populateForm() {
        Map<String, List<SearchAPI.Pair<String, String>>> parameters = SearchAPI.getAvailableParameters();

        // Add "None" as the default option to each spinner
        List<SearchAPI.Pair<String, String>> noneOption = List.of(new SearchAPI.Pair<>("", "None"));

        MainActivity.bar.setTitle("Advanced search");
        MainActivity.navigationBar.getMenu().findItem(R.id.navigation_browse).setChecked(true);

        // Populate Language Spinner
        if (parameters.containsKey("languages")) {
            List<SearchAPI.Pair<String, String>> languages = new ArrayList<>(noneOption);
            languages.addAll(parameters.get("languages"));
            ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    getDisplayValues(languages)
            );
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLanguage.setAdapter(languageAdapter);
        }

        // Populate Rating Spinner
        if (parameters.containsKey("ratings")) {
            List<SearchAPI.Pair<String, String>> ratings = new ArrayList<>(noneOption);
            ratings.addAll(parameters.get("ratings"));
            ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    getDisplayValues(ratings)
            );
            ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRating.setAdapter(ratingAdapter);
        }

        // Populate Sort Column Spinner
        if (parameters.containsKey("sortColumns")) {
            List<SearchAPI.Pair<String, String>> sortColumns = new ArrayList<>(noneOption);
            sortColumns.addAll(parameters.get("sortColumns"));
            ArrayAdapter<String> sortColumnAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    getDisplayValues(sortColumns)
            );
            sortColumnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSortColumn.setAdapter(sortColumnAdapter);
        }

        // Populate Sort Direction Spinner
        if (parameters.containsKey("sortDirections")) {
            List<SearchAPI.Pair<String, String>> sortDirections = new ArrayList<>(noneOption);
            sortDirections.addAll(parameters.get("sortDirections"));
            ArrayAdapter<String> sortDirectionAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    getDisplayValues(sortDirections)
            );
            sortDirectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSortDirection.setAdapter(sortDirectionAdapter);
        }
    }

    /**
     * Extracts display values from a list of key-value pairs.
     */
    private List<String> getDisplayValues(List<SearchAPI.Pair<String, String>> pairs) {
        return pairs.stream()
                .map(pair -> pair.second)
                .collect(Collectors.toList());
    }

    /**
     * Sets up the search button to generate the search URL.
     */
    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> {
            Map<String, String> searchParams = new HashMap<>();

            // Add the commit parameter
            searchParams.put("commit", "Search");

            // Add basic fields
            addIfNotEmpty(searchParams, "work_search[query]", etAnyField.getText().toString());
            addIfNotEmpty(searchParams, "work_search[title]", etTitle.getText().toString());
            addIfNotEmpty(searchParams, "work_search[creators]", etAuthor.getText().toString());
            addIfNotEmpty(searchParams, "work_search[revised_at]", etDate.getText().toString());
            addIfNotEmpty(searchParams, "work_search[single_chapter]", cbSingleChapter.isChecked() ? "1" : "0");
            addIfNotEmpty(searchParams, "work_search[word_count]", etWordCount.getText().toString());

            // Add completion status
            int completionStatusId = rgCompletionStatus.getCheckedRadioButtonId();
            if (completionStatusId != -1) {
                RadioButton selectedCompletionStatus = getView().findViewById(completionStatusId);
                String completionStatusValue = selectedCompletionStatus.getText().toString().equals("Complete works only") ? "T" : "F";
                if (!selectedCompletionStatus.getText().toString().equals("All works")) {
                    searchParams.put("work_search[complete]", completionStatusValue);
                }
            }

            // Add crossover status
            int crossoverStatusId = rgCrossovers.getCheckedRadioButtonId();
            if (crossoverStatusId != -1) {
                RadioButton selectedCrossoverStatus = getView().findViewById(crossoverStatusId);
                String crossoverStatusValue = selectedCrossoverStatus.getText().toString().equals("Exclude Crossovers") ? "T" : "F";
                if (!selectedCrossoverStatus.getText().toString().equals("Include crossovers")) {
                    searchParams.put("work_search[crossover]", crossoverStatusValue);
                }
            }

            // Add language if not "None"
            if (spinnerLanguage.getSelectedItem() != null && !spinnerLanguage.getSelectedItem().toString().equals("None")) {
                String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                searchParams.put("work_search[language_id]", SearchAPI.getLanguageId(selectedLanguage));
            }

            // Add rating if not "None"
            if (spinnerRating.getSelectedItem() != null && !spinnerRating.getSelectedItem().toString().equals("None")) {
                String selectedRating = spinnerRating.getSelectedItem().toString();
                searchParams.put("work_search[rating_ids]", getRatingValue(selectedRating));
            }

            // Add sort column if not "None"
            if (spinnerSortColumn.getSelectedItem() != null && !spinnerSortColumn.getSelectedItem().toString().equals("None")) {
                searchParams.put("work_search[sort_column]", getSortColumnValue(
                        spinnerSortColumn.getSelectedItem().toString()
                ));
            }

            // Add sort direction if not "None"
            if (spinnerSortDirection.getSelectedItem() != null && !spinnerSortDirection.getSelectedItem().toString().equals("None")) {
                searchParams.put("work_search[sort_direction]", getSortDirectionValue(
                        spinnerSortDirection.getSelectedItem().toString()
                ));
            }

            // Add Fandoms
            EditText etFandoms = getView().findViewById(R.id.et_fandoms);
            addIfNotEmpty(searchParams, "work_search[fandom_names]", etFandoms.getText().toString());

            // Add Warnings
            List<String> warnings = new ArrayList<>();
            if (((CheckBox) getView().findViewById(R.id.cb_warning_14)).isChecked()) warnings.add("14");
            if (((CheckBox) getView().findViewById(R.id.cb_warning_17)).isChecked()) warnings.add("17");
            if (((CheckBox) getView().findViewById(R.id.cb_warning_18)).isChecked()) warnings.add("18");
            if (((CheckBox) getView().findViewById(R.id.cb_warning_16)).isChecked()) warnings.add("16");
            if (((CheckBox) getView().findViewById(R.id.cb_warning_19)).isChecked()) warnings.add("19");
            if (((CheckBox) getView().findViewById(R.id.cb_warning_20)).isChecked()) warnings.add("20");
            if (!warnings.isEmpty()) {
                searchParams.put("work_search[archive_warning_ids][]", String.join(",", warnings));
            }

            // Add Categories
            List<String> categories = new ArrayList<>();
            if (((CheckBox) getView().findViewById(R.id.cb_category_116)).isChecked()) categories.add("116");
            if (((CheckBox) getView().findViewById(R.id.cb_category_22)).isChecked()) categories.add("22");
            if (((CheckBox) getView().findViewById(R.id.cb_category_21)).isChecked()) categories.add("21");
            if (((CheckBox) getView().findViewById(R.id.cb_category_23)).isChecked()) categories.add("23");
            if (((CheckBox) getView().findViewById(R.id.cb_category_2246)).isChecked()) categories.add("2246");
            if (((CheckBox) getView().findViewById(R.id.cb_category_24)).isChecked()) categories.add("24");
            if (!categories.isEmpty()) {
                searchParams.put("work_search[category_ids][]", String.join(",", categories));
            }

            // Add Characters
            EditText etCharacters = getView().findViewById(R.id.et_characters);
            addIfNotEmpty(searchParams, "work_search[character_names]", etCharacters.getText().toString());

            // Add Relationships
            EditText etRelationships = getView().findViewById(R.id.et_relationships);
            addIfNotEmpty(searchParams, "work_search[relationship_names]", etRelationships.getText().toString());

            // Add Additional Tags
            EditText etAdditionalTags = getView().findViewById(R.id.et_additional_tags);
            addIfNotEmpty(searchParams, "work_search[freeform_names]", etAdditionalTags.getText().toString());

            // Add Work Stats
            EditText etHits = getView().findViewById(R.id.et_hits);
            addIfNotEmpty(searchParams, "work_search[hits]", etHits.getText().toString());

            EditText etKudos = getView().findViewById(R.id.et_kudos);
            addIfNotEmpty(searchParams, "work_search[kudos_count]", etKudos.getText().toString());

            EditText etComments = getView().findViewById(R.id.et_comments);
            addIfNotEmpty(searchParams, "work_search[comments_count]", etComments.getText().toString());

            EditText etBookmarks = getView().findViewById(R.id.et_bookmarks);
            addIfNotEmpty(searchParams, "work_search[bookmarks_count]", etBookmarks.getText().toString());

            // Generate the search URL
            String searchUrl = SearchAPI.generateSearchUrl(searchParams);
            System.out.println(searchUrl);
            SearchResultFragment.showResults(getParentFragmentManager(), searchUrl, true);
        });
    }

    /**
     * Adds a key-value pair to the map only if the value is not empty.
     */
    private void addIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }

    /**
     * Gets the rating value from the selected display name.
     */
    private String getRatingValue(String displayName) {
        return SearchAPI.getAvailableParameters().get("ratings").stream()
                .filter(pair -> pair.second.equals(displayName))
                .findFirst()
                .map(pair -> pair.first)
                .orElse("");
    }

    /**
     * Gets the sort column value from the selected display name.
     */
    private String getSortColumnValue(String displayName) {
        return SearchAPI.getAvailableParameters().get("sortColumns").stream()
                .filter(pair -> pair.second.equals(displayName))
                .findFirst()
                .map(pair -> pair.first)
                .orElse("");
    }

    /**
     * Gets the sort direction value from the selected display name.
     */
    private String getSortDirectionValue(String displayName) {
        return SearchAPI.getAvailableParameters().get("sortDirections").stream()
                .filter(pair -> pair.second.equals(displayName))
                .findFirst()
                .map(pair -> pair.first)
                .orElse("");
    }

    private void toggleVisibility(LinearLayout layout, TextView textView) {
        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
            textView.setText(textView.getText().toString().replace("▼", "▲"));
        } else {
            layout.setVisibility(View.GONE);
            textView.setText(textView.getText().toString().replace("▲", "▼"));
        }
    }
}