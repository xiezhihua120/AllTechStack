package com.longtech.glide.demo.example1;

public class RequestOptions extends BaseRequestOptions<RequestOptions> {

    private static RequestOptions noAnimationOptions;

    public static RequestOptions priorityOf(int priority) {
        return new RequestOptions().priority(priority);
    }

    public static RequestOptions overrideOf(int width, int height) {
        return new RequestOptions().override(width, height);
    }

    public static RequestOptions disableAnimationOf() {
        if (noAnimationOptions == null) {
            noAnimationOptions = new RequestOptions().disableAnimation().autoClone();
        }
        return noAnimationOptions;
    }

    public static void main(String[] args) {
        RequestOptions requestOptions1 = RequestOptions.priorityOf(1);
        RequestOptions requestOptions2 = requestOptions1.override(100, 100);
        RequestOptions requestOptions3 = requestOptions2.disableAnimationOf();
        RequestOptions requestOptions4 = requestOptions3.priority(2);
        RequestOptions requestOptions5 = requestOptions4.overrideOf(150, 150);
    }

}
