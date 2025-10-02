public class News {
    private float effect;
    private String title;
    private String description;

    /// Initializes a news item.
    public News(String title, String description, int effect) {
        this.title = title;
        this.description = description;
        this.effect = effect;
    }

    /// Returns the title of this news.
    public String getTitle() {
        return this.title;
    }

    /// Returns the description of this news (longer text)
    public String getDescription() {
        return this.description;
    }

    /// Returns the measurable effect on stocks.
    public float getEffect() {
        return this.effect;
    }
}
