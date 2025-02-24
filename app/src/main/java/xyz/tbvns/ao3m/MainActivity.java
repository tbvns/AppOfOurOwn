package xyz.tbvns.ao3m;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationBarView;

import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.FandomAPI;

public class MainActivity extends AppCompatActivity {

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the Material 3 NavigationBar
        NavigationBarView navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        // Load default fragment (e.g., LibraryFragment) if not restoring from a previous state
        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new LibraryFragment())
//                    .commit();
        }

        // Example thread calling FandomAPI (adjust as needed)

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        new Thread(() -> {
            try {
                System.out.println(FandomAPI.getCategories());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
