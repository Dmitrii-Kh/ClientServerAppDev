package lab03.lab04;

import java.util.Set;

public class Criteria {

    private Set<Integer> ids;
    private String query;
    private Double fromPrice;
    private Double toPrice;
    private Integer fromQuantity;
    private Integer toQuantity;


    public Set<Integer> getIds() {
        return ids;
    }

    public void setIds(Set<Integer> ids) {
        this.ids = ids;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Double getFromPrice() {
        return fromPrice;
    }

    public void setFromPrice(Double fromPrice) {
        this.fromPrice = fromPrice;
    }

    public Double getToPrice() {
        return toPrice;
    }

    public void setToPrice(Double toPrice) {
        this.toPrice = toPrice;
    }

    public Integer getFromQuantity() {
        return fromQuantity;
    }

    public void setFromQuantity(Integer fromQuantity) {
        this.fromQuantity = fromQuantity;
    }

    public Integer getToQuantity() {
        return toQuantity;
    }

    public void setToQuantity(Integer toQuantity) {
        this.toQuantity = toQuantity;
    }

}
