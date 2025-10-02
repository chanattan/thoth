package thoth.simulator;

public class News {
    private float effect;
    private String title;
    private String description;

    /// Initializes a news item.
    public News(String title, String description, float effect) {
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

    /*
        Static table of news.
    */
    static News defined_news[] = {
         new News("BNP Paribas manque de sérieux", "BNP Paribas a perdu des documents administratifs, +20% de risques.", -20.0f)
    };

    /*
        Generates a pool of n different news.
    */
    public static void generateNews(int n) {
        ArrayList<News> news = new ArrayList<News>();
        //news.add(new News());
    }
}
