package xyz.tbvns.ao3m;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.LoginAPI;
import xyz.tbvns.ao3m.AO3.WebBrowser;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Storage.Data.AccountData;

import static xyz.tbvns.ao3m.AO3.WebBrowser.client;

public class LoginActivity extends AppCompatActivity {

    public static void show(Context context) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        });
    }

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button login = findViewById(R.id.loginButton);
        TextView usrname = findViewById(R.id.username);
        TextView pswd = findViewById(R.id.password);

        LoginAPI.appContext = getApplicationContext();
        login.setOnClickListener(l -> {
                new Thread(() -> {
                    try {
                        ConfigManager.saveAccountData(new AccountData(LoginAPI.login(usrname.getText().toString(), pswd.getText().toString()), "UsernameNotFetchedRN"));
                        WebBrowser.addCookie(client, "_otwarchive_session", ConfigManager.getAccountData().getToken(), "archiveofourown.org", "/");
                        finish();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
        });
    }
}