package br.ufsm.csi.pilacoin.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class RequisicoesService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void enviarRequisicao(String routingKey, String json) {
        this.rabbitTemplate.convertAndSend(routingKey, json);
    }

    @RabbitListener(queues = {"joao_leo"})
    public void recebeMensagem(@Payload Message message) {
        String responseMessage = new String(message.getBody());
        System.out.println("Mensagem: " + responseMessage);
    }
}
