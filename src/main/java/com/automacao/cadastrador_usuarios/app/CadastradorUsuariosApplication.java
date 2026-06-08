package com.automacao.cadastrador_usuarios.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "com.automacao.cadastrador_usuarios")
public class CadastradorUsuariosApplication implements CommandLineRunner {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CadastradorUsuariosApplication.class).headless(false).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        new Runner().start();
    }
}   
