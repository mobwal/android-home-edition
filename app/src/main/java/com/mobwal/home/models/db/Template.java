package com.mobwal.home.models.db;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import com.mobwal.home.CustomLayoutManager;
import com.mobwal.home.R;

public class Template {
    public Template() {
        id = UUID.randomUUID().toString();
        n_order = 0;
    }

    public String id;

    public String c_name;

    public String c_template;

    public String c_layout;

    public String f_route;

    public int n_order;

    public void setDefault(@NotNull Context context, @NotNull String f_route) {
        c_name = context.getString(R.string.template_default);

        CustomLayoutManager customLayoutManager = new CustomLayoutManager(context);

        c_template = customLayoutManager.getDefaultLayoutName();
        this.f_route = f_route;
        n_order = 1;

        c_layout = customLayoutManager.getDefaultLayout();
    }

    public void setDemo(@NotNull Context context, @NotNull String f_route) {
        c_name = context.getString(R.string.profile);
        c_template = "PROFILE";
        this.f_route = f_route;
        n_order = 1;
        c_layout = "layout 'vbox'\n" +
                "\ttextfield name 'Name'\n" +
                "\tdatefield birthday 'Birth day'\n" +
                "\tswitchfield male 'Male'\n" +
                "\ttextfield notice Notice";
    }
}
