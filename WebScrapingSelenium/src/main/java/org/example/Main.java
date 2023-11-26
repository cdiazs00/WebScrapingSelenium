package org.example;

import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal que realiza la extracción de datos de personajes desde un sitio web y los almacena en archivos CSV y XML.
 */
public class Main {

    /**
     * Método principal que inicia el proceso de extracción de datos y almacenamiento.
     *
     * @param args Argumentos de la línea de comandos (no se utilizan en este caso).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    public static void main(String[] args) throws IOException {

        // Define los archivos de salida CSV y XML
        File CSV = new File("src/Characters.csv");
        File XML = new File("src/Characters.xml");

        // Configuración del controlador de Selenium
        System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/home/usuario/Descargas/firefox-119.0/firefox/firefox");
        WebDriver driver = new FirefoxDriver(options);
        driver.get("https://genshin.gg/");

        // Obtiene la lista de personajes
        WebElement characterList = driver.findElement(By.className("character-list"));

        // Estructura de datos para almacenar los datos
        ArrayList<ArrayList<String>> FilesData = new ArrayList<>();

        // Obtiene elementos individuales de personajes
        List<WebElement> characterElements = characterList.findElements(By.className("character-portrait"));

        for (WebElement characterElement : characterElements) {
            // Estructura de datos para almacenar los datos de un personaje
            ArrayList<String> Data = new ArrayList<>();

            // Obtiene elementos específicos del personaje
            WebElement nameElement = characterElement.findElement(By.className("character-name"));
            WebElement typeElement = characterElement.findElement(By.className("character-type"));
            WebElement starElement = characterElement.findElement(By.tagName("img"));

            // Obtiene datos del personaje
            String Name = nameElement.getText();
            String Type = typeElement.getAttribute("alt");
            String Stars = starElement.getAttribute("class");

            // Determina la rareza del personaje
            String Rarity;
            if (Stars.equals("character-icon rarity-4")) {
                Rarity = "4★";
            } else {
                Rarity = "5★";
            }

            // Agrega datos a la estructura principal
            Data.add(Name);
            Data.add(Type);
            Data.add(Rarity);
            FilesData.add(Data);
        }

        // Escribe datos en un archivo CSV
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV))) {
            for (ArrayList<String> Line : FilesData) {
                String[] LineArray = new String[Line.size()];
                LineArray = Line.toArray(LineArray);
                writer.writeNext(LineArray);
            }
        }

        // Genera archivo XML
        GenerateXMLFile(XML, FilesData);

        // Cierra el navegador
        driver.quit();
    }

    /**
     * Método para generar un archivo XML a partir de los datos.
     *
     * @param XMLfile Archivo XML de salida.
     * @param XMLdata Datos a ser incluidos en el archivo XML.
     */
    private static void GenerateXMLFile(File XMLfile, ArrayList<ArrayList<String>> XMLdata) {
        try {
            // Configuración del generador de documentos XML
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Creación del documento XML y el elemento raíz
            Document doc = docBuilder.newDocument();
            org.w3c.dom.Element rootElement = doc.createElement("characters");
            doc.appendChild(rootElement);

            // Iteración sobre los datos para crear elementos XML individuales
            for (ArrayList<String> Character : XMLdata) {
                org.w3c.dom.Element characterElement = doc.createElement("character");

                org.w3c.dom.Element nameElement = doc.createElement("name");
                nameElement.appendChild(doc.createTextNode(Character.get(0)));
                characterElement.appendChild(nameElement);

                org.w3c.dom.Element typeElement = doc.createElement("type");
                typeElement.appendChild(doc.createTextNode(Character.get(1)));
                characterElement.appendChild(typeElement);

                org.w3c.dom.Element rarityElement = doc.createElement("rarity");
                rarityElement.appendChild(doc.createTextNode(Character.get(2)));
                characterElement.appendChild(rarityElement);

                // Agrega el elemento del personaje al elemento raíz
                rootElement.appendChild(characterElement);
            }

            // Configuración y aplicación de la transformación para escribir el archivo XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(XMLfile);

            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}