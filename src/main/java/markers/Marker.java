
package markers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Marker {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("properties")
    @Expose
    private Properties properties;
    @SerializedName("geometry")
    @Expose
    private Geometry geometry;
    @SerializedName("id_punktu")
    @Expose
    private String id_punktu;


    /**
     * No args constructor for use in serialization
     * 
     */
    public Marker() {
    }

    /**
     * @param type
     * @param id_punktu
     * @param properties
     * @param geometry
     */

    public Marker(String type, Properties properties, Geometry geometry, String id_punktu) {
        super();
        this.type = type;
        this.id_punktu = id_punktu;
        this.properties = properties;
        this.geometry = geometry;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getId_punktu() {
        return id_punktu;
    }

    public void setId_punktu(String id_punktu) {
        this.id_punktu = id_punktu;
    }
}
