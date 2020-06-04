package lab03.lab04.entities;

public class Product {

    private final Integer id;
    private final String title;
    private final double price;

    public Product(final Integer id, final String title, final double price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }

    public Product(final String title, final double id) {
        this(null, title, id);
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


    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                '}';
    }
}
