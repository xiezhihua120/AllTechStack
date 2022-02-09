package com.longtech.glide.demo.example1;

public abstract class BaseRequestOptions<T extends BaseRequestOptions<T>> implements Cloneable  {

    private static final int UNSET = -1;
    private static final int PRIORITY = 1 << 1;
    private static final int OVERRIDE = 1 << 2;
    private static final int DISABLE_ANIMATION = 1 << 3;

    private int priority;
    private int overrideWidth = UNSET;
    private int overrideHeight = UNSET;
    private boolean disableAnimationFlag;

    private int fields = 0;

    public boolean isSet(int fields, int flag) {
        return (fields & flag) != 0;
    }

    public T priority(int priority) {
        if (isAutoCloneEnabled) {
            return clone().priority(priority);
        }
        this.priority = priority;
        fields |= PRIORITY;
        return (T) this;
    }

    public T override(int width, int height) {
        if (isAutoCloneEnabled) {
            return clone().override(width, height);
        }
        this.overrideWidth = width;
        this.overrideHeight = height;
        fields |= OVERRIDE;
        return (T) this;
    }

    public T disableAnimation() {
        if (isAutoCloneEnabled) {
            return clone().disableAnimation();
        }
        this.disableAnimationFlag = true;
        fields |= DISABLE_ANIMATION;
        return (T) this;
    }

    private boolean isAutoCloneEnabled;

    public T autoClone() {
        isAutoCloneEnabled = true;
        return (T) this;
    }

    public T clone() {
        try {
            BaseRequestOptions<?> result = (BaseRequestOptions<?>) super.clone();
            result.isAutoCloneEnabled = false;
            return (T) result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
