package com.emmanuel.utils.customViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emmanuel.utils.R;
import com.emmanuel.utils.adapters.DropMenuAdapter;
import com.emmanuel.utils.interfaces.OnItemSelectedListener;

public class DropdownMenuPopup extends PopupWindow {
    private final Context context;
    private final DropMenuAdapter dropdownAdapter;

    public DropdownMenuPopup(Context context, DropMenuAdapter dropdownAdapter) {
        super(context);
        this.context = context;
        this.dropdownAdapter = dropdownAdapter;
        setupView();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        dropdownAdapter.setOnItemSelectedListener(onItemSelectedListener);
    }

    private void setupView() {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dropdown_menu, null);
        RecyclerView menuRv = view.findViewById(R.id.rvMenu);
        menuRv.setHasFixedSize(true);
        menuRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        //menuRv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        menuRv.setAdapter(dropdownAdapter);

        setContentView(view);
    }

//    public void addMenuItem(Object menuItem){
//        dropdownAdapter.addItem(menuItem);
//    }
}

