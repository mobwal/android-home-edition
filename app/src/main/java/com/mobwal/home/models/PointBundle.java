package com.mobwal.home.models;

import java.io.Serializable;

public class PointBundle implements Serializable {

    public PointBundle(String f_route) {
        this.f_route = f_route;
    }

    public PointBundle(String f_route, String f_point) {
        this(f_route);
        this.f_point = f_point;
    }

    public PointBundle(String f_route, String f_point, String f_result) {
        this(f_route, f_point);
        this.f_result = f_result;
    }

    public String f_route;
    public String f_point;
    public String f_result;
}
