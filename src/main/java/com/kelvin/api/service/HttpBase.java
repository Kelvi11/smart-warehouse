package com.kelvin.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class HttpBase {

    @Autowired
    WebRequest ui;

    public String get(String key) {
        return ui.getParameter(key);
    }

    public String lowercase(String key) {
        return get(key) != null ? get(key).toLowerCase() : null;
    }

    public Integer _integer(String key) {
        String value = ui.getParameter(key);
        return Integer.valueOf(value);
    }

    public Long _long(String key) {
        String value = ui.getParameter(key);
        return Long.valueOf(value);
    }

    public Boolean _boolean(String key) {
        String value = ui.getParameter(key);
        return Boolean.valueOf(value);
    }

    protected final String likeParamToLowerCase(String value) {
        return "%" + get(value).toLowerCase() + "%";
    }


    protected boolean nn(String key) {
        return ui.getParameterMap().containsKey(key)
                && ui.getParameter(key) != null
                && !ui.getParameter(key).trim().isEmpty();
    }

    protected String likeParam(String param) {
        return "%" + get(param) + "%";
    }

    protected String likeParamL(String param) {
        return "%" + get(param);
    }

    protected String likeParamR(String param) {
        return get(param) + "%";
    }

    public List<String> asList(String key) {
        String value = get(key);
        return Stream.of(value.split(",", -1))
                .collect(Collectors.toList());
    }

    public List<String> fromValueToList(String value) {
        return Stream.of(value.split(",", -1))
                .collect(Collectors.toList());
    }
}
