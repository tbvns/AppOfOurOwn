package xyz.tbvns.ao3m;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationBarView;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.LoginAPI;
import xyz.tbvns.ao3m.AO3.WebBrowser;
import xyz.tbvns.ao3m.Fragments.BrowseFragment;
import xyz.tbvns.ao3m.Fragments.HistoryFragment;
import xyz.tbvns.ao3m.Fragments.LibrairyFragment;
import xyz.tbvns.ao3m.Fragments.LoadingFragment;

public class MainActivity extends AppCompatActivity {

    public static ActionBar bar;
    public static NavigationBarView navigationBar;
    public static MainActivity main;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        main = this;
        new Thread(WebBrowser::preload).start();
        LoginAPI.initialize(getApplicationContext());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bar = getSupportActionBar();
        navigationBar = findViewById(R.id.navigation_bar);

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
                                .addToBackStack("main")
                                .commit();
                        break;
                    case "Browse":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new BrowseFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .addToBackStack("main")
                                .commit();
                        break;
                    case "History":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new HistoryFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .addToBackStack("main")
                                .commit();
                        break;
                    default:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LoadingFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .addToBackStack("main")
                                .commit();
                        break;
                }

                return true;
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LibrairyFragment())
                    .disallowAddToBackStack()
                    .commit();
        }

//        EdgeToEdge.enable(this);
    }
}
