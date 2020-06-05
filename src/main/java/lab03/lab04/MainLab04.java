package lab03.lab04;

import lab03.lab04.entities.Product;
import java.util.Arrays;
import java.util.HashSet;

public class MainLab04 {
    public static void main(String[] args) {
        DaoProduct dp = new DaoProduct("file.db");

        for (int i = 1; i < 21; i++) {
            dp.insertProduct(new Product("product" + i, Math.random() * 1000, i*i));
        }


        dp.deleteByTitle("product3");

        dp.getProductList(0, 20, new Criteria()).forEach(System.out::println);

        System.out.println("==========");

        final Criteria filter = new Criteria();
        //filter.setIds(new HashSet<>(Arrays.asList(1,2,3,4,10)));
//        filter.setQuery("product1");
//        filter.setFromPrice(600.0);
        //filter.setToPrice(900.0);
        //filter.setFromQuantity(10);
        filter.setToQuantity(26);
        dp.getProductList(0, 20, filter).forEach(System.out::println);


        System.out.println("\n==========");
        dp.update("title", "_MEGA_PRODUCT_", "title","product20");
        dp.update("price", "1000000000", "title","_MEGA_PRODUCT_");
        System.out.println("after update\n");

        dp.getProductList(0, 20, new Criteria()).forEach(System.out::println);

        dp.deleteAll();

    }
}
