package lab04.entities;

public class Product {

    private final Integer id;

    private String title;
    private String description;
    private String producer;
    private double price;
    private int    quantity;
    private String category;


    public Product(final Integer id, final String title, final String description, final String producer,
                   final double price, final int quantity, final String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.producer = producer;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Product(final String title, final String description, final String producer, final double price,
                   final int quantity, final String category) {
        this(null, title, description, producer, price, quantity, category);
    }

    @Override
    public String toString() {
        return "{\"id\":" + id +
                ",\"title\":\"" + title +
                "\",\"description\":\"" + description +
                "\",\"producer\":\"" + producer +
                "\",\"price\":" + price +
                ",\"quantity\":" + quantity +
                ",\"category\":\"" + category + "\"}";
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public String getProducer() {
        return producer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
