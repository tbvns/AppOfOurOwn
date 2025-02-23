package xyz.tbvns.ao3m;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        new Thread(() -> {
//            try {
//                System.out.println(
//                        Jsoup.parse(new URL("https://archiveofourown.org/works"), 50000).body()
//                );
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}