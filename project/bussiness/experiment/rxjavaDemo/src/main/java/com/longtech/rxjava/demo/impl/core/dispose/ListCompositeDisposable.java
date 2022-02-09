package com.longtech.rxjava.demo.impl.core.dispose;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public final class ListCompositeDisposable implements Disposable, DisposableContainer {

    List<Disposable> resources;

    volatile boolean disposed;

    public ListCompositeDisposable() {
    }

    public ListCompositeDisposable(Disposable... resources) {
        Objects.requireNonNull(resources, "resources is null");
        this.resources = new LinkedList<>();
        for (Disposable d : resources) {
            Objects.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    public ListCompositeDisposable(Iterable<? extends Disposable> resources) {
        Objects.requireNonNull(resources, "resources is null");
        this.resources = new LinkedList<>();
        for (Disposable d : resources) {
            Objects.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        List<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }
            disposed = true;
            set = resources;
            resources = null;
        }

        try {
            dispose(set);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public boolean add(Disposable d) {
        Objects.requireNonNull(d, "d is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    List<Disposable> set = resources;
                    if (set == null) {
                        set = new LinkedList<>();
                        resources = set;
                    }
                    set.add(d);
                    return true;
                }
            }
        }
        d.dispose();
        return false;
    }

    public boolean addAll(Disposable... ds) {
        Objects.requireNonNull(ds, "ds is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    List<Disposable> set = resources;
                    if (set == null) {
                        set = new LinkedList<>();
                        resources = set;
                    }
                    for (Disposable d : ds) {
                        Objects.requireNonNull(d, "d is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (Disposable d : ds) {
            d.dispose();
        }
        return false;
    }

    @Override
    public boolean remove(Disposable d) {
        if (delete(d)) {
            d.dispose();
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Disposable d) {
        Objects.requireNonNull(d, "Disposable item is null");
        if (disposed) {
            return false;
        }
        synchronized (this) {
            if (disposed) {
                return false;
            }

            List<Disposable> set = resources;
            if (set == null || !set.remove(d)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        if (disposed) {
            return;
        }
        List<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }

            set = resources;
            resources = null;
        }

        try {
            dispose(set);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void dispose(List<Disposable> set) throws Exception {
        if (set == null) {
            return;
        }
        List<Throwable> errors = null;
        for (Disposable o : set) {
            try {
                o.dispose();
            } catch (Throwable ex) {
                //Exceptions.throwIfFatal(ex);
                if (errors == null) {
                    errors = new ArrayList<>();
                }
                errors.add(ex);
            }
        }
        if (errors != null) {
            if (errors.size() == 1) {
                throw new Exception("ExceptionHelper.wrapOrThrow(errors.get(0));");
            }
            throw new Exception("CompositeException(errors);");
        }
    }
}