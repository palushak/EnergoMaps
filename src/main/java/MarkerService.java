import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import markers.EMarkers;
import markers.Properties;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Jacek Paluszak <palsoft.pl>
 */
public class MarkerService extends Service<Object[]> {

    private IntegerProperty counterProperty = new SimpleIntegerProperty(0);
    private ObservableList<RowPlikCSV_Map> listOdczyty;

    private Boolean currentState;

    static String[] cookies;

    Map<String, Integer> miastaMap;
    Map<String, Integer> uliceMap;
    Map<String, Integer> domyMap;

    int iloscMiastLabel = 0;
    int iloscUlicLabel = 0;
    int iloscDomowLabel = 0;
    int iloscBlednychMarkerowLabel = 0;
    int iloscOdczytowLabel = 0;
    int iloscPoprawnychOdczytowLabel = 0;
    int iloscBlednychOdczytowLabel = 0;
    int iloscWykonanychLabel = 0;

    SOAPConnectionFactory soapConnectionFactory;
    SOAPConnection soapConnection;
    GeoApiContext context;
    String soapEndpointUrl;

    static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static JsonFactory JSON_FACTORY = new JacksonFactory();

    private List<Long> errorsGeoList, errorsAddList;
    private Map<String, String> points;

    @Override
    protected Task<Object[]> createTask() {
        return new Task<Object[]>() {
            @Override
            protected Object[] call() throws Exception {

                int counter = 1;
                int max = listOdczyty.size();
                Object[] lists;
                points = new HashMap<>();

                currentState = true;
                lists = new Object[16];
                errorsGeoList = new ArrayList<Long>();
                errorsAddList = new ArrayList<Long>();

                EMarkers emarkersLabels = new EMarkers();
                emarkersLabels.setType("LabelsCollection");
                List<markers.Marker> labelsList = new ArrayList<markers.Marker>();

                EMarkers emarkersMarkers = new EMarkers();
                emarkersMarkers.setType("MarkersCollection");
                List<markers.Marker> markersList = new ArrayList<markers.Marker>();

                // GOOGLE
                context = new GeoApiContext.Builder().apiKey("AIzaSyDJBzpwv6y4wRCsXQv3gUBZr1J9PY3Xq5g").build();

                // TARGEO

                String custName = "palsoft";
                String host_key = "ZWQ2MmViZDcxMGUwNjUxMDRkOTE2YmY3YzY1OTFkYThiM2FiYWE1OQ==";

                // MAPLINKED
                String userMaplinked = "jacekpaluszak@gmail.com";
                String maplinked_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1YjdlODc0Y2JmNTQ2NTRkZThjZGEyZjciLCJuYW1lIjoiQVBJS0VZIiwiY3JlYXRlZFRpbWUiOjE1MzUwNDI2NTIwMzYsImlhdCI6MTUzNTA0MjY1Mn0.P6EJlNkrlbj4MKPBjmp2NurZz5TpjjM30cRd6oNCPFI";

                // OPEGIEKA
                String userOpegieka = "gcjp01";
                String passOpegieka = "n9bDRzu2et2yP7cS";

                // WSDL:
                // "http://mapa.targeo.pl/service.html?rpc=WSDL&typ=1&ver=22&tmk=palsoft&k=ZWQ2MmViZDcxMGUwNjUxMDRkOTE2YmY3YzY1OTFkYThiM2FiYWE1OQ=="
                soapEndpointUrl = "http://mapa.targeo.pl/SoapServer.html?tmk=" + custName + "&ver=22&typ=1&k=" + host_key;

                try {

                    boolean googleWorks = false;
                    boolean targeoWorks = false;
                    boolean maplinkedWorks = false;
                    boolean opegiekaWorks = true;

                    if (googleWorks) {

                    }

                    if (targeoWorks) {

                        // TARGEO SOAP
                        soapConnectionFactory = SOAPConnectionFactory.newInstance();
                        soapConnection = soapConnectionFactory.createConnection();

                        // Send SOAP Message to SOAP Server
                        SOAPMessage soapResponse = soapConnection.call(createSOAPRequestSecureLogin(), soapEndpointUrl);

                        MimeHeaders session = soapResponse.getMimeHeaders();
                        cookies = session.getHeader("Set-Cookie");

                        // SOAP Response
                        // System.out.println("Response SOAP Message:");
                        // soapResponse.writeTo(System.out);

                    }

                    if (maplinkedWorks) {

                    }

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

                        propertiesLabel.setMag(0.0);
                        propertiesLabel.setInkasent("");

                        propertiesLabel.setMiasto(row.miejscowosc.getValue());

                        if (row.ulica.isNotNull().getValue()) {
                            propertiesLabel.setUlica(row.ulica.getValue());
                        } else {
                            propertiesLabel.setUlica("");
                        }


                        markerLabel.setId("Label");
                        propertiesLabel.setStatus("");

                        if (row.budynek.isNotNull().getValue()) {
                            properties.setNumerDomu(row.budynek.getValue().toString());

                        } else {
                            properties.setNumerDomu("");
                            properties.setStatus("bez numeru domu");
                            iloscBlednychOdczytowLabel++;
                            errorsAddList.add(Long.parseLong(row.id_punktu.getValue()));
                        }

                        Double latitude = 0.0;
                        Double longitude = 0.0;


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
                                    && markersRow.getProperties().getNumerDomu().equals(marker.getProperties().getNumerDomu())
                                    && markersRow.getProperties().getStatus().equals(marker.getProperties().getStatus())) {

                                found = true;

                            }

                        }

                        if (!found) {


                            markersList.add(marker);
                        }

                        boolean foundLabel = false;
                        for (markers.Marker markersRow : labelsList) {

                            if (markersRow.getProperties().getMiasto().equals(markerLabel.getProperties().getMiasto())
                                    && markersRow.getProperties().getUlica().equals(markerLabel.getProperties().getUlica())
                                    && markersRow.getProperties().getStatus().equals(markerLabel.getProperties().getStatus())) {
                                foundLabel = true;


                            }

                        }

                        if (!foundLabel) {


                            String[] xxyy = new String[2];

                            if (googleWorks) {
                                xxyy = geocodeStreetGoogle(propertiesLabel, context);
                            }
                            if (targeoWorks) {
                                xxyy = geocodeStreetTargeo(propertiesLabel);
                            }
                            if (maplinkedWorks) {
                                xxyy = geocodeStreetMaplinked(propertiesLabel, maplinked_key);
                            }

                            if (opegiekaWorks) {
                                xxyy = geocodeStreetOpegieka(propertiesLabel, userOpegieka, passOpegieka);
                            }

                            String xx = xxyy[0];
                            String yy = xxyy[1];

                            if (xx.length() > 0 && yy.length() > 0) {
                                Double latit = Double.parseDouble(yy);
                                Double longit = Double.parseDouble(xx);

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


                    // soapConnection.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }

                try {
                    emarkersLabels.setMarkers(labelsList);
                    emarkersMarkers.setMarkers(markersList);

                    iloscOdczytowLabel = listOdczyty.size();

                    Gson gson = new Gson();
                    String listaLabelsString = gson.toJson(emarkersLabels);
                    String listaMarkersString = gson.toJson(emarkersMarkers);

                    lists[0] = listaLabelsString;
                    lists[1] = listaMarkersString;
                    lists[2] = Integer.toString(labelsList.size());
                    lists[3] = Integer.toString(markersList.size());
                    lists[4] = Integer.toString(iloscBlednychMarkerowLabel);
                    lists[5] = Integer.toString(iloscOdczytowLabel);
                    lists[6] = Integer.toString(iloscPoprawnychOdczytowLabel);
                    lists[7] = Integer.toString(iloscBlednychOdczytowLabel);
                    lists[8] = Integer.toString(iloscMiastLabel);
                    lists[9] = Integer.toString(iloscUlicLabel);
                    lists[10] = Integer.toString(iloscDomowLabel);
                    lists[11] = Integer.toString(iloscWykonanychLabel);
                    lists[12] = labelsList;
                    lists[13] = markersList;
                    lists[14] = errorsGeoList;
                    lists[15] = errorsAddList;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                return lists;
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

            private String[] geocodeMaplinked(Properties properties, String apikey) {

                String[] xxyy = new String[2];
                try {

                    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    });
                    String address = properties.getNumerDomu() + "," + properties.getUlica() + "," + properties.getMiasto() + ",pl,";
//					System.out.println("address  >>>>>>>>>>>>" + address);
                    GenericUrl genericUrl = new GenericUrl("https://servicemap.pl:3491/api/nominatim/search?q=" + address + "&format=json&countrycodes=pl");

                    HttpRequest request = requestFactory.buildGetRequest(genericUrl);

                    HttpHeaders headers = new HttpHeaders();
                    headers.put("x-auth", apikey);
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

            private String[] geocodeOpegieka(Properties properties, String user, String pass) {

                String[] xxyy = new String[2];
                try {

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

            private String[] geocodeTargeo(Properties properties) {

                String[] xxyy = new String[2];
                try {
                    SOAPMessage soapResponseGeocode;
                    if (properties.getUlica() != null) {
                        soapResponseGeocode = soapConnection
                                .call(createSOAPRequestGeocode(properties.getMiasto(), "", properties.getUlica(), properties.getNumerDomu()), soapEndpointUrl);
                    } else {
                        soapResponseGeocode = soapConnection.call(createSOAPRequestGeocode(properties.getMiasto(), "", "", ""), soapEndpointUrl);

                    }

                    SOAPBody responseBody = soapResponseGeocode.getSOAPBody();

                    Node returnX = (Node) responseBody.getElementsByTagName("x").item(0);
                    Node returnY = (Node) responseBody.getElementsByTagName("y").item(0);

                    String xx = returnX.getTextContent();
                    String yy = returnY.getTextContent();

                    xxyy[0] = xx;
                    xxyy[1] = yy;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return xxyy;
            }

            private String[] geocodeStreetOpegieka(Properties properties, String user, String pass) {

                String[] xxyy = new String[2];
                try {

                    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    });

                    String address = "";
                    if (properties.getUlica() != null) {
                        address = properties.getUlica() + "+" + properties.getMiasto();
                    } else {
                        address = properties.getMiasto();
                    }

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

            private String[] geocodeStreetMaplinked(Properties properties, String apikey) {
                String[] xxyy = new String[2];
                try {

                    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    });
                    String address = "";
                    if (properties.getUlica() != null) {
                        address = properties.getUlica() + ",+" + properties.getMiasto() + ",+pl";
                    } else {
                        address = properties.getMiasto();
                    }

                    GenericUrl genericUrl = new GenericUrl("https://servicemap.pl:3491/api/nominatim/search?q=" + address + "&format=json&countrycodes=pl");

                    HttpRequest request = requestFactory.buildGetRequest(genericUrl);

                    HttpHeaders headers = new HttpHeaders();
                    headers.put("x-auth", apikey);
                    request.setHeaders(headers);

                    // com.google.api.client.http.HttpResponse response = null;
                    String response = null;
                    String xx = "0.0";
                    String yy = "0.0";
                    try {
                        response = request.execute().parseAsString();

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
                        e.printStackTrace();
                    } finally {
                        if (response != null) {
                            // response.disconnect();
                        }
                    }

                    if (points.containsKey(xx + yy)) {
                        // System.out.println("FOUND SAME COORDINATES ULICE:" + properties.getUlica()+ "
                        // -> "+ yy +", "+ xx);
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
                        // System.out.println("FOUND SAME COORDINATES PO ZMIANIE:" +
                        // properties.getUlica()+ " -> " + yy +", "+ xx + ", R:"+ random);

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

            private String[] geocodeStreetGoogle(Properties properties, GeoApiContext context) {
                String[] xy = new String[2];
                String xx = "";
                String yy = "";
                try {
                    if (properties.getUlica() != null) {
                        // TODO
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                xy[0] = xx;
                xy[1] = yy;
                return xy;
            }

            private String[] geocodeStreetTargeo(Properties properties) {

                String[] xy = new String[2];
                SOAPMessage soapResponseGeocode;
                String xx = "";
                String yy = "";
                try {
                    if (properties.getUlica() != null) {
                        soapResponseGeocode = soapConnection.call(createSOAPRequestGeocode(properties.getMiasto(), "", properties.getUlica(), ""), soapEndpointUrl);
                        SOAPBody responseBody = soapResponseGeocode.getSOAPBody();
                        Node returnX = (Node) responseBody.getElementsByTagName("x").item(0);
                        Node returnY = (Node) responseBody.getElementsByTagName("y").item(0);
                        xx = returnX.getTextContent();
                        yy = returnY.getTextContent();
                    } else {
                        soapResponseGeocode = soapConnection.call(createSOAPRequestGeocode(properties.getMiasto(), "", "", ""), soapEndpointUrl);
                        SOAPBody responseBody = soapResponseGeocode.getSOAPBody();
                        Node returnX = (Node) responseBody.getElementsByTagName("x").item(0);
                        Node returnY = (Node) responseBody.getElementsByTagName("y").item(0);
                        xx = returnX.getTextContent();
                        yy = returnY.getTextContent();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                xy[0] = xx;
                xy[1] = yy;
                return xy;

            }

        };
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static SOAPMessage createSOAPRequestSecureLogin() throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespaceURI = "http://mapa.targeo.pl/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();

        // SOAP header
        SOAPHeader soapHeader = envelope.getHeader();

        // SOAP Body

        String user = "palsoft";
        String password = "8caf5dd6";
        String salt = "c3M2MxYzE0NjBjZWVlN2Q5YmRiZWNlMz";
        String token = "ch36zy3g";

        String passwordMD5 = DigestUtils.md5Hex(password);

        String tokenMD5 = DigestUtils.md5Hex(token);
        String tokenMD5_S = tokenMD5.substring(0, 8);

        String securePassword = DigestUtils.md5Hex(salt + passwordMD5 + tokenMD5_S);

        SOAPBody soapBody = envelope.getBody();

        SOAPBodyElement element = soapBody.addBodyElement(envelope.createName("secureLogin", "urn", myNamespaceURI));
        element.addChildElement("user").addTextNode(user);
        element.addChildElement("password").addTextNode(securePassword);
        element.addChildElement("token").addTextNode(tokenMD5_S);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", "secureLogin");
        headers.setHeader("Content-Type", "text/xml; charset=utf-8");

        soapMessage.saveChanges();

        /* Print the request message, just for debugging purposes */
        // System.out.println("Request SOAP Message:");
        // soapMessage.writeTo(System.out);
        // System.out.println("\n");

        return soapMessage;
    }

    private static SOAPMessage createSOAPRequestGeocode(String city, String pcode, String street, String house) throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespaceURI = "http://mapa.targeo.pl/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();

        // SOAP header
        SOAPHeader soapHeader = envelope.getHeader();

        // SOAP Body

        SOAPBody soapBody = envelope.getBody();

        SOAPBodyElement element = soapBody.addBodyElement(envelope.createName("geocode100m", "urn", myNamespaceURI));

        SOAPElement adressElement = element.addChildElement("address");
        adressElement.addChildElement("country").addTextNode("PL");
        adressElement.addChildElement("province").addTextNode(""); // województwo
        adressElement.addChildElement("district").addTextNode(""); // powiat
        adressElement.addChildElement("community").addTextNode(""); // gmina
        adressElement.addChildElement("city").addTextNode(city); // miasto
        adressElement.addChildElement("quarter").addTextNode(""); // dzielnica
        adressElement.addChildElement("pcode").addTextNode(pcode); // kod pocztowy
        adressElement.addChildElement("street").addTextNode(street); // ulica
        adressElement.addChildElement("house").addTextNode(house); // budynek

        SOAPElement paramsElement = element.addChildElement("params");
        paramsElement.addChildElement("max_results").addTextNode("1"); // możliwe wyniki
        paramsElement.addChildElement("p_province").addTextNode("false"); // precyzja - czy dokładne dopasowanie (nie
        // dopuszczać literówek) -
        paramsElement.addChildElement("p_district").addTextNode("false");
        paramsElement.addChildElement("p_community").addTextNode("false");
        paramsElement.addChildElement("p_city").addTextNode("true");
        paramsElement.addChildElement("p_street").addTextNode("false");
        paramsElement.addChildElement("p_pcode").addTextNode("false");

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", "geocode100m");
        headers.setHeader("Content-Type", "text/xml; charset=utf-8");

        headers.addHeader("Cookie", cookies[0]);

        soapMessage.saveChanges();

        /* Print the request message, just for debugging purposes */
        // System.out.println("Request SOAP Message:");
        // soapMessage.writeTo(System.out);
        // System.out.println("\n");

        return soapMessage;
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

        iloscMiastLabel = miastaMap.size();
        iloscUlicLabel = uliceMap.size();
        iloscDomowLabel = domyMap.size();

    }


    public int getCounterProperty() {
        return counterPropertyProperty().get();
    }

    public void setCounterProperty(int counterPropertyNew) {
        if (counterProperty == null) {
            counterProperty = new SimpleIntegerProperty();
        }
        this.counterProperty.set(counterPropertyNew);
    }

    public IntegerProperty counterPropertyProperty() {
        if (counterProperty == null) {
            counterProperty = new SimpleIntegerProperty();
        }
        return counterProperty;
    }

    public ObservableList<RowPlikCSV_Map> getListOdczyty() {
        return listOdczyty;
    }

    public void setListOdczyty(ObservableList<RowPlikCSV_Map> listOdczyty) {
        this.listOdczyty = listOdczyty;
    }

    public Boolean getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Boolean stateNew) {
        this.currentState = stateNew;
    }

}
