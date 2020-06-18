package lab05.HTTP;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Endpoint {

    private final String method;
    private final Pattern urlPattern;
    private final CustomHandler httpHandler;
    private final BiFunction<String, Pattern, Map<String, String>> paramsExtractor;


    public static Endpoint of(final String method, String pattern, final CustomHandler httpHandler, final BiFunction<String, Pattern, Map<String, String>> paramsExtractor) {
        return new Endpoint(method.toLowerCase(), Pattern.compile(pattern), httpHandler, paramsExtractor);
    }


    public boolean matches(final String method, final String uri) {
        return this.method.equals(method.toLowerCase()) && urlPattern.matcher(uri).matches();
    }


    public HttpHandler handler() {
        return httpExchange -> {
            final Map<String, String> params = paramsExtractor.apply(httpExchange.getRequestURI().toString(), urlPattern);
            httpHandler.handle(httpExchange, params);
        };
    }


    public interface CustomHandler {
        void handle(HttpExchange exchange, Map<String, String> pathParams);
    }

}
