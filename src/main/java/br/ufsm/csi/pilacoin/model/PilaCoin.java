package br.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PilaCoin {

    private byte[] chaveCriador;
    private String nomeCriador;
    private Date dataCriacao;
    private byte[] nonce; //big integer de 256bits

}
