package com.automacao.cadastrador_usuarios.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ModalClose {

    private final By FECHAR_MODAL = By.xpath("//button[@class='close']");

    public void fecharModal(WebDriver driver) {
        try {
            driver.findElement(FECHAR_MODAL).click();
        } catch (Exception ignored) {}
    }
}
