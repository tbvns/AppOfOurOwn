package xyz.tbvns.ao3m;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import com.google.android.material.navigation.NavigationBarView;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.LoginAPI;
import xyz.tbvns.ao3m.AO3.WebBrowser;
import xyz.tbvns.ao3m.Fragments.*;
import xyz.tbvns.ao3m.Storage.Database.CacheManager;

import java.util.concurrent.TimeUnit;

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
        CacheManager.clearOldCache(getApplicationContext());

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
                    case "More":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MoreFragment())
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

        checkAndRequestNotificationPermission();
        schedulePeriodicUpdateCheck();

        WorkRequest workRequest = new OneTimeWorkRequest(new OneTimeWorkRequest.Builder(
                    UpdateCheckWorker.class
                ).addTag("OneTimeUpdate"));
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    public void checkAndRequestNotificationPermission() {
        ActivityResultLauncher<String> requestPermissionLauncher = this.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        System.out.println("Granted !");
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, "POST_NOTIFICATIONS")) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        }
                    }
                }
        );

        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
    }

    private void schedulePeriodicUpdateCheck() {
        WorkRequest updateWorkRequest = new PeriodicWorkRequest.Builder(
                UpdateCheckWorker.class,
                6,
                TimeUnit.HOURS
        ).addTag("ChapterUpdater").build();
        WorkManager.getInstance(this)
                .getWorkInfosByTagLiveData("ChapterUpdater")
                .observe(this, workInfos -> {
                    if (workInfos == null || workInfos.isEmpty()) {
                        WorkManager.getInstance(this).enqueue(updateWorkRequest);
                    }
                });
    }
}
