package xyz.tbvns.ao3m.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import xyz.tbvns.ao3m.R;
import xyz.tbvns.ao3m.Storage.Data.ConfigManager;
import xyz.tbvns.ao3m.Storage.Data.CustomizationData;

public class CustomizationActivity extends AppCompatActivity {
    private static final int MIN_TEXT_SIZE = 8;
    private static final int MAX_TEXT_SIZE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customization);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Switch switchTextSize = findViewById(R.id.switch1);
        Switch switchCustomColor = findViewById(R.id.switch2);
        SeekBar seekBar = findViewById(R.id.textSizeSeekBar);
        EditText editText = findViewById(R.id.textSizeInput);
        View textColorView = findViewById(R.id.TextColor);
        View backgroundColorView = findViewById(R.id.BackgroundColor);
        View textColorViewShow = findViewById(R.id.view2);
        View backgroundColorViewShow = findViewById(R.id.view);
        TextView sampleText = findViewById(R.id.sampleText);

        switchTextSize.setChecked(CustomizationData.useTextCustomSize);
        switchCustomColor.setChecked(CustomizationData.useCustomColor);

        seekBar.setMax(MAX_TEXT_SIZE - MIN_TEXT_SIZE);
        seekBar.setProgress(CustomizationData.customTextSize - MIN_TEXT_SIZE);
        editText.setText(String.valueOf(CustomizationData.customTextSize));

        updateColorView(textColorViewShow, CustomizationData.textColor);
        updateColorView(backgroundColorViewShow, CustomizationData.backgroundColor);

        if (CustomizationData.useCustomColor) {
            sampleText.setTextColor(CustomizationData.textColor);
            sampleText.setBackgroundTintList(ColorStateList.valueOf(CustomizationData.backgroundColor));
        }
        if (CustomizationData.useTextCustomSize) {
            sampleText.setTextSize(CustomizationData.customTextSize);
        }

        switchTextSize.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    CustomizationData.useTextCustomSize = isChecked;
                    ConfigManager.save();
                    if (isChecked) {
                        sampleText.setTextSize(CustomizationData.customTextSize);
                    } else {
                        sampleText.setTextSize(16);
                    }
                });

        switchCustomColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CustomizationData.useCustomColor = isChecked;
            ConfigManager.save();
            if (isChecked) {
                sampleText.setTextColor(CustomizationData.textColor);
                sampleText.setBackgroundTintList(ColorStateList.valueOf(CustomizationData.backgroundColor));
            } else {
                sampleText.setTextColor(getResources().getColor(R.color.midnightdusk_onSurface));
                sampleText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.midnightdusk_surfaceContainer)));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int size = progress + MIN_TEXT_SIZE;
                    editText.setText(String.valueOf(size));
                    CustomizationData.customTextSize = size;
                    if (CustomizationData.useTextCustomSize) {
                        sampleText.setTextSize(size);
                    }
                    ConfigManager.save();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int size = Integer.parseInt(s.toString());
                    size = Math.max(MIN_TEXT_SIZE, Math.min(size, MAX_TEXT_SIZE));
                    seekBar.setProgress(size - MIN_TEXT_SIZE);
                    CustomizationData.customTextSize = size;
                    if (CustomizationData.useTextCustomSize) {
                        sampleText.setTextSize(size);
                    }
                    ConfigManager.save();
                } catch (NumberFormatException e) {
                    editText.setText(String.valueOf(CustomizationData.customTextSize));
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        textColorView.setOnClickListener(v -> showColorPicker(
                Color.valueOf(CustomizationData.textColor),
                selectedColor -> {
                    CustomizationData.textColor = selectedColor.toArgb();
                    ConfigManager.save();
                    updateColorView(textColorViewShow, selectedColor.toArgb());
                    if (CustomizationData.useCustomColor) {
                        sampleText.setTextColor(selectedColor.toArgb());
                    }
                }));

        backgroundColorView.setOnClickListener(v -> showColorPicker(
                Color.valueOf(CustomizationData.textColor),
                selectedColor -> {
                    CustomizationData.backgroundColor = selectedColor.toArgb();
                    ConfigManager.save();
                    updateColorView(backgroundColorViewShow, selectedColor.toArgb());
                    if (CustomizationData.useCustomColor) {
                        sampleText.setBackgroundTintList(ColorStateList.valueOf(selectedColor.toArgb()));
                    }
                }));
    }

    private void showColorPicker(Color initialColor, OnColorSelectedListener listener) {
        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(this)
                .setTitle("Choose Color")
                .setPreferenceName("CustomColorPicker")
                .setPositiveButton(getString(R.string.confirm),
                        (ColorEnvelopeListener) (envelope, fromUser) ->
                                listener.onColorSelected(convertIntToColor(envelope.getColor())))
                .setNegativeButton(getString(R.string.cancel),
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true);
        builder.show();
    }

    private int convertColorToInt(Color color) {
        return Color.rgb(
                (int) (color.red() * 255),
                (int) (color.green() * 255),
                (int) (color.blue() * 255)
        );
    }

    private Color convertIntToColor(int colorInt) {
        return Color.valueOf(
                Color.red(colorInt) / 255f,
                Color.green(colorInt) / 255f,
                Color.blue(colorInt) / 255f
        );
    }

    private void updateColorView(View view, int color) {
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    interface OnColorSelectedListener {
        void onColorSelected(Color color);
    }

    public static void show(Context context) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, CustomizationActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}