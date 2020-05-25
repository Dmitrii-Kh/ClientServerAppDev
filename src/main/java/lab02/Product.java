package lab02;

import java.util.concurrent.atomic.AtomicInteger;

public class Product {

    private String title;
    private AtomicInteger price;
    private AtomicInteger amount;


    Product(String title, int price, int amount){
        this.title = title;
        this.price = new AtomicInteger(price);
        this.amount = new AtomicInteger(amount);
    }

    Product(String title){
        this.title = title;
        this.price = new AtomicInteger(0);
        this.amount = new AtomicInteger(0);
    }


    public String getTitle() {
        return title;
    }

    public AtomicInteger getAmount() {
        return amount;
    }

    public AtomicInteger getPrice() {
        return price;
    }

}
