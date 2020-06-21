package Final.domain;


import lombok.Data;

@Data(staticConstructor = "of")
public class ErrorResponse {
    private final String message;
}
