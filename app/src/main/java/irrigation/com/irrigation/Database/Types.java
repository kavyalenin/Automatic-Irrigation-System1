package irrigation.com.irrigation.Database;

public enum Types {

    CROP("Crop"),
    IMAGEFILES("ImageFiles");

    private final String text;

    /**
     * @param text
     */
    private Types(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}