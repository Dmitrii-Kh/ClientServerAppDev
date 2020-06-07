package TestLab04;

import lab04.ProductFilter;
import lab04.database.Database;
import lab04.entities.Category;
import lab04.entities.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DBTest {
    static Database db;

    @BeforeAll
    static void init() {
        db = Database.getInstance();
        db.deleteAllCategories();
        db.deleteAllProducts();
    }

    @Test
    void shouldPass_whenProductCategoryInsertedSuccessfully() {
        Category category = new Category("categoryTitle", "categoryDescription");
        db.insertCategory(category);
        System.out.println(db.getCategory(category.getTitle()));
        assertEquals(("{\"title\":\"categoryTitle\",\"description\":\"categoryDescription\"}"), db.getCategory(category.getTitle()).toString());
    }

    @Test
    void shouldPass_whenProductCategoryUpdatedSuccessfully() {
        db.updateCategory("description", "new description here", "title", "categoryTitle");
        System.out.println(db.getCategory("categoryTitle"));
        assertEquals(("{\"title\":\"categoryTitle\",\"description\":\"new description here\"}"), db.getCategory("categoryTitle").toString());
    }

//    @Test
//    void shouldPass_whenProductCategoryDeletedSuccessfully() {
//        db.deleteCategory("categoryTitle");
//        assertEquals("[]", db.getCategoryList(0, 10).toString());
//    }

    @Test
    void shouldPass_whenProductInsertedSuccessfully() {
        db.insertCategory(new Category("Food", "food goes here"));
        db.insertProduct(new Product("apple", "fruit", "natural", 3, 10, "Food"));
        assertEquals("apple", db.getProduct("apple").getTitle());
    }


    @Test
    void shouldPass_whenProductUpdatedSuccessfully() {
        db.insertProduct(new Product("orange", "fruit", "natural", 3.99, 10, "Food"));
        db.updateProduct("price", "5.99", "title", "orange");
        assertEquals(5.99, db.getProduct("orange").getPrice());
    }

    @Test
    void shouldPass_whenCorrectlyListedByCriteria() {
        db.insertCategory(new Category("Non_food", "not food goes here"));
        for (int i = 1; i < 8; i++) {
            db.insertProduct(new Product("product" + i, "test product", "producer", i * 0.5 + i, i * i, "Non_food"));
        }
        db.getProductList(0, 10, new ProductFilter()).forEach(System.out::println);

        ProductFilter filter = new ProductFilter();
        filter.setFromPrice(4.0);
        filter.setToPrice(10.0);
        filter.setFromQuantity(10);
        filter.setToQuantity(36);
        filter.setIds(new HashSet<>(Arrays.asList(5,6,7)));

        int expected[] = {5,6};

        List<Product> productList = db.getProductList(0,10, filter);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], productList.get(i).getId());
        }
    }


}
