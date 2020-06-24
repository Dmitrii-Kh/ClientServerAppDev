package Final.database;


import Final.ProductFilter;
import Final.entities.Category;
import Final.entities.Product;
import Final.entities.User;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Database {

    private static final String fileName = "database.db";

    private static volatile Database instance;
    private final Connection connection;
    private final DaoProduct daoProduct;
    private final DaoCategory daoCategory;
    private final DaoUser daoUser;


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
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection("jdbc:sqlite:" + fileName, config.toProperties());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC not found!", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        initProductGroupsTable();
        initProductsTable();
        initUserTable();

        daoProduct = new DaoProduct(connection);
        daoCategory = new DaoCategory(connection);
        daoUser = new DaoUser(connection);
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
        initUserTable();

        daoProduct = new DaoProduct(connection);
        daoCategory = new DaoCategory(connection);
        daoUser = new DaoUser(connection);
    }


    private void initProductGroupsTable() {
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

    private void initProductsTable() {
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


    private void initUserTable() {
        try (final Statement statement = connection.createStatement()) {
            statement.execute("create table if not exists 'users'('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' VARCHAR(50) NOT NULL, 'password' VARCHAR(250) NOT NULL, 'role' VARCHAR(20) NOT NULL, UNIQUE (login))");
        } catch (final SQLException e) {
            throw new RuntimeException("Can't create table", e);
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

    public void updateCategory(String updateColumnName, String newValue, String searchColumnName, String searchValue) {
        daoCategory.update(updateColumnName, newValue, searchColumnName, searchValue);
    }

    public void deleteCategory(final String title) {
        daoCategory.delete(title);
    }

    public void deleteAllCategories() {
        daoCategory.deleteAll();
    }


    public int insertProduct(final Product product) {
        return daoProduct.insertProduct(product);
    }

    public Product getProduct(int id) {
        return daoProduct.getProduct(id);
    }

    public Product getProduct(String title) {
        return daoProduct.getProduct(title);
    }

    public List<Product> getProductList(final int page, final int size, final ProductFilter productFilter) {
        return daoProduct.getProductList(page, size, productFilter);
    }

    public void updateProduct(String updateColumnName, String newValue, String searchColumnName, String searchValue) {
        daoProduct.update(updateColumnName, newValue, searchColumnName, searchValue);
    }

    public void deleteProduct(final String title) {
        daoProduct.delete(title);
    }

    public void deleteAllProducts() {
        daoProduct.deleteAll();
    }


    public int insertUser(final User user) {
        return daoUser.insertUser(user);
    }

    public User getUser(final String login) {
        return daoUser.getByLogin(login);
    }

    public void deleteAllUsers() {
        daoUser.deleteAll();
    }

    public void shutdown() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection!", e);
        }
    }
}
