package xyz.tbvns.ao3m;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.datepicker.*;

import java.util.Objects;

public class DateSearchGUI {

    public interface DateSelectionCallback {
        void onDateSelected(long timestamp);
    }

    public interface DateRangeSelectionCallback {
        void onDateRangeSelected(long[] timestamps);
    }

    // Create constraints to disable future dates
    private static CalendarConstraints getPastDateConstraints() {
        return new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()) // Only allow past dates
                .build();
    }

    // Show a single date picker dialog (only past dates)
    public static void showDatePicker(@NonNull FragmentActivity activity, DateSelectionCallback callback) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Default to today
                .setCalendarConstraints(getPastDateConstraints()) // Restrict to past dates
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (callback != null) {
                callback.onDateSelected(selection);
            }
        });

        datePicker.show(activity.getSupportFragmentManager(), "SINGLE_DATE_PICKER");
    }

    // Show a range date picker dialog (only past dates)
    public static void showDateRangePicker(@NonNull FragmentActivity activity, DateRangeSelectionCallback callback) {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setCalendarConstraints(getPastDateConstraints()) // Restrict to past dates
                .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && callback != null) {
                long startDate = Objects.requireNonNull(selection.first);
                long endDate = Objects.requireNonNull(selection.second);
                callback.onDateRangeSelected(new long[]{startDate, endDate});
            }
        });

        dateRangePicker.show(activity.getSupportFragmentManager(), "DATE_RANGE_PICKER");
    }
}
