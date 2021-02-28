import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
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
    private long pointsCount;

    private int counter = 0;
    private long max = 0;

    long tStart;
    long tEnd;

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
                max = 0;
                currentState = true;
                tStart = System.currentTimeMillis();
                try {

//                    for (File file : files) {

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String row;

                        Platform.runLater(
                                () -> {
                                    setCurrentWork("1 z 3: dekodowanie plikow...");
                                }
                        );

                        while ((row = bufferedReader.readLine()) != null) {
                            if (!currentState) return null;



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
                                setCurrentWork("2 z 3: dzielenie na warstwy ...");
                            }
                    );




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

                    String filename = dirMain.toString() + "\\" + fName + "__1.kml";




                    List<markers.Marker> labelsListTEMP = labelsListMAIN.stream().collect(Collectors.toList());
                    List<markers.Marker> markersListTEMP = markersListMAIN.stream().collect(Collectors.toList());

                    Calendar currentCalendar = Calendar.getInstance();
                    String year = Integer.toString(currentCalendar.get(Calendar.YEAR));
                    // String month = currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG,
                    // Locale.getDefault());
                    String month = new SimpleDateFormat("MMMM").format(currentCalendar.getTime());

                    File dirYear = new File(dirMain.getName() + "\\" + year);

                    if (!dirYear.exists()) {
                        dirYear.mkdir();
                    }

                    File dirMonth = new File(dirMain.getName() + "\\" + dirYear.getName() + "\\" + month);

                    if (!dirMonth.exists()) {
                        dirMonth.mkdir();
                    }


                    Double sizes = (double) (markersListTEMP.size() + labelsListTEMP.size());

                    Integer maxMarkers = 1500;
                    Double sizesDz = sizes / maxMarkers;
                    Double mL = Math.ceil(sizesDz);

                    Integer nuberOfFiles = mL.intValue();

                    System.out.println("Markerow: " + sizes + ",  plikow: " + nuberOfFiles + "(kontrolnie:  przed: " + labelsListMAIN.size() + "," + markersListMAIN.size() + " , po: "
                            + labelsListTEMP.size() + ", " + markersListTEMP.size() + ")");

                    Integer licznikM;
                    for (int fileNr = 1; fileNr <= nuberOfFiles; fileNr++) {
                        licznikM = 0;
                        // System.out.println(fileNr);
                        String nazwaPliku = "ODCZYTY_" + RandomStringUtils.randomAlphanumeric(4).toUpperCase() + "_nr_" + fileNr;

                        if (context.getRegisteredObject("NazwaPaczki") != null) {
                            String nazwaWarstwy = (String) context.getRegisteredObject("NazwaPaczki");
                            nazwaPliku = (String) context.getRegisteredObject("NazwaPaczki") + "_nr_" + fileNr;
                            if (context.getRegisteredObject("InkasentPaczki") != null) {
                                String nazwaInkasenta = (String) context.getRegisteredObject("InkasentPaczki");
                                nazwaPliku = nazwaInkasenta.trim() + "_" + nazwaPliku;
                            }
                        }

                        String destination = dirMain.getName() + "\\" + dirYear.getName() + "\\" + dirMonth.getName() + "\\" + nazwaPliku + ".kml";

                        Kml kml = KmlFactory.createKml();
                        Document doc = kml.createAndSetDocument().withName("WARSTWY").withOpen(true);

                        // create a Folder

                        Folder folderImport = doc.createAndAddFolder();
                        folderImport.withName(nazwaPliku).withOpen(true);

                        String nazwaWarstwyLabels = nazwaWarstwy.trim().substring(0, nazwaWarstwy.length() - 4) + " - ulice (" + fileNr + "/" + nuberOfFiles + ")";
                        Folder folderLabels = folderImport.createAndAddFolder();
                        folderLabels.withName(nazwaWarstwyLabels).withOpen(true);

                        String nazwaWarstwyMarkers = nazwaWarstwy.trim().substring(0, nazwaWarstwy.length() - 4) + " - domy (" + fileNr + "/" + nuberOfFiles + ")";
                        Folder folderMarkers = folderImport.createAndAddFolder();
                        folderMarkers.withName(nazwaWarstwyMarkers).withOpen(true);


                        Iterator<markers.Marker> iterL = labelsListTEMP.iterator();
                        while (iterL.hasNext()) {
                            licznikM++;
                            // System.out.println(licznikM);

                            markers.Marker markersRow = iterL.next();

                            double longitude;
                            double latitude;
                            String markerLabel;
                            String miasto;
                            String iloscOdczytow;
                            String iloscWykonanych;

                            longitude = markersRow.getGeometry().getCoordinates().get(1);
                            latitude = markersRow.getGeometry().getCoordinates().get(0);

                            miasto = markersRow.getProperties().getMiasto();
                            markerLabel = "ul. " + markersRow.getProperties().getUlica();
                            iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();
                            iloscWykonanych = markersRow.getProperties().getIloscOdczytowWykonanych().toString();

                            createStreet(doc, folderLabels, longitude, latitude, miasto, markerLabel, iloscOdczytow, iloscWykonanych);

                            iterL.remove();

                            if (licznikM == maxMarkers) {
                                break;
                            }
                        }

                        Iterator<markers.Marker> iterM = markersListTEMP.iterator();
                        while (iterM.hasNext()) {
                            licznikM++;
                            // System.out.println(licznikM);
                            markers.Marker markersRow = iterM.next();

                            double longitude;
                            double latitude;
                            String markerLabel;
                            String miasto;
                            String iloscOdczytow;
                            String iloscWykonanych;

                            longitude = markersRow.getGeometry().getCoordinates().get(1);
                            latitude = markersRow.getGeometry().getCoordinates().get(0);

                            miasto = markersRow.getProperties().getMiasto();
                            markerLabel = "ul. " + markersRow.getProperties().getUlica() + " " + markersRow.getProperties().getNumerDomu() + markersRow.getProperties().getNumerDomuChars();
                            iloscOdczytow = markersRow.getProperties().getIloscOdczytow().toString();
                            iloscWykonanych = markersRow.getProperties().getIloscOdczytowWykonanych().toString();

                            createPlacemark(doc, folderMarkers, longitude, latitude, miasto, markerLabel, iloscOdczytow, iloscWykonanych);

                            iterM.remove();

                            if (licznikM == maxMarkers) {
                                break;
                            }
                        }

                        // marshals to console
                        // kml.marshal();

                        // print and save
                        try {
                            Marshaller marshaller = JAXBContext.newInstance(new Class[] { Kml.class }).createMarshaller();
                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
                                @Override
                                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                                    return namespaceUri.matches("http://www.w3.org/\\d{4}/Atom") ? "atom"
                                            : (namespaceUri.matches("urn:oasis:names:tc:ciq:xsdschema:xAL:.*?") ? "xal"
                                            : (namespaceUri.matches("http://www.google.com/kml/ext/.*?") ? "gx" : (namespaceUri.matches("http://www.opengis.net/kml/.*?") ? "" : (null))));
                                }
                            });
                            File file = new File(destination);
                            marshaller.marshal(kml, file);
                            // kml.marshal();
                        } catch (JAXBException e) {

                            e.printStackTrace();
                        }

                    }

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


    public Boolean getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Boolean stateNew) {
        this.currentState = stateNew;
    }

//    public void setFiles(List<File> files) {
//        this.files = files;
//    }

    public void setPointsCount(long pointsCount) {
        this.pointsCount = pointsCount;
    }

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
}
