package lab03.lab04;

import lab03.lab04.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DaoProduct {

    private final Connection connection;

    public DaoProduct(final String fileName) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC not found!", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initTable();
    }

    public void initTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("create table if not exists 'products' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'title' VARCHAR(250), 'price' DECIMAL(10, 3))");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create a table!", e);
        }
    }

    public int insertProduct(final Product product) {
        try (PreparedStatement insertStatement = connection.prepareStatement("insert into 'products'('title', 'price') values (?, ?)")) {
            insertStatement.setString(1, product.getTitle());
            insertStatement.setDouble(2, product.getPrice());
            insertStatement.execute();

            final ResultSet result = insertStatement.getGeneratedKeys();
            return result.getInt("last_insert_rowid()");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert product!", e);
        }
    }

    public boolean productTitleAlreadyExists(final String productTitle) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet result = statement.executeQuery(
                    String.format("select count(*) as num_of_products from 'products' where title = '%s'", productTitle)
            );
            result.next();
            return result.getInt("num_of_products") == 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can`t create product with this title", e);
        }
    }

    public List<Product> getProductList(final int page, final int size, final Criteria criteria) {
        try (final Statement statement = connection.createStatement()) {

            final String query = Stream.of(
                    like("title", criteria.getQuery()),
                    in("id", criteria.getIds()),
                    range("price", criteria.getFromPrice(), criteria.getToPrice())
            )
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" AND "));

            final String where = query.isEmpty() ? "" : "where " + query;
            final String finalSqlQuery = String.format("select * from 'products' %s limit %s offset %s", where, size, page * size);
            System.out.println(finalSqlQuery);
            final ResultSet resultSet = statement.executeQuery(finalSqlQuery);


            final List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                products.add(new Product(resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getDouble("price")));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create list by criteria!", e);
        }
    }

    private static String like(final String fieldName, final String query) {
        return query != null ? fieldName + " LIKE  '%" + query + "%'" : null;
    }

    private static String in(final String fieldName, final Collection<?> collection) {
        if (collection == null || collection.isEmpty()) return null;
        return fieldName + " IN (" + collection.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }

    private static String range(final String fieldName, final Double from, final Double to) {
        if (from == null && to == null) return null;

        if (from != null && to == null)
            return fieldName + " > " + from;

        if (from == null && to != null)
            return fieldName + " < " + to;

        return fieldName + " BETWEEN " + from + " AND " + to;
    }

    public void deleteByTitle(final String title) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format("delete from 'products' where title = '%s'", title));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete by title!", e);
        }
    }

    public void update(String updateColumnName, String newValue, String searchColumnName, String searchValue){
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("update 'products' set " + updateColumnName + " = '" + newValue +
                    "' where " + searchColumnName + " = '" + searchValue + "'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table!", e);
        }
    }

    public void deleteAll(){
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from 'products'");
            statement.executeUpdate("delete from sqlite_sequence where name='products';");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all!", e);
        }
    }

}
