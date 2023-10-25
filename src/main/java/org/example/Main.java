package org.example;

import java.io.File;
import java.io.FileWriter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/home/usuario/Descargas/firefox-119.0/firefox/firefox");
        WebDriver driver = new FirefoxDriver(options);
        driver.get("https://www.aviariojp.org/tipos-de-pajaros/");

        File inputCSV = new File("src/Pájaros.csv");
        File outputCSV = new File("src/Pruebas.csv");
        File inputXML = new File("src/Pájaros.xml");
        File outputXML = new File("src/Pruebas.xml");

        Scanner scannerCSV = new Scanner(inputCSV);
        FileWriter writerCSV = new FileWriter(outputCSV);
        Scanner scannerXML = new Scanner(inputXML);
        FileWriter writerXML = new FileWriter(outputXML);

        while(scannerCSV.hasNextLine()) {
            String csvLine = scannerCSV.nextLine();
            writerCSV.write(csvLine + "\n");
            System.out.println(csvLine);
        }
        while(scannerXML.hasNextLine()) {
            String xmlLine = scannerXML.nextLine();
            writerXML.write(xmlLine + "\n");
            System.out.println(xmlLine);
        }

        scannerCSV.close();
        writerCSV.close();
        scannerXML.close();
        writerXML.close();
    }
}