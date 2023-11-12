package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Bloco;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;

@Service
public class ValidaBlocoService {

    private final RequisicoesService requisicoesService;
    private DificuldadeService dificuldadeService;
    private static ArrayList<String> ignoreBlocos = new ArrayList<>();

    public ValidaBlocoService(RequisicoesService requisicoesService, DificuldadeService dificuldadeService) {
        this.requisicoesService = requisicoesService;
        this.dificuldadeService = dificuldadeService;
    }

    @SneakyThrows
    @RabbitListener(queues = {"bloco-minerado"})
    public void getMinerados(@Payload String strBloco) {

        System.out.println(strBloco);

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean blocoExistente = true;

                for (String bloco : ignoreBlocos) {
                    if (bloco.equals(strBloco)) {
                        blocoExistente = true;
                        break;
                    }
                }

                if (blocoExistente) {
                    ignoreBlocos.add(strBloco);

                    ObjectMapper ob = new ObjectMapper();
                    Bloco bloco = ob.readValue(strBloco, Bloco.class);

                    if (bloco.getNomeUsuarioMinerador().equals("joao_leo")) {
                        requisicoesService.enviarRequisicao("bloco-minerado", strBloco);
                    } else {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        BigInteger hash = new BigInteger(md.digest(strBloco.getBytes(StandardCharsets.UTF_8))).abs(); //Ver se não falta isso nas outras hashs

                        synchronized (dificuldadeService) {
                            if (dificuldadeService.getDif() == null) {
                                dificuldadeService.wait();
                            }
                        }

                        if (!(hash.compareTo(dificuldadeService.getDif()) > 0)) {
                            System.out.println("Validando bloco do(a): " + bloco.getNomeUsuarioMinerador());
                            requisicoesService.enviarRequisicao("bloco-minerado", strBloco);
                        }
                    }
                }
            }
        }).start();
    }
}
