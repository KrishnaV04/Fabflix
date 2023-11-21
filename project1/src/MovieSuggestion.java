public class MovieSuggestion {
    private String id;
    private String title;

    MovieSuggestion(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}

