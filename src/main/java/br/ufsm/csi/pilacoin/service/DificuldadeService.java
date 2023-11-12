package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Dificuldade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DificuldadeService {
    private BigInteger dificuldade;

    public BigInteger getDif() {
        return dificuldade;
    }

    public void setDif(BigInteger novaDificuldade) {
        synchronized (this) {
            dificuldade = novaDificuldade;
            // Notifique todas as threads que estão esperando pela dificuldade
            this.notifyAll();
        }
    }

    private BigInteger dif;

    @RabbitListener(queues = {"dificuldade"})
    public void receivePilaCoin(@Payload String strDificuldade) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Dificuldade dificuldade = objectMapper.readValue(strDificuldade, Dificuldade.class);
        BigInteger dificuldadeFinal = new BigInteger(dificuldade.getDificuldade(), 16).abs();
        setDif(dificuldadeFinal);
    }
}
