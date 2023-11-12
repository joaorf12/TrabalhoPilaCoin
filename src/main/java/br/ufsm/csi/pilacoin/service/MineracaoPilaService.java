package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Chaves;
import br.ufsm.csi.pilacoin.model.PilaCoin;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Random;

@Service
public class MineracaoPilaService {

    private DificuldadeService dificuldadeService;
    private RequisicoesService requisicoesService;

    public MineracaoPilaService(DificuldadeService dificuldadeService, RequisicoesService requisicoesService) {
        this.dificuldadeService = dificuldadeService;
        this.requisicoesService = requisicoesService;
    }

    //@PostConstruct
    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    @SneakyThrows
    public void iniciaMineracao () {
        // Leia as chaves de volta dos arquivos
        Chaves chaves = new Chaves();
        PrivateKey privateKey = chaves.getPrivateKey();
        PublicKey publicKey =  chaves.getPublicKey();

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                PilaCoin pilaCoin = PilaCoin.builder().chaveCriador(publicKey.getEncoded())
                        .dataCriacao(new Date(System.currentTimeMillis())).nomeCriador("joao_leo").build();

                synchronized (dificuldadeService) {
                    if (dificuldadeService.getDif() == null) {
                        dificuldadeService.wait();
                    }
                }

                BigInteger hash;
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                int total = 0;
                do {
                    Random random = new Random();
                    byte[] byteArray = new byte[256/8];
                    random.nextBytes(byteArray);

                    pilaCoin.setNonce(new BigInteger(byteArray).abs().toString());

                    ObjectMapper om = new ObjectMapper();

                    String json = om.writeValueAsString(pilaCoin);
                    hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
                    total++;

                } while (hash.compareTo(dificuldadeService.getDif()) > 0);
                //achou!!!!
                System.out.println("Pila minerado com "+total+" tentativas");
                ObjectMapper om = new ObjectMapper();

                requisicoesService.enviarRequisicao("pila-minerado", om.writeValueAsString(pilaCoin));
            }
        }).start();
    }

//    public static PrivateKey readPrivateKeyFromFile(String fileName) throws Exception {
//        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
//        return keyFactory.generatePrivate(keySpec);
//    }
//
//    // Método para ler a chave pública de um arquivo
//    public static PublicKey readPublicKeyFromFile(String fileName) throws Exception {
//        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
//        return keyFactory.generatePublic(keySpec);
//    }


}
