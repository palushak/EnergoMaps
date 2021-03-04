import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import markers.Properties;

import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class ExportService extends Service<Void> {

    private Boolean currentState;
    private ObjectProperty<String> currentWork = new SimpleObjectProperty<>();
    //private List<File> files;
    private File file;
    private ObservableList<RowPlikCSV_Map> listOdczyty;
    private int counter = 0;
    private long max = 0;

    long tStart;
    long tEnd;

    private GeoApiContext context;

    private Map<String, String> points;
    Map<String, Integer> miastaMap;
    Map<String, Integer> uliceMap;
    Map<String, Integer> domyMap;

    int iloscMiast = 0;
    int iloscUlic = 0;
    int iloscDomow = 0;

    private static Stage toastStage;


    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(
                        () -> {
                            setCurrentWork("Start");
                        }
                );

                counter = 0;
                updateProgress(counter, max);
                currentState = true;
                tStart = System.currentTimeMillis();

                listOdczyty = FXCollections.observableArrayList();
                points = new HashMap<>();


                try {

//                    for (File file : files) {

                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    bufferedReader.readLine();
                    String row;

                    Platform.runLater(
                            () -> {
                                setCurrentWork("1 z 3: dekodowanie pliku...");
                            }
                    );

                    while ((row = bufferedReader.readLine()) != null) {
                        if (!currentState) return null;

                        String[] line;
                        line = row.split(";");

                        RowPlikCSV_Map rowOdczyt = new RowPlikCSV_Map(
                                line[5],
                                line[0],
                                line[1]
                                        .replace("ul.", "")
                                        .replace("UL.", "")
                                        .replace("Ul.", "")
                                        .replace("uL.", ""),
                                line[2],
                                line[3],
                                line[4],
                                line[5],
                                line[6],
                                line[7]
                        );
//                        System.out.println(rowOdczyt.kod_punktu.toString());
                        listOdczyty.add(rowOdczyt);

//                        counter++;
//                        updateProgress(counter, max);
                    }

                    //} end for files


                } catch (Exception e) {
//                    ErrorToast("ERROR:", e.toString());
                    e.printStackTrace();

                }

//                EMarkers emarkersLabels = new EMarkers();
//                emarkersLabels.setType("LabelsCollection");
                List<markers.Marker> labelsList_12m = new ArrayList<markers.Marker>();
                List<markers.Marker> labelsList_6m = new ArrayList<markers.Marker>();
                List<markers.Marker> labelsList_2m = new ArrayList<markers.Marker>();

//                EMarkers emarkersMarkers = new EMarkers();
//                emarkersMarkers.setType("MarkersCollection");
                List<markers.Marker> markersList_12m = new ArrayList<markers.Marker>();
                List<markers.Marker> markersList_6m = new ArrayList<markers.Marker>();
                List<markers.Marker> markersList_2m = new ArrayList<markers.Marker>();

                List<markers.Marker> markersList_errors = new ArrayList<markers.Marker>();

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("2 z 3: geomapowanie adresow...");
                            }
                    );


                    context = new GeoApiContext.Builder().apiKey("AIzaSyDJBzpwv6y4wRCsXQv3gUBZr1J9PY3Xq5g").build();

                    String userOpegieka = "gcjp01";
                    String passOpegieka = "n9bDRzu2et2yP7cS";

                    countGroups();


                    for (RowPlikCSV_Map row : listOdczyty) {

                        if (!currentState) {
                            return null;
                        }


                        boolean errorXXYY = false;

                        markers.Marker marker = new markers.Marker();
                        markers.Properties properties = new markers.Properties();

                        markers.Marker markerLabel = new markers.Marker();
                        markers.Properties propertiesLabel = new markers.Properties();

                        markers.Geometry geometry = new markers.Geometry();
                        List<Double> coordinates = new ArrayList<>();

                        markers.Geometry geometryLabel = new markers.Geometry();
                        List<Double> coordinatesLabel = new ArrayList<>();

                        properties.setMag(0.0);
                        properties.setInkasent("");
                        properties.setMiasto(row.miejscowosc.getValue());
                        if (row.ulica.isNotNull().getValue()) {
                            properties.setUlica(row.ulica.getValue());
//                            properties.setUlica("testowa"+row.budynek.getValue());
                        } else {
                            properties.setUlica("");
                        }
                        properties.setStatus("");
                        if (row.budynek.isNotNull().getValue()) {
                            properties.setNumerDomu(row.budynek.getValue().toString());
                        } else {
                            properties.setNumerDomu("");
                            properties.setStatus("bez numeru domu");
//                            iloscBlednychOdczytowLabel++;
//                            errorsAddList.add(row.id_odczytu.getValue());
                        }

                        propertiesLabel.setMag(0.0);
                        propertiesLabel.setInkasent("");
                        propertiesLabel.setMiasto(row.miejscowosc.getValue());
                        if (row.ulica.isNotNull().getValue()) {
                            propertiesLabel.setUlica(row.ulica.getValue());
//                            propertiesLabel.setUlica("testowa"+row.budynek.getValue());
                        } else {
                            propertiesLabel.setUlica("");
                        }

                        if (row.id_punktu.isNotNull().getValue()) {
                            marker.setId_punktu(row.id_punktu.getValue());
                        } else {
                            marker.setId_punktu("");
                        }
                        if (row.id_punktu.isNotNull().getValue()) {
                            markerLabel.setId_punktu(row.id_punktu.getValue());
                        } else {
                            markerLabel.setId_punktu("");
                        }
                        markerLabel.setId_punktu("Label");
                        propertiesLabel.setStatus("");

                        Double latitude = 0.0;
                        Double longitude = 0.0;

                        String address = properties.getUlica() + " " + properties.getNumerDomu() + ", "// + properties.getKodPocztowy()
                                + ", " + properties.getMiasto() + ", Polska";

                        String[] xxyy = new String[2];

                        xxyy = geocodeOpegieka(properties, userOpegieka, passOpegieka);

                        String xx = "0.0";
                        String yy = "0.0";

                        if (xxyy[0] != null && xxyy[1] != null) {
                            xx = xxyy[0];
                            yy = xxyy[1];
                        }


                        if (xx.length() > 0 && yy.length() > 0) {

                            longitude = Double.parseDouble(xx);
                            latitude = Double.parseDouble(yy);

                        }
//                        System.out.println(longitude + ", " + latitude);
                        if (longitude < 2 && latitude < 2) {
                            errorXXYY = true;
                            System.out.println("ERROR: " + longitude + ", " + latitude);
                        }

                        coordinates.add(latitude);
                        coordinates.add(longitude);

                        geometry.setCoordinates(coordinates);
                        geometry.setType("Point");

                        marker.setProperties(properties);
                        marker.setGeometry(geometry);
                        marker.setType("Marker");

                        markerLabel.setProperties(propertiesLabel);
                        markerLabel.setType("Marker");

                        if (!errorXXYY) {

                            switch (row.cykl.getValue()) {

                                case "12m":
                                    boolean found = false;
                                    for (markers.Marker markersRow : markersList_12m) {

                                        if (markersRow.getProperties().getMiasto().equals(marker.getProperties().getMiasto())
                                                && markersRow.getProperties().getUlica().equals(marker.getProperties().getUlica())
                                                && markersRow.getId_punktu().equals(marker.getId_punktu())
                                                && markersRow.getProperties().getNumerDomu().equals(marker.getProperties().getNumerDomu())
                                                && markersRow.getProperties().getStatus().equals(marker.getProperties().getStatus())) {

                                            found = true;
                                            markersRow.getProperties().setIloscOdczytow(markersRow.getProperties().getIloscOdczytow() + 1);
                                        }
                                    }
                                    if (!found) {
                                        properties.setIloscOdczytow(1);
                                        markersList_12m.add(marker);
                                    }

                                    boolean foundLabel = false;
                                    for (markers.Marker markersRow : labelsList_12m) {

                                        if (markersRow.getId_punktu().equals(markerLabel.getId_punktu())
                                                && markersRow.getProperties().getMiasto().equals(markerLabel.getProperties().getMiasto())
                                                && markersRow.getProperties().getUlica().equals(markerLabel.getProperties().getUlica())
                                                && markersRow.getProperties().getStatus().equals(markerLabel.getProperties().getStatus())) {

                                            foundLabel = true;
                                            markersRow.getProperties().setIloscOdczytow(markersRow.getProperties().getIloscOdczytow() + 1);
                                        }
                                    }

                                    if (!foundLabel) {
                                        propertiesLabel.setIloscOdczytow(1);
                                        String[] xxyyL = new String[2];
                                        xxyyL = geocodeStreetOpegieka(propertiesLabel, userOpegieka, passOpegieka);
                                        String xxL = xxyyL[0];
                                        String yyL = xxyyL[1];
                                        if (xxL.length() > 0 && yyL.length() > 0) {
                                            Double latit = Double.parseDouble(yyL);
                                            Double longit = Double.parseDouble(xxL);
                                            coordinatesLabel.add(latit);
                                            coordinatesLabel.add(longit);
                                            geometryLabel.setCoordinates(coordinatesLabel);
                                            geometryLabel.setType("Point");
                                            markerLabel.setGeometry(geometryLabel);
                                            labelsList_12m.add(markerLabel);
                                        }
                                    }
                                    break;
                                case "6m":
                                    boolean found_6m = false;
                                    for (markers.Marker markersRow : markersList_6m) {

                                        if (markersRow.getProperties().getMiasto().equals(marker.getProperties().getMiasto())
                                                && markersRow.getProperties().getUlica().equals(marker.getProperties().getUlica())
                                                && markersRow.getId_punktu().equals(marker.getId_punktu())
                                                && markersRow.getProperties().getNumerDomu().equals(marker.getProperties().getNumerDomu())
                                                && markersRow.getProperties().getStatus().equals(marker.getProperties().getStatus())) {

                                            found_6m = true;
                                            markersRow.getProperties().setIloscOdczytow(markersRow.getProperties().getIloscOdczytow() + 1);
                                        }
                                    }
                                    if (!found_6m) {
                                        properties.setIloscOdczytow(1);
                                        markersList_6m.add(marker);
                                    }

                                    boolean foundLabel_6m = false;
                                    for (markers.Marker markersRow : labelsList_6m) {

                                        if (markersRow.getId_punktu().equals(markerLabel.getId_punktu())
                                                && markersRow.getProperties().getMiasto().equals(markerLabel.getProperties().getMiasto())
                                                && markersRow.getProperties().getUlica().equals(markerLabel.getProperties().getUlica())
                                                && markersRow.getProperties().getStatus().equals(markerLabel.getProperties().getStatus())) {

                                            foundLabel_6m = true;
                                            markersRow.getProperties().setIloscOdczytow(markersRow.getProperties().getIloscOdczytow() + 1);
                                        }
                                    }

                                    if (!foundLabel_6m) {
                                        propertiesLabel.setIloscOdczytow(1);
                                        String[] xxyyL = new String[2];
                                        xxyyL = geocodeStreetOpegieka(propertiesLabel, userOpegieka, passOpegieka);
                                        String xxL = xxyyL[0];
                                        String yyL = xxyyL[1];
                                        if (xxL.length() > 0 && yyL.length() > 0) {
                                            Double latit = Double.parseDouble(yyL);
                                            Double longit = Double.parseDouble(xxL);
                                            coordinatesLabel.add(latit);
                                            coordinatesLabel.add(longit);
                                            geometryLabel.setCoordinates(coordinatesLabel);
                                            geometryLabel.setType("Point");
                                            markerLabel.setGeometry(geometryLabel);
                                            labelsList_6m.add(markerLabel);
                                        }
                                    }
                                    break;
                                case "2m":
                                    boolean found_2m = false;
                                    for (markers.Marker markersRow : markersList_2m) {

                                        if (markersRow.getProperties().getMiasto().equals(marker.getProperties().getMiasto())
                                                && markersRow.getProperties().getUlica().equals(marker.getProperties().getUlica())
                                                && markersRow.getId_punktu().equals(marker.getId_punktu())
                                                && markersRow.getProperties().getNumerDomu().equals(marker.getProperties().getNumerDomu())
                                                && markersRow.getProperties().getStatus().equals(marker.getProperties().getStatus())) {

                                            found_2m = true;
                                            markersRow.getProperties().setIloscOdczytow(markersRow.getProperties().getIloscOdczytow() + 1);
                                        }
                                    }
                                    if (!found_2m) {
                                        properties.setIloscOdczytow(1);
                                        markersList_2m.add(marker);
                                    }

                                    boolean foundLabel_2m = false;
                                    for (markers.Marker markersRow : labelsList_2m) {

                                        if (markersRow.getId_punktu().equals(markerLabel.getId_punktu())
                                                && markersRow.getProperties().getMiasto().equals(markerLabel.getProperties().getMiasto())
                                                && markersRow.getProperties().getUlica().equals(markerLabel.getProperties().getUlica())
                                                && markersRow.getProperties().getStatus().equals(markerLabel.getProperties().getStatus())) {

                                            foundLabel_2m = true;
                                            markersRow.getProperties().setIloscOdczytow(markersRow.getProperties().getIloscOdczytow() + 1);
                                        }
                                    }

                                    if (!foundLabel_2m) {
                                        propertiesLabel.setIloscOdczytow(1);
                                        String[] xxyyL = new String[2];
                                        xxyyL = geocodeStreetOpegieka(propertiesLabel, userOpegieka, passOpegieka);
                                        String xxL = xxyyL[0];
                                        String yyL = xxyyL[1];
                                        if (xxL.length() > 0 && yyL.length() > 0) {
                                            Double latit = Double.parseDouble(yyL);
                                            Double longit = Double.parseDouble(xxL);
                                            coordinatesLabel.add(latit);
                                            coordinatesLabel.add(longit);
                                            geometryLabel.setCoordinates(coordinatesLabel);
                                            geometryLabel.setType("Point");
                                            markerLabel.setGeometry(geometryLabel);
                                            labelsList_2m.add(markerLabel);
                                        }
                                    }
                                    break;

                            }
                        } else {
                            markersList_errors.add(marker);
                        }

                        counter++;
                        updateProgress(counter, max);
                    }


//                    emarkersLabels.setMarkers(labelsList);
//                    emarkersMarkers.setMarkers(markersList);
//
//                    Gson gson = new Gson();
//                    String listaLabelsString = gson.toJson(emarkersLabels);
//                    String listaMarkersString = gson.toJson(emarkersMarkers);


                } catch (Exception e) {
                    e.printStackTrace();
//                    ErrorToast("ERROR:", e.toString());
                }


                String myDocuments;
                File dirMain;
                String fName;
//                File dirMain2;


                try {

                    Platform.runLater(
                            () -> {
                                setCurrentWork("3 z 3: generowanie plikow KML...");
                            }
                    );


                    if (!currentState) return null;

                    myDocuments = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();

                    dirMain = new File(myDocuments + "\\" + "EZ_KML-pliki");

                    if (!dirMain.exists()) {
                        dirMain.mkdir();
                    }

                    fName = file.getName().replaceFirst("[.][^.]+$", "");


//                    dirMain2 = new File(dirMain + "\\" + fName);
//
//                    if (!dirMain2.exists()) {
//                        dirMain2.mkdir();
//                    }
//                    mainFilesPath = dirMain2.toString() + "\\" + fName;

                    String filename = dirMain.toString() + "\\12m_" + fName + "__1.kml";


                    List<markers.Marker> labelsListTemp_12m = labelsList_12m.stream().collect(Collectors.toList());
                    List<markers.Marker> markersListTemp_12m = markersList_12m.stream().collect(Collectors.toList());

                    List<markers.Marker> labelsListTemp_6m = labelsList_6m.stream().collect(Collectors.toList());
                    List<markers.Marker> markersListTemp_6m = markersList_6m.stream().collect(Collectors.toList());

                    List<markers.Marker> labelsListTemp_2m = labelsList_2m.stream().collect(Collectors.toList());
                    List<markers.Marker> markersListTemp_2m = markersList_2m.stream().collect(Collectors.toList());

                    List<markers.Marker> markersListTemp_errors = markersList_errors.stream().collect(Collectors.toList());

                    Calendar currentCalendar = Calendar.getInstance();
                    String year = Integer.toString(currentCalendar.get(Calendar.YEAR));


                    File dirYear = new File(dirMain + "\\" + year);

                    if (!dirYear.exists()) {
                        dirYear.mkdir();
                    }


//                    Double sizes = (double) (markersListTemp_12m.size() + labelsListTemp_12m.size());
//
//                    Integer maxMarkers = 1500;
//                    Double sizesDz = sizes / maxMarkers;
//                    Double mL = Math.ceil(sizesDz);
//
//                    Integer nuberOfFiles = mL.intValue();

//                    System.out.println("Markerow: " + sizes + ",  plikow: " + nuberOfFiles + "(kontrolnie:  przed: " + labelsList.size() + "," + markersList.size() + " , po: "
//                            + labelsListTEMP.size() + ", " + markersListTEMP.size() + ")");

                    Integer licznikM;

//                    for (int fileNr = 1; fileNr <= nuberOfFiles; fileNr++) {
                    licznikM = 0;
                    // System.out.println(fileNr);
//                        String nazwaPliku_12m = "12m_" + RandomStringUtils.randomAlphanumeric(4).toUpperCase() + "_nr_" + fileNr;
//                        String nazwaPliku_6m = "6m_" + RandomStringUtils.randomAlphanumeric(4).toUpperCase() + "_nr_" + fileNr;
//                        String nazwaPliku_2m = "2m_" + RandomStringUtils.randomAlphanumeric(4).toUpperCase() + "_nr_" + fileNr;

                    String nazwaPliku_12m = "12m_" + fName;
                    String nazwaPliku_6m = "6m_" + fName;
                    String nazwaPliku_2m = "2m_" + fName;
                    String nazwaPliku_errors = "Errors_" + fName;


                    String destination_12m = dirYear + "\\" + nazwaPliku_12m + ".kml";
                    String destination_6m = dirYear + "\\" + nazwaPliku_6m + ".kml";
                    String destination_2m = dirYear + "\\" + nazwaPliku_2m + ".kml";
                    String destination_errors = dirYear + "\\" + nazwaPliku_errors + ".kml";

                    String nazwaWarstwy_12m = "12m";
                    String nazwaWarstwy_6m = "6m";
                    String nazwaWarstwy_2m = "2m";
                    String nazwaWarstwy_errors = "errors";

                    Kml kml_12m = KmlFactory.createKml();
                    Kml kml_6m = KmlFactory.createKml();
                    Kml kml_2m = KmlFactory.createKml();
                    Kml kml_errors = KmlFactory.createKml();
                    Document doc_12m = kml_12m.createAndSetDocument().withName("WARSTWY").withOpen(true);
                    Document doc_6m = kml_6m.createAndSetDocument().withName("WARSTWY").withOpen(true);
                    Document doc_2m = kml_2m.createAndSetDocument().withName("WARSTWY").withOpen(true);
                    Document doc_errors = kml_errors.createAndSetDocument().withName("WARSTWY").withOpen(true);

                    // create a Folder

                    Folder folderImport_12m = doc_12m.createAndAddFolder();
                    Folder folderImport_6m = doc_6m.createAndAddFolder();
                    Folder folderImport_2m = doc_2m.createAndAddFolder();
                    Folder folderImport_errors = doc_errors.createAndAddFolder();

                    folderImport_12m.withName(nazwaPliku_12m).withOpen(true);
                    folderImport_6m.withName(nazwaPliku_6m).withOpen(true);
                    folderImport_2m.withName(nazwaPliku_2m).withOpen(true);
                    folderImport_errors.withName(nazwaPliku_errors).withOpen(true);

//                        String nazwaWarstwyLabels_12m = nazwaWarstwy_12m.trim().substring(0, nazwaWarstwy_12m.length() - 4) + " - ulice (" + fileNr + "/" + nuberOfFiles + ")";

                    Folder folderLabels_12m = folderImport_12m.createAndAddFolder();
                    Folder folderLabels_6m = folderImport_6m.createAndAddFolder();
                    Folder folderLabels_2m = folderImport_2m.createAndAddFolder();

                    folderLabels_12m.withName(nazwaWarstwy_12m + " ULICE").withOpen(true);
                    folderLabels_6m.withName(nazwaWarstwy_6m + " ULICE").withOpen(true);
                    folderLabels_2m.withName(nazwaWarstwy_2m + " ULICE").withOpen(true);

//                        String nazwaWarstwyMarkers_12m = nazwaWarstwy_12m.trim().substring(0, nazwaWarstwy_12m.length() - 4) + " - domy (" + fileNr + "/" + nuberOfFiles + ")";
                    Folder folderMarkers_12m = folderImport_12m.createAndAddFolder();
                    Folder folderMarkers_6m = folderImport_6m.createAndAddFolder();
                    Folder folderMarkers_2m = folderImport_2m.createAndAddFolder();
                    Folder folderMarkers_errors = folderImport_errors.createAndAddFolder();

                    folderMarkers_12m.withName(nazwaWarstwy_12m + " DOMY").withOpen(true);
                    folderMarkers_6m.withName(nazwaWarstwy_6m + " DOMY").withOpen(true);
                    folderMarkers_2m.withName(nazwaWarstwy_2m + " DOMY").withOpen(true);
                    folderMarkers_errors.withName(nazwaWarstwy_errors).withOpen(true);

//                    System.out.println("Rozmiary: " + markersList_12m.size() + ", " + markersList_6m.size() + ", " + markersList_2m.size());


                    Iterator<markers.Marker> iterL = labelsListTemp_12m.iterator();
                    while (iterL.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);

                        markers.Marker markersRow = iterL.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica();
                        iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();

                        createStreet(doc_12m, folderLabels_12m, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterL.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    Iterator<markers.Marker> iterM = markersListTemp_12m.iterator();
                    while (iterM.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);
                        markers.Marker markersRow = iterM.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica() + " " + markersRow.getProperties().getNumerDomu();
                        iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();

                        createPlacemark(doc_12m, folderMarkers_12m, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterM.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    iterL = labelsListTemp_6m.iterator();
                    while (iterL.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);

                        markers.Marker markersRow = iterL.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica();
                        iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();

                        createStreet(doc_6m, folderLabels_6m, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterL.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    iterM = markersListTemp_6m.iterator();
                    while (iterM.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);
                        markers.Marker markersRow = iterM.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica() + " " + markersRow.getProperties().getNumerDomu();
                        iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();

                        createPlacemark(doc_6m, folderMarkers_6m, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterM.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    iterL = labelsListTemp_2m.iterator();
                    while (iterL.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);

                        markers.Marker markersRow = iterL.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica();
                        iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();

                        createStreet(doc_2m, folderLabels_2m, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterL.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    iterM = markersListTemp_2m.iterator();
                    while (iterM.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);
                        markers.Marker markersRow = iterM.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica() + " " + markersRow.getProperties().getNumerDomu();
                        iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();

                        createPlacemark(doc_2m, folderMarkers_2m, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterM.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    iterM = markersListTemp_errors.iterator();
                    while (iterM.hasNext()) {
                        licznikM++;
                        // System.out.println(licznikM);
                        markers.Marker markersRow = iterM.next();

                        double longitude;
                        double latitude;
                        String markerLabel;
                        String miasto;
                        String iloscOdczytow;

                        longitude = markersRow.getGeometry().getCoordinates().get(1);
                        latitude = markersRow.getGeometry().getCoordinates().get(0);

                        miasto = markersRow.getProperties().getMiasto();
                        markerLabel = "ul. " + markersRow.getProperties().getUlica() + " " + markersRow.getProperties().getNumerDomu();
                        iloscOdczytow = "error";

                        createPlacemark(doc_errors, folderMarkers_errors, longitude, latitude, miasto, markerLabel, iloscOdczytow);

                        iterM.remove();

//                        if (licznikM == maxMarkers) {
//                            break;
//                        }
                    }

                    // marshals to console
                    // kml.marshal();

                    // print and save
                    try {
                        Marshaller marshaller = JAXBContext.newInstance(new Class[]{Kml.class}).createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
                            @Override
                            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                                return namespaceUri.matches("http://www.w3.org/\\d{4}/Atom") ? "atom"
                                        : (namespaceUri.matches("urn:oasis:names:tc:ciq:xsdschema:xAL:.*?") ? "xal"
                                        : (namespaceUri.matches("http://www.google.com/kml/ext/.*?") ? "gx" : (namespaceUri.matches("http://www.opengis.net/kml/.*?") ? "" : (null))));
                            }
                        });
                        File file_12m = new File(destination_12m);
                        File file_6m = new File(destination_6m);
                        File file_2m = new File(destination_2m);
                        File file_errors = new File(destination_errors);
                        marshaller.marshal(kml_12m, file_12m);
                        marshaller.marshal(kml_6m, file_6m);
                        marshaller.marshal(kml_2m, file_2m);
                        marshaller.marshal(kml_errors, file_errors);
                        // kml.marshal();
                    } catch (JAXBException e) {
//                        ErrorToast("ERROR:", e.toString());
                        e.printStackTrace();
                    }

//                    }

                    tEnd = System.currentTimeMillis();

                } catch (Exception e) {
                    e.printStackTrace();
//                    ErrorToast("ERROR:", e.toString());
                } finally {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            long tDelta = tEnd - tStart;
                            double elapsedSeconds = tDelta / 1000.0;
//                            System.out.println("ENERGO System: >>> Eksport poprawny w czasie:  " + elapsedSeconds);
//                            toastStage = (Stage) spinner.getScene().getWindow();
//                            SucssesToast("Wyeksportowano", "ilosc plikow: " + nuberOfFiles);
//                            spinner.setVisible(false);
                        }
                    });
                }


                Platform.runLater(
                        () -> {
                            setCurrentWork("");
                        }
                );
                return null;
            }


        };
    }

    private String[] geocodeOpegieka(Properties properties, String user, String pass) {

        String[] xxyy = new String[2];
        try {
            HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            JsonFactory JSON_FACTORY = new JacksonFactory();

            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            });
            String address = "";

            address = properties.getUlica() + "+" + properties.getNumerDomu() + "+" + properties.getMiasto();

            // String address = properties.getNumerDomu() + properties.getNumerDomuChars() +
            // "+" + properties.getUlica() + "," + properties.getMiasto() + ",pl," +
            // properties.getKodPocztowy();

//					System.out.println("address  >>>>>>>>>>>>" + address);
            GenericUrl genericUrl = new GenericUrl("http://gc.giscenter.pl/search?format=json&q=" + address);

            HttpRequest request = requestFactory.buildGetRequest(genericUrl);

            HttpHeaders headers = new HttpHeaders();

            //// API KEY
            // headers.put("x-auth", apikey);

            ///// BASIC: USER + PASSWORD
            String authString = user + ":" + pass;
            String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
            headers.setAuthorization("Basic " + encodedAuth);

            request.setHeaders(headers);

            // com.google.api.client.http.HttpResponse response = null;

            String response = null;
            String xx = "0.0";
            String yy = "0.0";
            try {
                response = request.execute().parseAsString();
//                System.out.println("RESPONSE  >>>>>>>>>>>>" + response);
                Gson gson = new Gson();
                markers.MLR[] mlrList = gson.fromJson(response, markers.MLR[].class);

                if (response != null && mlrList.length > 0) {
                    xx = mlrList[0].getLon();
                    yy = mlrList[0].getLat();
                } else {
                    String[] xxyy_g = geocodeGoogle(properties, address, context);

                    if (xxyy_g.length > 0) {
                        xx = xxyy_g[0];
                        yy = xxyy_g[1];
                    }

                }
            } catch (Exception e) {
                System.err.println("ERROR RESPONSE  >>>>>>>>>>>>" + response);
//                ErrorToast("ERROR:", e.toString());
                e.printStackTrace();
            } finally {
                if (response != null) {
                    // response.disconnect();
                }
            }

            if (points.containsKey(xx + yy)) {
                Double xxNew = Double.parseDouble(xx);
                Double yyNew = Double.parseDouble(yy);

                Double random = ThreadLocalRandom.current().nextDouble(150, 200);
                random = random / 100000;

                random = round(random, 6);

                xxNew = xxNew + random;
                yyNew = yyNew + random;

                xx = xxNew.toString();
                yy = yyNew.toString();
                points.put(xx + yy, "");

            } else {
                points.put(xx + yy, "");
            }

            xxyy[0] = xx;
            xxyy[1] = yy;
//            System.out.println("XXYY  >>>>>>>>>>>>" + xx + "," + yy);
        } catch (IOException e) {
//            ErrorToast("ERROR:", e.toString());
            e.printStackTrace();
        }
        return xxyy;
    }


    private String[] geocodeStreetOpegieka(Properties properties, String user, String pass) {

        String[] xxyy = new String[2];
        try {
            HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            JsonFactory JSON_FACTORY = new JacksonFactory();

            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            });

            String address = "";
            if (properties.getUlica() != null) {
                address = properties.getUlica() + "+" + properties.getMiasto();
            } else {
                address = properties.getMiasto();
            }

//            System.out.println("address  >>>>>>>>>>>>" + address);

            GenericUrl genericUrl = new GenericUrl("http://gc.giscenter.pl/search?format=json&q=" + address);

            HttpRequest request = requestFactory.buildGetRequest(genericUrl);

            HttpHeaders headers = new HttpHeaders();

            //// API KEY
            // headers.put("x-auth", apikey);

            ///// BASIC: USER + PASSWORD
            String authString = user + ":" + pass;
            String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
            headers.setAuthorization("Basic " + encodedAuth);

            request.setHeaders(headers);

            // com.google.api.client.http.HttpResponse response = null;
            String response = null;
            String xx = "0.0";
            String yy = "0.0";
            try {
                response = request.execute().parseAsString();
//						System.out.println("RESPONSE  >>>>>>>>>>>>" + response);
                Gson gson = new Gson();
                markers.MLR[] mlrList = gson.fromJson(response, markers.MLR[].class);

                if (response != null && mlrList.length > 0) {
                    xx = mlrList[0].getLon();
                    yy = mlrList[0].getLat();
                } else {
                    String[] xxyy_g = geocodeGoogle(properties, address, context);

                    if (xxyy_g.length > 0) {
                        xx = xxyy_g[0];
                        yy = xxyy_g[1];
                    }

                }
            } catch (Exception e) {
//						Systemsystem.err.println("ERROR RESPONSE  >>>>>>>>>>>>" + response);
//                ErrorToast("ERROR:", e.toString());
                e.printStackTrace();
            } finally {
                if (response != null) {
                    // response.disconnect();
                }
            }

            if (points.containsKey(xx + yy)) {
                Double xxNew = Double.parseDouble(xx);
                Double yyNew = Double.parseDouble(yy);

                Double random = ThreadLocalRandom.current().nextDouble(150, 200);
                random = random / 100000;

                random = round(random, 6);

                xxNew = xxNew + random;
                yyNew = yyNew + random;

                xx = xxNew.toString();
                yy = yyNew.toString();
                points.put(xx + yy, "");

            } else {
                points.put(xx + yy, "");
            }

            xxyy[0] = xx;
            xxyy[1] = yy;

        } catch (IOException e) {
//            ErrorToast("ERROR:", e.toString());
            e.printStackTrace();
        }
        return xxyy;
    }

    private String[] geocodeGoogle(Properties properties, String address, GeoApiContext context) {
        String[] xxyy = new String[2];
        Double xx = 0.0;
        Double yy = 0.0;

        try {

            GeocodingResult[] results;

            results = GeocodingApi.geocode(context, address).await();

            xx = results[0].geometry.location.lng;
            yy = results[0].geometry.location.lat;

        } catch (Exception e) {
//            ErrorToast("ERROR:", e.toString());
            e.printStackTrace();
        }

        xxyy[0] = xx.toString();
        xxyy[1] = yy.toString();
        return xxyy;
    }

    private static void createPlacemark(Document document, Folder folder, double longitude, double latitude, String miasto, String markerName, String iloscOdczytow) {

//        Icon icon = new Icon().withHref("https://chart.googleapis.com/chart?chst=d_bubble_icon_text_big&chld=homegardenbusiness|bb|" + markerName + "|FFFFFF|000000");
//        Style style = document.createAndAddStyle();
//        style.withId("style_" + markerName) // set the stylename to use this style from the placemark
//                .createAndSetIconStyle().withScale(5.0).withIcon(icon); // set size and icon
//        style.createAndSetLabelStyle().withColor("ff43b3ff").withScale(5.0); // set color and size of the name

        Placemark placemark = folder.createAndAddPlacemark();
        placemark.withName(markerName).withStyleUrl("#style_" + markerName)
                // .withDescription("<![CDATA[<h1>" + markerName + "</h1>\r\n<p><font
                // color=\"red\">odczyty: " + iloscOdczytow + "\r\n<b>wykonane: </b>" +
                // iloscWykonanych + "</font></p>")
                .withDescription("<![CDATA[<h1>" + miasto + "</h1><p><font color='red'>odczyty: " + iloscOdczytow)

                // coordinates and distance (zoom level) of the viewer
                .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(0).withRange(12000000);

        placemark.createAndSetPoint().addToCoordinates(longitude, latitude);
    }

    private static void createStreet(Document document, Folder folder, double longitude, double latitude, String miasto, String markerName, String iloscOdczytow) {

//        Icon icon = new Icon().withHref("https://chart.googleapis.com/chart?chst=d_bubble_icon_text_big&chld=homegardenbusiness|bb|" + markerName + "|FFFFFF|000000");
//        Style style = document.createAndAddStyle();
//        style.withId("style_" + markerName) // set the stylename to use this style from the placemark
//                .createAndSetIconStyle().withScale(5.0).withIcon(icon); // set size and icon
//        style.createAndSetLabelStyle().withColor("ff43b3ff").withScale(5.0); // set color and size of the name

        Placemark placemark = folder.createAndAddPlacemark();
        placemark.withName(markerName).withStyleUrl("#style_" + markerName)
                // .withDescription("<![CDATA[<h1>" + markerName + "</h1>\r\n<p><font
                // color=\"red\">odczyty: " + iloscOdczytow + "\r\n<b>wykonane: </b>" +
                // iloscWykonanych + "</font></p>")
                .withDescription("<![CDATA[<h1>" + miasto + "</h1><p><font color='red'>odczyty: " + iloscOdczytow)

                // coordinates and distance (zoom level) of the viewer
                .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(0).withRange(12000000);

        placemark.createAndSetPoint().addToCoordinates(longitude, latitude);
    }

    public void countGroups() {

        miastaMap = new HashMap<>();
        uliceMap = new HashMap<>();
        domyMap = new HashMap<>();

        listOdczyty.forEach((temp) -> {

            String nr_domu = temp.ulica.toString() + temp.budynek.toString();

            Integer countMiasta = miastaMap.get(temp.miejscowosc.toString());
            Integer countUlice = uliceMap.get(temp.ulica.toString());
            Integer countDomy = domyMap.get(nr_domu);
            // System.out.println("nr_domu: "+nr_domu+", "+countDomy);

            miastaMap.put(temp.miejscowosc.toString(), (countMiasta == null) ? 1 : countMiasta + 1);
            uliceMap.put(temp.ulica.toString(), (countUlice == null) ? 1 : countUlice + 1);
            domyMap.put(nr_domu, (countDomy == null) ? 1 : countDomy + 1);

        });

        iloscMiast = miastaMap.size();
        iloscUlic = uliceMap.size();
        iloscDomow = domyMap.size();

    }

    public static double round(double value, int places) {
        if (places < 0) {
//            ErrorToast("ERROR:", "IllegalArgument exception");
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Boolean getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Boolean stateNew) {
        this.currentState = stateNew;
    }

//    public void setFiles(List<File> files) {
//        this.files = files;
//    }

//    public void setPointsCount(long pointsCount) {
//        this.pointsCount = pointsCount;
//    }

//    public void setFilesCount(long filesCount) {
//        this.filesCount = filesCount;
//    }


    public ObjectProperty<String> currentWorkProperty() {
        return currentWork;
    }

    public final String getCurrentWork() {
        return currentWorkProperty().get();
    }

    public final void setCurrentWork(String currentWork) {
        currentWorkProperty().set(currentWork);
    }


    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ObservableList<RowPlikCSV_Map> getListOdczyty() {
        return listOdczyty;
    }

    public void setListOdczyty(ObservableList<RowPlikCSV_Map> listOdczyty) {
        this.listOdczyty = listOdczyty;
    }

    public static Stage getToastStage() {
        return toastStage;
    }

    public void setToastStage(Stage toastStage) {
        this.toastStage = toastStage;
    }


    static public Tooltip NoticeToast(String Title, String Txt) {
        return JToast(Title, Txt, "rgba(255,255,255,0.8)", "#222", Duration.seconds(7));
    }

    static public Tooltip SucssesToast(String Title, String Txt) {
        return JToast(Title, Txt, "rgba(15, 157, 88, 0.8)", "#222", Duration.seconds(7));
    }

    static public Tooltip ErrorToast(String Title, String Txt) {
        return JToast(Title, Txt, "rgba(255, 25, 36, 0.8)", "#fff", Duration.seconds(15));
    }

    public static Tooltip JToast(String Title, String Text, String BackgroundColor, String Color, Duration duration) {
        Tooltip S = new Tooltip();
        Label X = new Label("X");
        X.setId("Close");
        X.setOnMouseClicked(value -> {
            S.hide();
        });

        Label Content = new Label(Text);
        Content.setAlignment(Pos.TOP_RIGHT);
        Content.setTextAlignment(TextAlignment.RIGHT);
        Content.setWrapText(true);
        Content.setId("Content");

        double MaxWidth = 800;
        Content.setMaxWidth(MaxWidth);

        Label TitleLable = new Label(Title);
        TitleLable.setAlignment(Pos.TOP_RIGHT);
        Content.setStyle("-fx-text-fill:" + Color);
        TitleLable.setStyle("-fx-text-fill:" + Color);

        GridPane GB = new GridPane();
        double Width = 0;
        if (Content.getText() != null) {
            Width = 150 + Content.getText().length() / 2.5;
        }
        if (Width > MaxWidth) {
            Width = MaxWidth;
        }
        double height = Content.getPrefHeight();

        GB.getColumnConstraints().setAll(new ColumnConstraints(30, 30, 30, Priority.NEVER, HPos.LEFT, true), new ColumnConstraints(Width, Width, MaxWidth, Priority.ALWAYS, HPos.RIGHT, true));

        GB.getRowConstraints().setAll(new RowConstraints(30, 30, 30), new RowConstraints(height, height, 750, Priority.ALWAYS, VPos.CENTER, true));
        GB.setId("msgtipbox");
        GB.setStyle("-fx-background-color:" + BackgroundColor);
        GB.setVgap(10);
        GB.setHgap(10);
        GB.add(X, 0, 0);
        GB.add(TitleLable, 1, 0);
        GB.add(Content, 0, 1, 2, 1);
        GridPane.setVgrow(Content, Priority.ALWAYS);
        GridPane.setHgrow(Content, Priority.ALWAYS);
        S.setGraphic(GB);
        S.setId("msg-tip");
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double y = screenBounds.getMinY() + screenBounds.getHeight();
        S.show(getToastStage(), 0, y);

        ////// Show and Hide
        ////// Still showing in Hover Support
        SimpleBooleanProperty HoveProperty = new SimpleBooleanProperty(false);
        GB.setOnMouseEntered(v -> HoveProperty.set(true));
        GB.setOnMouseExited(v -> HoveProperty.set(false));
        PauseTransition wait = new PauseTransition(duration);
        wait.setOnFinished((e) -> {
            if (HoveProperty.get()) {
                wait.play();
            } else {
                S.hide();
            }
        });
        wait.play();

        HoveProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            wait.playFromStart();
        });
        return S;
    }


}
