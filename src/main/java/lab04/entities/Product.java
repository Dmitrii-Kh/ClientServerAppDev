package lab04.entities;

public class Product {

    private final Integer id;
    private       String  title;
    private       String  description;
    private       String  producer;
    private       double  price;
    private       int     quantity;


    public Product(final Integer id, final String title, final String description, final String producer,
                   final double price, final int quantity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.producer = producer;
        this.price = price;
        this.quantity = quantity;
    }

    public Product(final String title, final String description, final String producer, final double id,
                   final int quantity) {
        this(null, title, description, producer, id, quantity);
    }

    @Override
    public String toString() {
        return "{\"id\": " + id +
                ", \"title\": \"" + title +
                "\", \"description\": \"" + description +
                "\", \"producer\": \"" + producer +
                "\", \"price\": " + price +
                ", \"quantity\": " + quantity + '}';
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
}
