package is.vinnsla;


public enum WatchStatus {
    VIL_HORFA("Vil horfa"),
    KANNSKI_SEINNA("Kannski seinna"),
    BUINN_AD_HORFA("Búinn að horfa");

    private final String displayName;

    WatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}