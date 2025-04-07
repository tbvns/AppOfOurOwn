package xyz.tbvns.ao3m;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import lombok.NonNull;
import xyz.tbvns.ao3m.AO3.APIResponse;
import xyz.tbvns.ao3m.AO3.ChaptersAPI;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Storage.Data.LibraryData;
import xyz.tbvns.ao3m.Storage.Data.UpdatesHistoryData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class UpdateCheckWorker extends Worker {
    private static final String CHANNEL_ID = "update_channel";
    private static final int NOTIFICATION_ID = 1;

    public UpdateCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        createNotificationChannel(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        showProgressNotification(context, "Starting update check...", 0);

        WorkAPI.Work[] works = ConfigManager.getLibraryConf(getApplicationContext()).getWorks();
        List<String> errors = new ArrayList<>();
        List<WorkAPI.Work> updatedWorks = new ArrayList<>();
        UpdatesHistoryData data = ConfigManager.getUpdateHistoryData(getApplicationContext());

        for (int i = 0; i < works.length; i++) {
            WorkAPI.Work work = works[i];
            float progress = (i + 1) / (float) works.length;

            showProgressNotification(
                    context,
                    "Checking: " + work.title,
                    (int) (progress * 100)
            );

            // Your existing work checking logic
            if (work.chapterMax == work.chapterCount) {
                updatedWorks.add(work);
                continue;
            }

            APIResponse<WorkAPI.Work> response = WorkAPI.fetchWork(work.workId);
            if (response.isSuccess()) {
                WorkAPI.Work newWork = response.getObject();
                if (newWork.chapterCount != work.chapterCount) {
                    showUpdateNotification(context, newWork, i);
                    APIResponse<List<ChaptersAPI.Chapter>> chapter = ChaptersAPI.fetchChapters(newWork, getApplicationContext());
                    if (chapter.isSuccess()) {
                        data.addEntry(
                                new UpdatesHistoryData.Entry(
                                        newWork,
                                        Instant.now().getEpochSecond(),
                                        chapter.getObject().get(chapter.getObject().size() - 1).getTitle(),
                                        chapter.getObject().size()
                                )
                        );
                    } else {
                        data.addEntry(new UpdatesHistoryData.Entry(newWork, Instant.now().getEpochSecond(), "Error while fetching the chapter", -1));
                    }
                }

                updatedWorks.add(newWork);
            } else {
                updatedWorks.add(work);
                errors.add(work.title + ": " + response.getMessage());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return Result.failure();
            }
        }

        ConfigManager.saveUpdateHistoryData(data, getApplicationContext());
        ConfigManager.saveLibraryConf(new LibraryData(updatedWorks.toArray(new WorkAPI.Work[0])), getApplicationContext());
        showCompletionNotification(context, errors);
        return Result.success();
    }

    private void showProgressNotification(Context context, String text, int progress) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Update Check")
                .setContentText(text)
                .setProgress(100, progress, false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true);

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showUpdateNotification(Context context, WorkAPI.Work work, int i) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Update for " + work.title)
                .setContentText("Chapter " + work.chapterCount + " is out !")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent);

        manager.notify(i + 2, builder.build());
    }


    private void showCompletionNotification(Context context, List<String> errors) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (!errors.isEmpty()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(errors.size() + " Works have not been updated !")
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            StringBuilder stringBuilder = new StringBuilder("The following works where not updated:\n");

            for (String error : errors) {
                stringBuilder.append(error).append("\n");
            }

            builder.setContentText(stringBuilder.toString());
            manager.notify(NOTIFICATION_ID, builder.build());
        } else {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    private void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Update Checker",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Background update checks");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}