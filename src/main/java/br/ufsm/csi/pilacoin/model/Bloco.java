package br.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bloco {
    private Long numeroBloco;
    private BigInteger nonceBlocoAnterior;
    private String nonce;
    private byte[] chaveUsuarioMinerador;
    private String nomeUsuarioMinerador;
    private List<Transacoes> transacoes;
}
