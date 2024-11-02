module com.udacity.catpoint.security {
	requires java.desktop;
	requires com.google.common;
	requires com.google.gson;
	requires java.prefs;
	requires com.udacity.catpoint.image;
	requires miglayout.swing;
	opens com.udacity.catpoint.security.data to com.google.gson;
}