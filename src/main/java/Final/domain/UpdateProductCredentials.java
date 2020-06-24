package Final.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class UpdateProductCredentials {
    private int id;
    private String title;
    private String description;
    private String producer;
    private Double price;
    private Integer quantity;
    private String category;
}
