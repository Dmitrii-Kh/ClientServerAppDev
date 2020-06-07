package lab04.database;

import lab04.ProductFilter;
import lab04.entities.Category;
import lab04.entities.Product;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DaoProduct {

    private final Connection connection;

    public DaoProduct(final Connection connection) {
        this.connection = connection;
    }


    public int insertProduct(final Product product) {
        try (PreparedStatement insertStatement = connection.prepareStatement(
                "insert into 'products'('title', 'description', 'producer', 'price', 'quantity', 'category') " +
                        "values (?, ?, ?, ?, ?, ?)")) {
            insertStatement.setString(1, product.getTitle());
            insertStatement.setString(2, product.getDescription());
            insertStatement.setString(3, product.getProducer());
            insertStatement.setDouble(4, product.getPrice());
            insertStatement.setInt(5, product.getQuantity());
            insertStatement.setString(6, product.getCategory());
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
                    String.format("select count(*) as num_of_products from 'products' where title = '%s'",
                            productTitle));
            result.next();
            return result.getInt("num_of_products") == 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can`t create product with this title", e);
        }
    }


    public Product getProduct(int id) {
        ProductFilter productFilter = new ProductFilter();
        productFilter.setIds(Set.of(id));
        List<Product> list = getProductList(0, 1, productFilter);
        return list.isEmpty() ? null : list.get(0);
    }

    public Product getProduct(String title) {
        try (final Statement statement = connection.createStatement()) {

            final String    query     = "SELECT * FROM 'products' WHERE title = '" + title + "'";
            final ResultSet resultSet = statement.executeQuery(query);

            return resultSet.next() ?
                    new Product(
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getString("producer"),
                            resultSet.getDouble("price"),
                            resultSet.getInt("quantity"),
                            resultSet.getString("category"))
                    : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product!", e);
        }
    }

    public List<Product> getProductList(final int page, final int size, final ProductFilter productFilter) {
        try (final Statement statement = connection.createStatement()) {

            final String where = createWhereClause(productFilter);

            final String finalSqlQuery =
                    String.format("select * from 'products' %s limit %s offset %s", where, size, page * size);
            System.out.println(finalSqlQuery);
            final ResultSet resultSet = statement.executeQuery(finalSqlQuery);


            final List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                products.add(new Product(resultSet.getInt("id"), resultSet.getString("title"),
                        resultSet.getString("description"), resultSet.getString("producer"), resultSet.getInt("price"),
                        resultSet.getInt("quantity"), resultSet.getString("category")));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create list by criteria!", e);
        }
    }

    private static String createWhereClause(ProductFilter productFilter) {
        final String query = Stream.of(like("title", productFilter.getQuery()), in("id", productFilter.getIds()),
                range("price", productFilter.getFromPrice(), productFilter.getToPrice()),
                range("quantity", productFilter.getFromQuantity(), productFilter.getToQuantity()))
                .filter(Objects::nonNull).collect(Collectors.joining(" AND "));

        final String where = query.isEmpty() ? "" : "where " + query;
        return where;
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

        if (from != null && to == null) return fieldName + " > " + from;

        if (from == null && to != null) return fieldName + " < " + to;

        return fieldName + " BETWEEN " + from + " AND " + to;
    }

    private static String range(final String fieldName, final Integer from, final Integer to) {
        if (from == null && to == null) return null;

        if (from != null && to == null) return fieldName + " > " + from;

        if (from == null && to != null) return fieldName + " < " + to;

        return fieldName + " BETWEEN " + from + " AND " + to;
    }


    public void update(String updateColumnName, String newValue, String searchColumnName, String searchValue) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "update 'products' set " + updateColumnName + " = '" + newValue + "' where " + searchColumnName +
                            " = '" + searchValue + "'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table!", e);
        }
    }


    public void delete(final String title) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format("delete from 'products' where title = '%s'", title));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete by title!", e);
        }
    }

    public void deleteAll() {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from 'products'");
            statement.executeUpdate("delete from sqlite_sequence where name='products'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all!", e);
        }
    }

}
