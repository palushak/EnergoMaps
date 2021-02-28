
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
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("idPaczki")
    @Expose
    private String idPaczki;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Marker() {
    }

    /**
     * 
     * @param id
     * @param idPaczki
     * @param properties
     * @param type
     * @param geometry
     */
    public Marker(String type, Properties properties, Geometry geometry, String id, String idPaczki) {
        super();
        this.type = type;
        this.properties = properties;
        this.geometry = geometry;
        this.id = id;
        this.idPaczki = idPaczki;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public String getIdPaczki() {
		return idPaczki;
	}

	public void setIdPaczki(String idPaczki) {
		this.idPaczki = idPaczki;
	}

}
