import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
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
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import markers.EMarkers;
import markers.Marker;
import markers.Properties;
import org.apache.commons.lang3.RandomStringUtils;

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
                                setCurrentWork("1 z 4: dekodowanie pliku...");
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

                        counter++;
                        updateProgress(counter, max);
                    }

                    //} end for files


                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("2 z 4: dzielenie na warstwy...");
                            }
                    );


                } catch (Exception e) {
                    e.printStackTrace();
                }

                counter = 0;

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("3 z 4: geomapowanie adresow...");
                            }
                    );


                    EMarkers emarkersLabels = new EMarkers();
                    emarkersLabels.setType("LabelsCollection");
                    List<markers.Marker> labelsList = new ArrayList<markers.Marker>();

                    EMarkers emarkersMarkers = new EMarkers();
                    emarkersMarkers.setType("MarkersCollection");
                    List<markers.Marker> markersList = new ArrayList<markers.Marker>();

                    context = new GeoApiContext.Builder().apiKey("AIzaSyDJBzpwv6y4wRCsXQv3gUBZr1J9PY3Xq5g").build();

                    String userOpegieka = "gcjp01";
                    String passOpegieka = "n9bDRzu2et2yP7cS";

                    countGroups();


                    for (RowPlikCSV_Map row : listOdczyty) {

                        if (!currentState) {
                            return null;
                        }

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

                        coordinates.add(latitude);
                        coordinates.add(longitude);

                        geometry.setCoordinates(coordinates);
                        geometry.setType("Point");

                        marker.setProperties(properties);
                        marker.setGeometry(geometry);
                        marker.setType("Marker");

                        markerLabel.setProperties(propertiesLabel);
                        markerLabel.setType("Marker");

                        boolean found = false;

                        for (markers.Marker markersRow : markersList) {

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
                            markersList.add(marker);

                        }

                        boolean foundLabel = false;
                        for (markers.Marker markersRow : labelsList) {

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

                                labelsList.add(markerLabel);
                            }

                        }

                        counter++;
                        updateProgress(counter, max);
                    }


                    emarkersLabels.setMarkers(labelsList);
                    emarkersMarkers.setMarkers(markersList);

                    Gson gson = new Gson();
                    String listaLabelsString = gson.toJson(emarkersLabels);
                    String listaMarkersString = gson.toJson(emarkersMarkers);


                } catch (Exception e) {
                    e.printStackTrace();
                }


                String myDocuments;
                File dirMain;
                String fName;
//                File dirMain2;


                try {

                    Platform.runLater(
                            () -> {
                                setCurrentWork("4 z 4: generowanie plikow KML...");
                            }
                    );


//                    if (!currentState) return null;
//
//                    myDocuments = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
//
//                    dirMain = new File(myDocuments + "\\" + "EZ_KML-pliki");
//
//                    if (!dirMain.exists()) {
//                        dirMain.mkdir();
//                    }
//
//                    fName = file.getName().replaceFirst("[.][^.]+$", "");
//
//
////                    dirMain2 = new File(dirMain + "\\" + fName);
////
////                    if (!dirMain2.exists()) {
////                        dirMain2.mkdir();
////                    }
////                    mainFilesPath = dirMain2.toString() + "\\" + fName;
//
//                    String filename = dirMain.toString() + "\\" + fName + "__1.kml";
//
//
//                    List<markers.Marker> labelsListTEMP = labelsListMAIN.stream().collect(Collectors.toList());
//                    List<markers.Marker> markersListTEMP = markersListMAIN.stream().collect(Collectors.toList());
//
//                    Calendar currentCalendar = Calendar.getInstance();
//                    String year = Integer.toString(currentCalendar.get(Calendar.YEAR));
//                    // String month = currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG,
//                    // Locale.getDefault());
//                    String month = new SimpleDateFormat("MMMM").format(currentCalendar.getTime());
//
//                    File dirYear = new File(dirMain.getName() + "\\" + year);
//
//                    if (!dirYear.exists()) {
//                        dirYear.mkdir();
//                    }
//
//                    File dirMonth = new File(dirMain.getName() + "\\" + dirYear.getName() + "\\" + month);
//
//                    if (!dirMonth.exists()) {
//                        dirMonth.mkdir();
//                    }
//
//
//                    Double sizes = (double) (markersListTEMP.size() + labelsListTEMP.size());
//
//                    Integer maxMarkers = 1500;
//                    Double sizesDz = sizes / maxMarkers;
//                    Double mL = Math.ceil(sizesDz);
//
//                    Integer nuberOfFiles = mL.intValue();
//
//                    System.out.println("Markerow: " + sizes + ",  plikow: " + nuberOfFiles + "(kontrolnie:  przed: " + labelsListMAIN.size() + "," + markersListMAIN.size() + " , po: "
//                            + labelsListTEMP.size() + ", " + markersListTEMP.size() + ")");
//
//                    Integer licznikM;
//                    for (int fileNr = 1; fileNr <= nuberOfFiles; fileNr++) {
//                        licznikM = 0;
//                        // System.out.println(fileNr);
//                        String nazwaPliku = "ODCZYTY_" + RandomStringUtils.randomAlphanumeric(4).toUpperCase() + "_nr_" + fileNr;
//
//                        if (context.getRegisteredObject("NazwaPaczki") != null) {
//                            String nazwaWarstwy = (String) context.getRegisteredObject("NazwaPaczki");
//                            nazwaPliku = (String) context.getRegisteredObject("NazwaPaczki") + "_nr_" + fileNr;
//                            if (context.getRegisteredObject("InkasentPaczki") != null) {
//                                String nazwaInkasenta = (String) context.getRegisteredObject("InkasentPaczki");
//                                nazwaPliku = nazwaInkasenta.trim() + "_" + nazwaPliku;
//                            }
//                        }
//
//                        String destination = dirMain.getName() + "\\" + dirYear.getName() + "\\" + dirMonth.getName() + "\\" + nazwaPliku + ".kml";
//
//                        Kml kml = KmlFactory.createKml();
//                        Document doc = kml.createAndSetDocument().withName("WARSTWY").withOpen(true);
//
//                        // create a Folder
//
//                        Folder folderImport = doc.createAndAddFolder();
//                        folderImport.withName(nazwaPliku).withOpen(true);
//
//                        String nazwaWarstwyLabels = nazwaWarstwy.trim().substring(0, nazwaWarstwy.length() - 4) + " - ulice (" + fileNr + "/" + nuberOfFiles + ")";
//                        Folder folderLabels = folderImport.createAndAddFolder();
//                        folderLabels.withName(nazwaWarstwyLabels).withOpen(true);
//
//                        String nazwaWarstwyMarkers = nazwaWarstwy.trim().substring(0, nazwaWarstwy.length() - 4) + " - domy (" + fileNr + "/" + nuberOfFiles + ")";
//                        Folder folderMarkers = folderImport.createAndAddFolder();
//                        folderMarkers.withName(nazwaWarstwyMarkers).withOpen(true);
//
//
//                        Iterator<markers.Marker> iterL = labelsListTEMP.iterator();
//                        while (iterL.hasNext()) {
//                            licznikM++;
//                            // System.out.println(licznikM);
//
//                            markers.Marker markersRow = iterL.next();
//
//                            double longitude;
//                            double latitude;
//                            String markerLabel;
//                            String miasto;
//                            String iloscOdczytow;
//                            String iloscWykonanych;
//
//                            longitude = markersRow.getGeometry().getCoordinates().get(1);
//                            latitude = markersRow.getGeometry().getCoordinates().get(0);
//
//                            miasto = markersRow.getProperties().getMiasto();
//                            markerLabel = "ul. " + markersRow.getProperties().getUlica();
//                            iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();
//                            iloscWykonanych = markersRow.getProperties().getIloscOdczytowWykonanych().toString();
//
//                            createStreet(doc, folderLabels, longitude, latitude, miasto, markerLabel, iloscOdczytow, iloscWykonanych);
//
//                            iterL.remove();
//
//                            if (licznikM == maxMarkers) {
//                                break;
//                            }
//                        }
//
//                        Iterator<markers.Marker> iterM = markersListTEMP.iterator();
//                        while (iterM.hasNext()) {
//                            licznikM++;
//                            // System.out.println(licznikM);
//                            markers.Marker markersRow = iterM.next();
//
//                            double longitude;
//                            double latitude;
//                            String markerLabel;
//                            String miasto;
//                            String iloscOdczytow;
//                            String iloscWykonanych;
//
//                            longitude = markersRow.getGeometry().getCoordinates().get(1);
//                            latitude = markersRow.getGeometry().getCoordinates().get(0);
//
//                            miasto = markersRow.getProperties().getMiasto();
//                            markerLabel = "ul. " + markersRow.getProperties().getUlica() + " " + markersRow.getProperties().getNumerDomu() + markersRow.getProperties().getNumerDomuChars();
//                            iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();
//                            iloscWykonanych = markersRow.getProperties().getIloscOdczytowWykonanych().toString();
//
//                            createPlacemark(doc, folderMarkers, longitude, latitude, miasto, markerLabel, iloscOdczytow, iloscWykonanych);
//
//                            iterM.remove();
//
//                            if (licznikM == maxMarkers) {
//                                break;
//                            }
//                        }
//
//                        // marshals to console
//                        // kml.marshal();
//
//                        // print and save
//                        try {
//                            Marshaller marshaller = JAXBContext.newInstance(new Class[]{Kml.class}).createMarshaller();
//                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//                            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
//                                @Override
//                                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//                                    return namespaceUri.matches("http://www.w3.org/\\d{4}/Atom") ? "atom"
//                                            : (namespaceUri.matches("urn:oasis:names:tc:ciq:xsdschema:xAL:.*?") ? "xal"
//                                            : (namespaceUri.matches("http://www.google.com/kml/ext/.*?") ? "gx" : (namespaceUri.matches("http://www.opengis.net/kml/.*?") ? "" : (null))));
//                                }
//                            });
//                            File file = new File(destination);
//                            marshaller.marshal(kml, file);
//                            // kml.marshal();
//                        } catch (JAXBException e) {
//
//                            e.printStackTrace();
//                        }
//
//                    }

                    tEnd = System.currentTimeMillis();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            long tDelta = tEnd - tStart;
                            double elapsedSeconds = tDelta / 1000.0;
                            System.out.println("ENERGO System: >>> Eksport poprawny w czasie:  " + elapsedSeconds);
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
            e.printStackTrace();
        }

        xxyy[0] = xx.toString();
        xxyy[1] = yy.toString();
        return xxyy;
    }

    private static void createPlacemark(Document document, Folder folder, double longitude, double latitude, String miasto, String markerName, String iloscOdczytow, String iloscWykonanych) {

        Icon icon = new Icon().withHref("https://chart.googleapis.com/chart?chst=d_bubble_icon_text_big&chld=homegardenbusiness|bb|" + markerName + "|FFFFFF|000000");
        Style style = document.createAndAddStyle();
        style.withId("style_" + markerName) // set the stylename to use this style from the placemark
                .createAndSetIconStyle().withScale(5.0).withIcon(icon); // set size and icon
        style.createAndSetLabelStyle().withColor("ff43b3ff").withScale(5.0); // set color and size of the name

        Placemark placemark = folder.createAndAddPlacemark();
        placemark.withName(markerName).withStyleUrl("#style_" + markerName)
                // .withDescription("<![CDATA[<h1>" + markerName + "</h1>\r\n<p><font
                // color=\"red\">odczyty: " + iloscOdczytow + "\r\n<b>wykonane: </b>" +
                // iloscWykonanych + "</font></p>")
                .withDescription("<![CDATA[<h1>" + miasto + "</h1><p><font color=\"red\">odczyty: " + iloscOdczytow + "\r\n<b>wykonane: </b>" + iloscWykonanych + "</font></p>")

                // coordinates and distance (zoom level) of the viewer
                .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(0).withRange(12000000);

        placemark.createAndSetPoint().addToCoordinates(longitude, latitude);
    }

    private static void createStreet(Document document, Folder folder, double longitude, double latitude, String miasto, String markerName, String iloscOdczytow, String iloscWykonanych) {

        Icon icon = new Icon().withHref("https://chart.googleapis.com/chart?chst=d_bubble_icon_text_big&chld=homegardenbusiness|bb|" + markerName + "|FFFFFF|000000");
        Style style = document.createAndAddStyle();
        style.withId("style_" + markerName) // set the stylename to use this style from the placemark
                .createAndSetIconStyle().withScale(5.0).withIcon(icon); // set size and icon
        style.createAndSetLabelStyle().withColor("ff43b3ff").withScale(5.0); // set color and size of the name

        Placemark placemark = folder.createAndAddPlacemark();
        placemark.withName(markerName).withStyleUrl("#style_" + markerName)
                // .withDescription("<![CDATA[<h1>" + markerName + "</h1>\r\n<p><font
                // color=\"red\">odczyty: " + iloscOdczytow + "\r\n<b>wykonane: </b>" +
                // iloscWykonanych + "</font></p>")
                .withDescription("<![CDATA[<h1>" + miasto + "</h1><p><font color=\"red\">odczyty: " + iloscOdczytow + "\r\n<b>wykonane: </b>" + iloscWykonanych + "</font></p>")

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
        if (places < 0)
            throw new IllegalArgumentException();

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
}
