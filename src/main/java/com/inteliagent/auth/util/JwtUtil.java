package com.inteliagent.auth.util;
import com.inteliagent.auth.entity.Usuario;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
@Component
public class JwtUtil {
    private final JwtEncoder encoder;
    private final String issuer;
    public JwtUtil(SecretKeySpec key, @Value("${security.jwt.issuer}") String issuer) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.issuer = issuer;
    }
    public String generateToken(Usuario user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getEmail()).issuedAt(now)
            .expiresAt(now.plusSeconds(1800)).claim("uid", user.getId()).claim("cede", user.getIdCede())
            .claim("ver", user.getTokenVersion()).claim("roles", List.of(user.getRole())).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
