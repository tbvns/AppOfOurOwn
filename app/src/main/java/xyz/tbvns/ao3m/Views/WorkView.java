package xyz.tbvns.ao3m.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import xyz.tbvns.ao3m.AO3.WorkAPI;
import xyz.tbvns.ao3m.Fragments.ChaptersListFragment;
import xyz.tbvns.ao3m.Fragments.FandomTagsBottomSheet;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Utils;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkView extends LinearLayout {

    private WorkAPI.Work work;

    public WorkView(Context context, WorkAPI.Work work) {
        super(context);
        setOrientation(VERTICAL);
        this.work = work;
        init();
    }

    // Required constructors for View inflation (not intended to be used without Work)
    public WorkView(Context context) {
        super(context);
        throw new RuntimeException("WorkView must be initialized with a Work object.");
    }

    public WorkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new RuntimeException("WorkView must be initialized with a Work object.");
    }

    public WorkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new RuntimeException("WorkView must be initialized with a Work object.");
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_work, this, true);

        WorkAPI.applyImage(work.classification, findViewById(R.id.rating), 0);
        WorkAPI.applyImage(work.classification, findViewById(R.id.relationship), 1);
        WorkAPI.applyImage(work.classification, findViewById(R.id.warning), 2);
        WorkAPI.applyImage(work.classification, findViewById(R.id.status), 3);

        ((TextView) findViewById(R.id.title)).setText(work.title);
        ((TextView) findViewById(R.id.author)).setText(work.author);

        ((TextView) findViewById(R.id.hits)).setText(Utils.simplifyNumber(work.hits));
        ((TextView) findViewById(R.id.words)).setText(Utils.simplifyNumber(work.wordCount));
        if (work.chapterMax != -1) {
            ((TextView) findViewById(R.id.chapters)).setText(work.chapterCount + "/" + work.chapterMax);
        } else {
            ((TextView) findViewById(R.id.chapters)).setText(work.chapterCount + "/?");
        }
        ((TextView) findViewById(R.id.kudos)).setText(Utils.simplifyNumber(work.kudos));
        ((TextView) findViewById(R.id.bookmark)).setText(Utils.simplifyNumber(work.bookmarks));
        ((TextView) findViewById(R.id.update)).setText(work.publishedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        ((TextView) findViewById(R.id.language)).setText(work.language);

        List<String> fandoms = work.fandoms;
        String fandomText = !fandoms.isEmpty() ? fandoms.get(0) : "";
        ((TextView) findViewById(R.id.fandom)).setText(fandomText);
        
        ((TextView) findViewById(R.id.desc)).setText(work.summary);

        findViewById(R.id.allTF).setOnClickListener(l -> {
            FandomTagsBottomSheet bottomSheet = new FandomTagsBottomSheet(work.fandoms, work.tags);
            bottomSheet.show(MainActivity.main.getSupportFragmentManager(), bottomSheet.getTag());
        });
        findViewById(R.id.fandom).setOnClickListener(l -> {
            FandomTagsBottomSheet bottomSheet = new FandomTagsBottomSheet(work.fandoms, work.tags);
            bottomSheet.show(MainActivity.main.getSupportFragmentManager(), bottomSheet.getTag());
        });

        setOnClickListener(l -> {
            ChaptersListFragment.show(MainActivity.main.getSupportFragmentManager(), work);
        });
    }
}