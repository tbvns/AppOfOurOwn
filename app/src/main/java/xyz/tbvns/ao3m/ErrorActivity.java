package xyz.tbvns.ao3m;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Views.ErrorView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ErrorActivity extends AppCompatActivity {

    public static void show(String message, Context context) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra("message", message);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_error);

        String message = getIntent().getStringExtra("message");

        ErrorView view = new ErrorView(getApplicationContext(), message, true);
        LinearLayout layout = findViewById(R.id.main);
        layout.addView(view);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}