package lab04;

import lab04.database.DaoProduct;
import lab04.database.Database;
import lab04.entities.Category;
import lab04.entities.Product;

public class MainLab04 {
    public static void main(String[] args) {
        Database db = Database.getInstance();
        db.deleteAllCategories();
        db.deleteAllProducts();

        Category category = new Category("categoryTitle", "categoryDescription");

        db.insertCategory(category);
        System.out.println(db.getCategory(category.getTitle()));
        System.out.println(db.getCategoryList(0, 10));

        db.updateCategory("description", "ffffffff", "title", category.getTitle());
        System.out.println(db.getCategoryList(0, 10));

        db.deleteCategory(category.getTitle());
        System.out.println(db.getCategoryList(0, 10));

        System.out.println();


        db.insertCategory(category);
        Product product = new Product("productTitle", "productDescription", "productProducer",
                5, 10, category.getTitle());
        db.insertProduct(product);
        System.out.println(db.getProduct(product.getTitle()));
        System.out.println(db.getProductList(0, 10, new ProductFilter()));

        db.updateProduct("price", "10000", "title", product.getTitle());
        System.out.println(db.getProduct(product.getTitle()));

        db.deleteProduct(product.getTitle());
        System.out.println(db.getProduct(product.getTitle()));

        db.deleteAllCategories();
        db.deleteAllProducts();
    }
}
