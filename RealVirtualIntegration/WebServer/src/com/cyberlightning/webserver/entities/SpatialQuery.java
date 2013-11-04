package com.cyberlightning.webserver.entities;

import com.cyberlightning.webserver.StaticResources;

public class SpatialQuery {
	
	public int queryType;
	public float[] points;
	public int maxResults;
	public int radius;
	public String type;
	
	public SpatialQuery(float _minLat, float _minLon, float _maxLat, float _maxLon, int _maxResults) {
		this.queryType = StaticResources.QUERY_SPATIA_BOUNDING_BOX;
		this.maxResults = _maxResults;
		this.points = new float[4];
		this.points[0] = _minLat;
		this.points[1] = _minLon;
		this.points[2] = _maxLat;
		this.points[3] = _maxLon;
	}
	
	public SpatialQuery(float _lat, float _lon,int _radius, int _maxResults) {
		this.queryType = StaticResources.QUERY_SPATIAL_CIRCLE;
		this.maxResults = _maxResults;
		this.radius = _radius;
		this.points = new float[2];
		this.points[0] = _lat;
		this.points[1] = _lon;
	}
	
	public SpatialQuery(float[] _points, int _maxResults) {
		this.queryType = StaticResources.QUERY_SPATIA_SHAPE;
		this.maxResults = _maxResults;
		this.points = _points;
	}
	
	
	public SpatialQuery(int _query, String _type, int _maxResults) {
		this.type = _type;
		this.maxResults = _maxResults;
		this.queryType = StaticResources.QUERY_TYPE;
	}

}
