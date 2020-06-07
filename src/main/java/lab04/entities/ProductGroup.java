package lab04.entities;

public class ProductGroup {

    private final Integer id;
    private String title;
    private String description;

    ProductGroup(final Integer id, final String title, final String description){
        this.id = id;
        this.title = title;
        this.description = description;
    }



    @Override
    public String toString() {
        return "{\"id\": " + id +
                ", \"title\": \"" + title + "\"" +
                ", \"description\": \"" + description + "\"}";
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
