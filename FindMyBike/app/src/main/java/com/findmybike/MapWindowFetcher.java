package com.findmybike;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;



public class MapWindowFetcher {

    public MapWindowFetcher() {}

    public LatLng getBestPositionFit(double lat, double lng, float accuracy) {
        // Location.getAccuracy() determines the accuracy in meters with 68% confidence.
        // Thus, we investigate the map data in the window with radius accuracy,
        // and returns the position of the nearest point defining the periphery of
        // a bicycle parking tagged area, or just the input point if no bicycle parking
        // area is within the window
        double km_accuracy = accuracy / 1000;
        double radius_earth = 6378;

        // find coordinates defining the accuracy map window
        double w_left = lng - (km_accuracy / radius_earth) * (180 / Math.PI) / Math.cos(lat * Math.PI/180);;
        double w_right = lng + (km_accuracy / radius_earth) * (180 / Math.PI) / Math.cos(lat * Math.PI/180);;
        double w_up = lat + (km_accuracy / radius_earth) * (180 / Math.PI);
        double w_down = lat - (km_accuracy / radius_earth) * (180 / Math.PI);

        LatLng lower_left = new LatLng(w_down, w_left);
        LatLng upper_right = new LatLng(w_up, w_right);

        // fetch xml data of map within the accuracy window
        String xml = getXmlMapDataWithinWindow(lower_left, upper_right);

        // search the xml data for bicycle parking areas
        List<LatLng> bike_areas = findBikeParkingsInXmlMapData(xml);

        // bail out if no bicycle parking areas are within the accuracy window
        if (bike_areas.isEmpty()) {
            LatLng res = new LatLng(lat, lng);
            WriteToFile.writeToFile("MapWindowFetcher_results", "no bike areas near\n"+res+"\n", true);
            return new LatLng(lat, lng);
        }

        // a bicycle parking area was found! Predict position to closest point
        // defining the parking area region and return it
        LatLng curr_best_latlng = null;
        double curr_best_dist = Long.MAX_VALUE;

        for (LatLng ll : bike_areas) {
            double curr_dist = distance(lat, lng, ll.latitude, ll.longitude);
            if (curr_dist < curr_best_dist) {
                curr_best_latlng = ll;
                curr_best_dist = curr_dist;
            }
        }

        WriteToFile.writeToFile("MapWindowFetcher_results", "bike area found!\n"+curr_best_latlng+"\n", true);

        return curr_best_latlng;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public String getXmlMapDataWithinWindow(LatLng lowerLeft, LatLng upperRight) {
        URL url = null;
        HttpURLConnection urlConnection = null;
        String xml = "";
        try {
            String params = ""+lowerLeft.longitude+","+lowerLeft.latitude+
                    ","+upperRight.longitude+","+upperRight.latitude;
            params = URLEncoder.encode(params, "UTF-8");


            url = new URL("http://overpass-api.de/api/map?bbox="+params);


            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();
            in = new BufferedInputStream(in);
            xml = getStringFromInputStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return xml;
    }

    public List<LatLng> findBikeParkingsInXmlMapData(String xml) {
        List<LatLng> coords = new ArrayList<LatLng>();
        try {
            DocumentBuilderFactory domFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(
                    new ByteArrayInputStream(xml.getBytes("utf-8"))));

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            String query = "//*[@id=number(//way/tag[@k='amenity' and @v='bicycle_parking']/../@id)]/nd/@ref";
            XPathExpression expr = xpath.compile(query);

            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            for (int i = 0; i < nodes.getLength(); i++) {
                // running through all area-defining id's to fetch corresponding node points
                long curr_id = Long.valueOf(nodes.item(i).getNodeValue());
                query = "//node[@id="+curr_id+"]/@lat | //node[@id="+curr_id+"]/@lon";
                expr = xpath.compile(query);

                result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList latlongsnodelist = (NodeList) result;

                for (int j = 0; j < latlongsnodelist.getLength(); j += 2) {
                    double lat = Double.valueOf(latlongsnodelist.item(j).getNodeValue());
                    double lng = Double.valueOf(latlongsnodelist.item(j+1).getNodeValue());
                    coords.add(new LatLng(lat, lng));
                }
            }

            query = "//*[@id=number(//node/tag[@k='amenity' and @v='bicycle_parking']/../@id)]/@lat |" +
                    "//*[@id=number(//node/tag[@k='amenity' and @v='bicycle_parking']/../@id)]/@lon";
            expr = xpath.compile(query);

            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;

            for (int i = 0; i < nodes.getLength(); i += 2) {
                double lat = Double.valueOf(nodes.item(i).getNodeValue());
                double lng = Double.valueOf(nodes.item(i+1).getNodeValue());
                coords.add(new LatLng(lat, lng));
            }

        } catch (XPathExpressionException | ParserConfigurationException
                | IOException | SAXException e) {
            e.printStackTrace();
        }

        return coords;
    }

    // convert InputStream to String
    private String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
/*
    class LatLng {
        private double _lat;
        private double _lng;

        public LatLng(double lat, double lng) {
            _lat = lat;
            _lng = lng;
        }

        public double lat() { return _lat; }
        public double lng() { return _lng; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) { return true; }
            if (!(obj instanceof LatLng)) { return false; }
            LatLng other = (LatLng) obj;
            if (_lat != other._lat ||
                    _lng != other._lng) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return _lat+":"+_lng;
        }
    }*/

}
