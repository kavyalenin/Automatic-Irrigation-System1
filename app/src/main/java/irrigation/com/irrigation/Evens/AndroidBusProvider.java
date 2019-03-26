package irrigation.com.irrigation.Evens;

public class AndroidBusProvider {
    private static final AndroidBus BUS = new AndroidBus();

    public static AndroidBus getInstance() {
        return BUS;
    }

    private AndroidBusProvider() {
    }
}