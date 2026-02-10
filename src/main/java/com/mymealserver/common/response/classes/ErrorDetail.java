package com.mymealserver.common.response.classes;

public record ErrorDetail (
    String code,
    String message
){ }