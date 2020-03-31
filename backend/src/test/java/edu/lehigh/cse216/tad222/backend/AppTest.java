package edu.lehigh.cse216.tad222.backend;

import java.util.Arrays;
import java.util.List;

import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testJWTVerify() {
        User u = new User("email", "nickname", "a", "This is fake");
        RsaJsonWebKey rsaJsonWebKey = null;
        try {
            rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        } catch (JoseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        JwtClaims claims = new JwtClaims();
        claims.setIssuer("Issuer");  // who creates the token and signs it
        claims.setAudience("Audience"); // to whom the token is intended to be sent
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created (now)
        claims.setSubject("subject"); // the subject/principal is whom the token is about

        JsonWebSignature jws = new JsonWebSignature();

        jws.setPayload(claims.toJson());

        jws.setKey(rsaJsonWebKey.getPrivateKey());

        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        String jwt = "";
        try {
            jwt = jws.getCompactSerialization();
        } catch (JoseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        MockDatabase db = new MockDatabase();
        db.jwtPubKeys.put(u.getUserID(), rsaJsonWebKey.getPublicKey());
        assertTrue(db.verify(u.getUserID(), jwt));
        db.jwtPubKeys.remove(u.getUserID());
    }

    public void verifyDatabaseJWTProduction(){
        MockDatabase db = new MockDatabase();
        User u = new User("email", "nickname", "b", "This is fake");
        String jwt = "";
        try {
            jwt = db.produceJWTKey(u);
        } catch (JoseException e) {
            e.printStackTrace();
        }
        assertTrue(db.jwtPubKeys.get(u.getUserID()) != null);
        db.jwtPubKeys.remove(u.getUserID());
    }
}
