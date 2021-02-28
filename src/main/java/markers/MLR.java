package markers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MLR {

	@SerializedName("place_id")
	@Expose
	private String placeId;
	@SerializedName("licence")
	@Expose
	private String licence;
	@SerializedName("osm_type")
	@Expose
	private String osmType;
	@SerializedName("osm_id")
	@Expose
	private String osmId;
	@SerializedName("boundingbox")
	@Expose
	private List<String> boundingbox = new ArrayList<String>();
	@SerializedName("lat")
	@Expose
	private String lat;
	@SerializedName("lon")
	@Expose
	private String lon;
	@SerializedName("display_name")
	@Expose
	private String displayName;
	@SerializedName("class")
	@Expose
	private String _class;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("importance")
	@Expose
	private Double importance;

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getLicence() {
		return licence;
	}

	public void setLicence(String licence) {
		this.licence = licence;
	}

	public String getOsmType() {
		return osmType;
	}

	public void setOsmType(String osmType) {
		this.osmType = osmType;
	}

	public String getOsmId() {
		return osmId;
	}

	public void setOsmId(String osmId) {
		this.osmId = osmId;
	}

	public List<String> getBoundingbox() {
		return boundingbox;
	}

	public void setBoundingbox(List<String> boundingbox) {
		this.boundingbox = boundingbox;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getClass_() {
		return _class;
	}

	public void setClass_(String _class) {
		this._class = _class;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getImportance() {
		return importance;
	}

	public void setImportance(Double importance) {
		this.importance = importance;
	}

}