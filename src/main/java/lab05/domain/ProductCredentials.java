package lab05.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")

public class ProductCredentials {
    private String title;
    private String description;
    private String producer;
    private double price;
    private int    quantity;
    private String category;
}
