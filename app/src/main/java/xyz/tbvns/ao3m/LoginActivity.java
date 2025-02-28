package xyz.tbvns.ao3m;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.LoginAPI;

public class LoginActivity extends AppCompatActivity {

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
                        System.out.println(LoginAPI.login(usrname.getText().toString(), pswd.getText().toString()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
        });
    }
}