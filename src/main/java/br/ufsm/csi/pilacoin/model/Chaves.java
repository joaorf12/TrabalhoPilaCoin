package br.ufsm.csi.pilacoin.model;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Chaves {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @SneakyThrows
    public PrivateKey getPrivateKey() {
        byte[] keyBytes = Files.readAllBytes(Paths.get("private_key.pem"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        this.privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    @SneakyThrows
    public PublicKey getPublicKey() {
        byte[] keyBytes = Files.readAllBytes(Paths.get("public_key.pem"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        this.publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }
}
