package lab04.database;

import lab04.ProductFilter;
import lab04.entities.Category;
import lab04.entities.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Database {

    public static final String fileName = "database.db";

    private static volatile Database instance;
    private final Connection connection;
    private final DaoProduct  daoProduct;
    private final DaoCategory daoCategory;


    public static Database getInstance() {
        Database localInstance = instance;
        if (localInstance == null) {
            synchronized (Database.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Database();
                }
            }
        }
        return localInstance;
    }

    public static Database getInstance(final String fileName) {
        Database localInstance = instance;
        if (localInstance == null) {
            synchronized (Database.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Database(fileName);
                }
            }
        }
        return localInstance;
    }

    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC not found!", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        initProductGroupsTable();
        initProductsTable();

        daoProduct = new DaoProduct(connection);
        daoCategory = new DaoCategory(connection);
    }

    private Database(final String fileName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC not found!", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        initProductGroupsTable();
        initProductsTable();

        daoProduct = new DaoProduct(connection);
        daoCategory = new DaoCategory(connection);
    }




    private void initProductGroupsTable(){
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS 'categories' (" +
                            "'title' VARCHAR(200) PRIMARY KEY," +
                            "'description' VARCHAR(700) NOT NULL)"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create categories table!", e);
        }
    }

    private void initProductsTable(){
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS 'products' (" +
                    "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "'title' VARCHAR(200) NOT NULL UNIQUE," +
                    "'description' VARCHAR(700) NOT NULL," +
                    "'producer' VARCHAR(200) NOT NULL," +
                    "'price' DECIMAL(10, 3) NOT NULL," +
                    "'quantity' INTEGER NOT NULL," +
                    "'category' VARCHAR(200) NOT NULL," +
                    "FOREIGN KEY(category) REFERENCES categories(title) ON UPDATE CASCADE ON DELETE CASCADE)"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create products table!", e);
        }
    }




    public int insertCategory(final Category category) {
        return daoCategory.insertCategory(category);
    }

    public Category getCategory(String title) {
        return daoCategory.getCategory(title);
    }

    public List<Category> getCategoryList(final int page, final int size) {
        return daoCategory.getCategoryList(page, size);
    }

    public void updateCategory(String updateColumnName, String newValue, String searchColumnName, String searchValue){
        daoCategory.update(updateColumnName, newValue, searchColumnName, searchValue);
    }

    public void deleteCategory(final String title) {
        daoCategory.delete(title);
    }

    public void deleteAllCategories(){
        daoCategory.deleteAll();
    }



    public int insertProduct(final Product product) {
        return daoProduct.insertProduct(product);
    }

    public Product getProduct(int id){
        return daoProduct.getProduct(id);
    }

    public Product getProduct(String title){
        return daoProduct.getProduct(title);
    }

    public List<Product> getProductList(final int page, final int size, final ProductFilter productFilter) {
        return daoProduct.getProductList(page, size, productFilter);
    }

    public void updateProduct(String updateColumnName, String newValue, String searchColumnName, String searchValue){
        daoProduct.update(updateColumnName, newValue, searchColumnName, searchValue);
    }

    public void deleteProduct(final String title) {
        daoProduct.delete(title);
    }

    public void deleteAllProducts(){
        daoProduct.deleteAll();
    }


    public void shutdown(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw  new RuntimeException("Failed to close connection!", e);
        }
    }
}
