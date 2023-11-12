package br.ufsm.csi.pilacoin;

import br.ufsm.csi.pilacoin.model.PilaCoin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRabbit
@SpringBootApplication
public class PilacoinApplication {

	public static void main(String[] args) {
		SpringApplication.run(PilacoinApplication.class, args);
	}

}
