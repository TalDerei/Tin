package edu.lehigh.cse216.tad222.backend;

import java.security.PublicKey;
import java.util.HashMap;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

class MockDatabase {

    HashMap<String, PublicKey> jwtPubKeys = new HashMap<String, PublicKey>();
    
    public MockDatabase() {

    }

    String produceJWTKey(User u) throws JoseException{
        // Generate an RSA key pair, which will be used for signing and verification of the JWT, wrapped in a JWK
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        
        // Give the JWK a Key ID (kid), which is just the polite thing to do
        rsaJsonWebKey.setKeyId("k" + jwtPubKeys.size());
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("BuzzServer");
        claims.setAudience(u.getEmail());
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setClaim("email", u.getEmail());
        claims.setClaim("name", "name");
        claims.setClaim("biography", u.getBio());
        claims.setClaim("userID", u.getUserID());

        JsonWebSignature jws = new JsonWebSignature();

        // The payload comes in a json format
        jws.setPayload(claims.toJson());
        // The JWT is signed using the private key
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        // Set the key ID header
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        // Set the signature algorithm
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        
        String uid = u.getUserID();
        jwtPubKeys.put(uid, rsaJsonWebKey.getPublicKey());

        return jws.getCompactSerialization();
    }

    Object verify(String uid, String jwt) {
        if(uid.isEmpty() || uid == null || jwt.isEmpty() || jwt == null) {
            return new StructuredResponse("error", "No uid and/or jwt given", null);
        }
        JsonWebSignature jws = new JsonWebSignature(); 
        PublicKey pk = jwtPubKeys.get(uid);
        boolean verified = false;
        jws.setAlgorithmConstraints(
                new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256));
        try {
            jws.setCompactSerialization(jwt);
            jws.setKey(pk);
            verified = jws.verifySignature();
        } catch (JoseException je) {
            je.printStackTrace();
        }

        if(verified) {
            return new StructuredResponse("error", "Couldn't verify user", jwt);
        }

        return "Verification successful";
    }
}