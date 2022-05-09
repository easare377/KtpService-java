package com.emmanuel.utils.customViews;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {
    private final List<TextWatcher> textWatcherList = new ArrayList<>();
    public CustomEditText(@NonNull Context context) {
        super(context);
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        textWatcherList.add(watcher);
    }

    public void removeAllTextChangedListeners() {
        textWatcherList.clear();
    }
}
