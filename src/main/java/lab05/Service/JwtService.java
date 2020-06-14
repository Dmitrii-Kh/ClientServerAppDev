package lab05.Service;

import java.security.Key;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lab04.entities.User;


public class JwtService {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    //todo made valid for limited period of time
    public static String generateToken(final User user) {

        return Jwts.builder()
                .setSubject(user.getLogin())
                .signWith(SECRET_KEY)
                .claim("role", user.getRole())
                .compact();
    }



    public static String getUsernameFromToken(String jwt) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }

}
