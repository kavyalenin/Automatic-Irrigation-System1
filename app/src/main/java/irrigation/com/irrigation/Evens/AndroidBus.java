package irrigation.com.irrigation.Evens;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import java.util.HashSet;
import java.util.Set;

public class AndroidBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Set<Object> registeredObjects = new HashSet<>();

    @Override
    public void post(final Object event) {
        if(event!=null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        post(event);
                    }
                });
            }
        }
    }

    @Override
    public void register(Object object) {
        if (!registeredObjects.contains(object)) {
            registeredObjects.add(object);
            super.register(object);
        }
    }

    @Override
    public void unregister(Object object) {
        if (registeredObjects.contains(object)) {
            registeredObjects.remove(object);
            super.unregister(object);
        }
    }
}