
package markers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class EMarkers {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("markers")
    @Expose
    private List<Marker> markers = new ArrayList<Marker>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public EMarkers() {
    }

    /**
     * 
     * @param markers
     * @param type
     */
    public EMarkers(String type, List<Marker> markers) {
        super();
        this.type = type;
        this.markers = markers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

}
