package org.example;

import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
        File CSV = new File("src/GenshinCharacters.csv");
        File CSV2 = new File("src/GenshinCharactersInfo.csv");
        File XML = new File("src/GenshinCharacters.xml");

        // Configuración del controlador de Selenium
        System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/home/usuario/Descargas/firefox-119.0/firefox/firefox");
        WebDriver driver = new FirefoxDriver(options);
        driver.get("https://genshin.gg/");

        // Configura el tiempo de ejecución del archivo, hace clic en la pestaña de cookies y cambia los switchs de los elementos
        WebDriverWait wdw = new WebDriverWait(driver, 10);
        wdw.until(ExpectedConditions.elementToBeClickable(By.id("sp_message_iframe_873935")));
        driver.switchTo().frame("sp_message_iframe_873935");
        wdw.until(ExpectedConditions.elementToBeClickable(By.className("sp_choice_type_11")));
        WebElement acceptButton = driver.findElement(By.className("sp_choice_type_11"));
        acceptButton.click();
        driver.switchTo().defaultContent();

        // Obtiene la lista de personajes
        WebElement characterList = driver.findElement(By.className("character-list"));

        // Estructura de datos para almacenar los datos
        ArrayList<ArrayList<String>> FilesData = new ArrayList<>();
        ArrayList<ArrayList<String>> FilesData2 = new ArrayList<>();

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
            String Element = typeElement.getAttribute("alt");
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
            Data.add(Element);
            Data.add(Rarity);
            FilesData.add(Data);
        }

        // recorrer la lista de personajes, guardar su enlace e ir haciendo get de la
        // página del enlace para extraer la información de cada personaje

        ArrayList<String> CharacterLinks = new ArrayList<>();

        for (WebElement characterElement : characterElements) {
            characterElement.getAttribute("href");
            CharacterLinks.add(characterElement.getAttribute("href"));
        }

        for (String CharacterLink : CharacterLinks) {
            ArrayList<String> Data2 = new ArrayList<>();

            driver.get(CharacterLink);

            WebElement characterInfo = driver.findElement(By.className("character-intro"));

            WebElement WeaponElement = characterInfo.findElement(By.className("character-path"));
            WebElement ClassElement = characterInfo.findElement(By.className("character-role"));

            String Weapon = WeaponElement.getText();
            String Class = ClassElement.getText();

            Data2.add(Weapon);
            Data2.add(Class);
            FilesData2.add(Data2);
        }

        // Escribe datos en un archivo CSV
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV))) {
            String[] header = {"Name", "Element", "Rarity"};
            writer.writeNext(header);
            for (ArrayList<String> Line : FilesData) {
                String[] LineArray = new String[Line.size()];
                LineArray = Line.toArray(LineArray);
                writer.writeNext(LineArray);
            }
        }

        try (CSVWriter writer2 = new CSVWriter(new FileWriter(CSV2))) {
            String[] header2 = {"Weapon", "Class"};
            writer2.writeNext(header2);
            for (ArrayList<String> Line2 : FilesData2) {
                String[] LineArray2 = new String[Line2.size()];
                LineArray2 = Line2.toArray(LineArray2);
                writer2.writeNext(LineArray2);
            }
        }

        // Genera archivo XML
        GenerateXMLFile(XML, FilesData, FilesData2);

        driver.quit();
    }

    /**
     * Método para generar un archivo XML a partir de los datos.
     *
     * @param XMLfile Archivo XML de salida.
     * @param XMLdata1 Datos a ser incluidos en el archivo XML.
     * @param XMLdata2 Datos a ser incluidos en el archivo XML.
     */
    private static void GenerateXMLFile(File XMLfile, ArrayList<ArrayList<String>> XMLdata1, ArrayList<ArrayList<String>> XMLdata2) {
        try {
            // Configuración del generador de documentos XML
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Creación del documento XML y el elemento raíz
            Document doc = docBuilder.newDocument();
            org.w3c.dom.Element rootElement = doc.createElement("characters");
            doc.appendChild(rootElement);

            // Iteración sobre los datos para crear elementos XML individuales
            for (int i = 0; i < Math.max(XMLdata1.size(), XMLdata2.size()); i++) {
                org.w3c.dom.Element characterElement = doc.createElement("character");

                // Agrega datos de la primera ArrayList si están disponibles
                if (i < XMLdata1.size()) {
                    org.w3c.dom.Element nameElement = doc.createElement("name");
                    nameElement.appendChild(doc.createTextNode(XMLdata1.get(i).get(0)));
                    characterElement.appendChild(nameElement);

                    org.w3c.dom.Element typeElement = doc.createElement("type");
                    typeElement.appendChild(doc.createTextNode(XMLdata1.get(i).get(1)));
                    characterElement.appendChild(typeElement);

                    org.w3c.dom.Element rarityElement = doc.createElement("rarity");
                    rarityElement.appendChild(doc.createTextNode(XMLdata1.get(i).get(2)));
                    characterElement.appendChild(rarityElement);
                }

                // Agrega datos de la segunda ArrayList si están disponibles
                if (i < XMLdata2.size()) {
                    org.w3c.dom.Element weaponElement = doc.createElement("weapon");
                    weaponElement.appendChild(doc.createTextNode(XMLdata2.get(i).get(0)));
                    characterElement.appendChild(weaponElement);

                    org.w3c.dom.Element classElement = doc.createElement("class");
                    classElement.appendChild(doc.createTextNode(XMLdata2.get(i).get(1)));
                    characterElement.appendChild(classElement);
                }

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