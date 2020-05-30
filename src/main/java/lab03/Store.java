package lab03;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Store {

    private ConcurrentHashMap<String, ArrayList<Product>> store;

    Store(){
        store = new ConcurrentHashMap<>();
    }

    void addProductGroup(String groupTitle){
        store.put(groupTitle, new ArrayList<>());
    }

    void addProductTitleToGroup(String groupTitle, String productTitle){
        store.get(groupTitle).add(new Product(productTitle));
    }

    void addProduct(String productTitle, int amount){
        for(ArrayList<Product> al : store.values()){
            for(Product p : al){
                if(p.getTitle().equals(productTitle)){
                    p.getAmount().addAndGet(amount);
                }
            }
        }
    }

    void getProduct(String productTitle, int amount){
        for(ArrayList<Product> al : store.values()){
            for(Product p : al){
                if(p.getTitle().equals(productTitle)){
                    p.getAmount().addAndGet(-amount);
                }
            }
        }
    }

    void setProductPrice(String productTitle, int price){
        for(ArrayList<Product> al : store.values()){
            for(Product p : al){
                if(p.getTitle().equals(productTitle)){
                    p.getPrice().set(price);
                }
            }
        }
    }

    AtomicInteger getProductPrice(String productTitle){
        for(ArrayList<Product> al : store.values()){
            for(Product p : al){
                if(p.getTitle().equals(productTitle)){
                    return p.getPrice();
                }
            }
        }
        return new AtomicInteger(-1);
    }

    AtomicInteger getProductAmount(String productTitle){
        for(ArrayList<Product> al : store.values()){
            for(Product p : al){
                if(p.getTitle().equals(productTitle)){
                    return p.getAmount();
                }
            }
        }
        return new AtomicInteger(-1);
    }

    public static void main(String[] args) {
        Store store = new Store();
        store.addProductGroup("Meat");
        store.addProductTitleToGroup("Meat", "Beef");
        store.addProduct("Beef", 3);
        store.getProduct("Beef", 1);
        store.setProductPrice("Beef", 123);

        System.out.println("Price for Beef : " + store.getProductPrice("Beef"));
        System.out.println("Amount of Beef : " + store.getProductAmount("Beef"));
    }

}
