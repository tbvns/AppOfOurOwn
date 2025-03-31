package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import xyz.tbvns.ao3m.R;

public class ErrorView extends LinearLayout {
    String message;
    boolean isError;
    public ErrorView(Context context, String message, boolean isError) {
        super(context);
        this.message = message;
        this.isError = isError;
        innit();
    }

    public void innit() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_error, this, true);
        if (isError) {
            ImageView view = findViewById(R.id.errorIcon);
            view.setImageDrawable(getResources().getDrawable(R.drawable.error_icon));
        }

        TextView textView = findViewById(R.id.errorText);
        textView.setText(message);
    }
}