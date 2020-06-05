package lab03.lab04.entities;

public class Product {

    private final Integer id;
    private final String title;
    private final double price;
    private final int quantity;



    public Product(final Integer id, final String title, final double price, final int quantity) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public Product(final String title, final double id, final int quantity) {
        this(null, title, id, quantity);
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

    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }

}
