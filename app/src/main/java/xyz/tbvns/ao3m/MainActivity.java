package xyz.tbvns.ao3m;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationBarView;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.WebBrowser;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.Fragments.BrowseFragment;
import xyz.tbvns.ao3m.Fragments.FandomMainFragment;
import xyz.tbvns.ao3m.Fragments.LibrairyFragment;
import xyz.tbvns.ao3m.Fragments.LoadingFragment;
import xyz.tbvns.ao3m.Views.WorkView;

import java.time.LocalDate;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static ActionBar bar;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(WebBrowser::preload).start();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bar = getSupportActionBar();

        // Apply window insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Set up the Material 3 NavigationBar
        NavigationBarView navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getTitle().toString()) {
                    case "Library":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LibrairyFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit();
                        break;
                    case "Browse":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new BrowseFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit();
                        break;
                    default:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LoadingFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit();
                        break;
                }

                return true;
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

        //This repair the nav bar
        EdgeToEdge.enable(this);
    }
}
