package xyz.tbvns.ao3m.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import xyz.tbvns.ao3m.Api.APIResponse;
import xyz.tbvns.ao3m.Api.ChaptersAPI;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.Data.ChapterProgress;
import xyz.tbvns.ao3m.Storage.Data.CustomizationData;
import xyz.tbvns.ao3m.Storage.Database.ChapterProgressManager;
import xyz.tbvns.ao3m.Storage.Database.HistoryManager;
import xyz.tbvns.ao3m.databinding.ActivityReaderBinding;

import java.time.Instant;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 0;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private View mControlsView;
    private View mControlsViewTop;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            mControlsView.setVisibility(View.VISIBLE);
            mControlsViewTop.setVisibility(View.VISIBLE);

            animateContainer(mControlsView, false, false);
            animateContainer(mControlsViewTop, false, true);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private ActivityReaderBinding binding;

    public static APIResponse<String> currentParagraphs;
    public static ChaptersAPI.Chapter currentChapter;

    public static void showFullscreen(Context context, ChaptersAPI.Chapter chapter, boolean showLoading) {
        new Thread(() -> {
            if (showLoading)
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent loadingIntent = new Intent(context, LoadingActivity.class);
                    loadingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    context.startActivity(loadingIntent);
                });

            //TODO: This may cause error (And will cause them). To fix when the error fragment is created
            currentParagraphs = ChaptersAPI.fetchChapterParagraphs(chapter.getUrl());
            currentChapter = chapter;

            if (!currentParagraphs.isSuccess()) {
                Intent intent = new Intent(context, ErrorActivity.class);
                intent.putExtra("message", currentParagraphs.getMessage());
                context.startActivity(intent);
                return;
            }

            try {
                if (!ChapterProgressManager.chapterExists(context, Integer.parseInt(chapter.getWork().workId), chapter.getNumber())) {
                    ChapterProgressManager.updateProgress(context, new ChapterProgress(Integer.parseInt(chapter.getWork().workId), chapter.getNumber(), "touched", 0));
                }
            } catch (Exception e) {
                Intent intent = new Intent(context, ErrorActivity.class);
                intent.putExtra("message", e.getMessage());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }

            HistoryManager.insertWork(
                    context.getApplicationContext(),
                    new HistoryManager.HistoryEntry(
                            Integer.parseInt(chapter.getWork().workId),
                            chapter.getWork().title,
                            Instant.now().getEpochSecond(),
                            chapter.getNumber(),
                            0
                            )
            );

            new Handler(Looper.getMainLooper()).post(() -> {
                Intent readerIntent = new Intent(context, ReaderActivity.class);
                readerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(readerIntent);
            });
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mControlsViewTop = binding.fullscreenContentControlsTop;
        mContentView = binding.textDisplay;

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        setText(currentParagraphs.getObject());
        getSupportActionBar().hide();

        findViewById(R.id.backButton).setOnClickListener(l -> {
            finish();
        });
        ((TextView) findViewById(R.id.titleText)).setText(currentChapter.getWork().title);
        ((TextView) findViewById(R.id.chapterText)).setText(currentChapter.getTitle());

        ScrollView scrollView = findViewById(R.id.fullscreen_content);
        SeekBar seekBar = findViewById(R.id.progressBarChapter);

        scrollView.post(() -> {
            int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
            seekBar.setMax(maxScroll);

            if (ChapterProgressManager.chapterExists(getApplicationContext(), Integer.parseInt(currentChapter.getWork().workId), currentChapter.getNumber())) {
                ChapterProgress progress = ChapterProgressManager.getChapterProgress(getApplicationContext(), Integer.parseInt(currentChapter.getWork().workId), currentChapter.getNumber());
                seekBar.setProgress(Math.round(progress.getProgress() * maxScroll));
                scrollView.scrollTo(0, Math.round(progress.getProgress() * maxScroll));
            }
        });

        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            seekBar.setProgress(scrollY);

            int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
            String state = "touched";
            if (maxScroll == scrollY) {
                state = "finished";
            }
            ChapterProgress progress = new ChapterProgress(Integer.parseInt(currentChapter.getWork().workId), currentChapter.getNumber(), state, (float) scrollY/maxScroll);
            ChapterProgressManager.updateProgress(getApplicationContext(), progress);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    scrollView.scrollTo(0, progress);


                    int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
                    String state = "touched";
                    if (maxScroll == progress) {
                        state = "finished";
                    }
                    ChapterProgress progressObj = new ChapterProgress(Integer.parseInt(currentChapter.getWork().workId), currentChapter.getNumber(), state, (float) progress/maxScroll);
                    ChapterProgressManager.updateProgress(getApplicationContext(), progressObj);

                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.buttonChapterBack).setOnClickListener(l -> {
            //TODO: This may cause error (And will cause them). To fix when the error fragment is created
            List<ChaptersAPI.Chapter> chapters = ChaptersAPI.fetchChapters(currentChapter.getWork()).getObject();
            for (int i = 0; i < chapters.size(); i++) {
                if (currentChapter.getTitle().equals(chapters.get(i).getTitle())) {
                    if (-1 != i-1) {
                        finish();
                        ReaderActivity.showFullscreen(getApplicationContext(), chapters.get(i-1), true);
                    } else {
                        Toast.makeText(getApplicationContext(), "This chapters does not exist.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        findViewById(R.id.buttonChapterForward).setOnClickListener(l -> {
            //TODO: This may cause error (And will cause them). To fix when the error fragment is created
            List<ChaptersAPI.Chapter> chapters = ChaptersAPI.fetchChapters(currentChapter.getWork()).getObject();
            for (int i = 0; i < chapters.size(); i++) {
                if (currentChapter.getTitle().equals(chapters.get(i).getTitle())) {
                    if (chapters.size() > i+1) {
                        finish();
                        ReaderActivity.showFullscreen(getApplicationContext(), chapters.get(i+1), true);
                    } else {
                        Toast.makeText(getApplicationContext(), "This chapters does not exist.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @SuppressLint("AppCompatCustomView")
    public void setText(String texts) {
        Document doc = Jsoup.parse(texts);

        Elements title = doc.select("div.chapter.preface.group h3.title");
        title.remove();

        Elements summary = doc.select("#summary.summary.module");
        String summaryString = summary.html();
        summary.remove();

        Elements startNotes = doc.select("#notes.notes.module");
        String startNotesString = startNotes.html();
        startNotes.remove();

        Elements endNotesElement = doc.select("div.end.notes.module");
        String endNotes = endNotesElement.html();
        endNotesElement.remove();

        LinearLayout layout = findViewById(R.id.textDisplay);

        if (!title.isEmpty()) {
            layout.addView(new TextView(getApplicationContext()){{
                setText(Html.fromHtml(title.html()));
                setPadding(0, 50, 0, 50);
                if (CustomizationData.useTextCustomSize) {
                    setTextSize(CustomizationData.customTextSize + 9);
                } else  {
                    setTextSize(25);
                }
            }});
        }

        if (!summaryString.isEmpty()) {
            TextView textView = new TextView(getApplicationContext()){{
                setText(Html.fromHtml(summaryString, Html.FROM_HTML_OPTION_USE_CSS_COLORS | Html.FROM_HTML_MODE_COMPACT));
                setPadding(50, 50, 50, 50);
                if (CustomizationData.useTextCustomSize) {
                    setTextSize(CustomizationData.customTextSize - 2);
                } else  {
                    setTextSize(14);
                }
            }};

            FrameLayout frameLayout = new FrameLayout(getApplicationContext());
            frameLayout.setPadding(50, 50, 50, 50); // Ensure outer padding
            frameLayout.addView(textView);
            frameLayout.setBackground(getDrawable(R.drawable.rounded_workview));

            layout.addView(frameLayout);

            Space space = new Space(getApplicationContext());
            space.setMinimumHeight(25);
            layout.addView(space);
        }

        if (!startNotesString.isEmpty()) {
            TextView textView = new TextView(getApplicationContext()){{
                setText(Html.fromHtml(startNotesString, Html.FROM_HTML_OPTION_USE_CSS_COLORS | Html.FROM_HTML_MODE_COMPACT));
                setPadding(50, 50, 50, 50);
                if (CustomizationData.useTextCustomSize) {
                    setTextSize(CustomizationData.customTextSize - 2);
                } else  {
                    setTextSize(14);
                }
            }};

            // Wrap in a container to ensure padding is respected
            FrameLayout frameLayout = new FrameLayout(getApplicationContext());
            frameLayout.setPadding(50, 50, 50, 50); // Ensure outer padding
            frameLayout.addView(textView);
            frameLayout.setBackground(getDrawable(R.drawable.rounded_workview));

            layout.addView(frameLayout);

            Space space = new Space(getApplicationContext());
            space.setMinimumHeight(25);
            layout.addView(space);
        }

        layout.addView(new TextView(getApplicationContext()){{
            setText(Html.fromHtml(doc.html(), Html.FROM_HTML_OPTION_USE_CSS_COLORS | Html.FROM_HTML_MODE_LEGACY));
            setPadding(0, 50, 0, 50);
            if (CustomizationData.useTextCustomSize) {
                setTextSize(CustomizationData.customTextSize);
            } else  {
                setTextSize(16);
            }
            if (CustomizationData.useCustomColor) {
                setTextColor(CustomizationData.textColor);
            }
        }});

        if (!endNotes.isEmpty()) {
            TextView textView = new TextView(getApplicationContext()) {{
                setText(Html.fromHtml(endNotes, Html.FROM_HTML_OPTION_USE_CSS_COLORS | Html.FROM_HTML_MODE_COMPACT));
                setPadding(50, 50, 50, 50);
                if (CustomizationData.useTextCustomSize) {
                    setTextSize(CustomizationData.customTextSize - 2);
                } else  {
                    setTextSize(14);
                }
            }};

            // Wrap in a container to ensure padding is respected
            FrameLayout frameLayout = new FrameLayout(getApplicationContext());
            frameLayout.setPadding(50, 50, 50, 50); // Ensure outer padding
            frameLayout.addView(textView);
            frameLayout.setBackground(getDrawable(R.drawable.rounded_workview));

            layout.addView(frameLayout);
        }

        //TODO: This may cause error (And will cause them). To fix when the error fragment is created
        List<ChaptersAPI.Chapter> chapters = ChaptersAPI.fetchChapters(currentChapter.getWork()).getObject();
        for (int i = 0; i < chapters.size(); i++) {
            if (currentChapter.getTitle().equals(chapters.get(i).getTitle())) {
                if (chapters.size() > i+1) {
                    Button button = new Button(getApplicationContext());
                    layout.addView(button);
                    button.setText("Next chapter: " + chapters.get(i+1).getTitle());
                    int finalI = i;
                    button.setOnClickListener(l -> {
                        finish();
                        ReaderActivity.showFullscreen(getApplicationContext(), chapters.get(finalI +1), true);
                    });
                }
            }
        }

        if (CustomizationData.useCustomColor) {
            layout.setBackgroundColor(CustomizationData.backgroundColor);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        animateContainer(mControlsView, true, false);
        animateContainer(mControlsViewTop, true, true);

//        mControlsView.setVisibility(View.GONE);
//        mControlsViewTop.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void animateContainer(View view, boolean isGoingOut, boolean isAtTop) {
        float translationY = isGoingOut ? (isAtTop ? -view.getHeight() : view.getHeight()) : 0;
        int visibility = isGoingOut ? View.GONE : View.VISIBLE;

        view.animate()
                .translationY(translationY)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (animation != null) {
                            animation.removeAllListeners();
                        }
                        view.clearAnimation();
                        view.setVisibility(visibility);
                    }
                });
    }
}