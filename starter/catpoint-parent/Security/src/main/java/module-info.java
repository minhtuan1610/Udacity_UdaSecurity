module com.udacity.catpoint.security {
	requires java.desktop;
	requires com.google.common;
	requires com.google.gson;
	requires java.prefs;
	exports com.udacity.catpoint.security.data;
	exports com.udacity.catpoint.security.service;
}