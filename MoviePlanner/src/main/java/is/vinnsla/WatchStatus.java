package is.vinnsla;

/**
 * Teljutegund sem lýsir stöðu myndar á persónulega listanum.
 */
public enum WatchStatus {

    /** Mynd sem notandi vill horfa á. */
    VIL_HORFA("Vil horfa"),

    /** Mynd sem notandi gæti horft á síðar. */
    KANNSKI_SEINNA("Kannski seinna"),

    /** Mynd sem notandi er búinn að horfa á. */
    BUINN_AD_HORFA("Búinn að horfa");

    /** Birtingarheiti stöðunnar í viðmóti. */
    private final String displayName;

    /**
     * Smíðar nýja stöðu.
     *
     * @param displayName heiti sem birtist í viðmóti
     */
    WatchStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Skilar birtingarheiti stöðunnar.
     *
     * @return birtingarheiti
     */
    public String getDisplayName() {
        return displayName;
    }
}