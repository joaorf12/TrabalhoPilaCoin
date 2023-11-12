package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Chaves;
import br.ufsm.csi.pilacoin.model.PilaCoin;
import br.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.ufsm.csi.pilacoin.model.ValidacaoPilaJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.coyote.http11.filters.ChunkedInputFilter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
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
import java.util.ArrayList;

@Service
public class ValidaPilaService {
    private final RequisicoesService requisicoesService;
    private DificuldadeService dificuldadeService;
    private static ArrayList<String> ignorePilas = new ArrayList<>();

    public ValidaPilaService(RequisicoesService requisicoesService, DificuldadeService dificuldadeService) {
        this.requisicoesService = requisicoesService;
        this.dificuldadeService = dificuldadeService;
    }

    @SneakyThrows
    @RabbitListener(queues = {"pila-minerado"})
    public void getMinerados(@Payload String strPila){

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean pilaExistente = true;

                for (String pila : ignorePilas) {
                    if (pila.equals(strPila)) {
                        pilaExistente = true;
                        break;
                    }
                }

                if (pilaExistente) {
                    ignorePilas.add(strPila);

                    //System.out.println(ignorePilas);

                    ObjectMapper ob = new ObjectMapper();
                    PilaCoinJson pilaCoin = ob.readValue(strPila, PilaCoinJson.class);

                    if (pilaCoin.getNomeCriador().equals("joao_leo")) {
                        requisicoesService.enviarRequisicao("pila-minerado", strPila);
                    } else {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        BigInteger hash = new BigInteger(md.digest(strPila.getBytes(StandardCharsets.UTF_8))).abs(); //Ver se não falta isso nas outras hashs

                        synchronized (dificuldadeService) {
                            if (dificuldadeService.getDif() == null) {
                                dificuldadeService.wait();
                            }
                        }

                        if (!(hash.compareTo(dificuldadeService.getDif()) > 0)) {

                            System.out.println("Validando pila do(a): " + pilaCoin.getNomeCriador());
                            md.reset();
                            byte[] hash2 = md.digest(strPila.getBytes(StandardCharsets.UTF_8));
                            Cipher cipher = Cipher.getInstance("RSA");

                            Chaves chaves = new Chaves();
                            PrivateKey privateKey = chaves.getPrivateKey();
                            PublicKey publicKey =  chaves.getPublicKey();

                            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                            String strHash = ob.writeValueAsString(hash2);
                            byte[] assinatura = md.digest(strHash.getBytes(StandardCharsets.UTF_8));
                            ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().pilaCoin(pilaCoin).
                                    assinaturaPilaCoin(cipher.doFinal(assinatura)).
                                    nomeValidador("joao_leo").
                                    chavePublicaValidador(publicKey.toString().getBytes(StandardCharsets.UTF_8))
                                    .build();
                            String jsonValidado = ob.writeValueAsString(validacaoPilaJson);
                            requisicoesService.enviarRequisicao("pila-validado", jsonValidado);
                            System.out.println("Minha assinatura: "+ jsonValidado);
                            System.out.println("Validado!");
                        } else {
                            requisicoesService.enviarRequisicao("pila-minerado", strPila);
                        }
                    }

                }
            }
        }).start();
    }

    public static PrivateKey readPrivateKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    // Método para ler a chave pública de um arquivo
    public static PublicKey readPublicKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }
}
