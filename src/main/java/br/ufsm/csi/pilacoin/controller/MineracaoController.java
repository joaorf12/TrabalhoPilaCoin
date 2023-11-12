package br.ufsm.csi.pilacoin.controller;

import br.ufsm.csi.pilacoin.service.DificuldadeService;
import br.ufsm.csi.pilacoin.service.MineracaoPilaService;
import br.ufsm.csi.pilacoin.service.RequisicoesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/minerar")
public class MineracaoController {


    private final MineracaoPilaService mineracaoPilaService;

    public MineracaoController( MineracaoPilaService mineracaoPilaService) {
        this.mineracaoPilaService = mineracaoPilaService;
    }


    @PostMapping("/iniciar")
    public void iniciarMineracao(@RequestParam int threads) {
        mineracaoPilaService.iniciaMineracao();
    }

}
