package com.google.codeu.api;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.File;


import java.net.URL;
import java.net.URLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

import java.io.ByteArrayOutputStream;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.client.utils.URIBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.params.HttpParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpResponse;

import com.google.gson.Gson;


public class YelpQuery {
  private String apiKey;
  private Map<String, String> params;

  private Set<String> validParams;
  private Map<String, String> radiusMap;
  private Map<String, String> priceMap; 

  public YelpQuery() throws IOException {
    this.apiKey = readFile(new File("api.key").getAbsolutePath());
    this.params = new HashMap<String, String>() {{ 
      put("location", "Chicago");
      put("term", "ice cream");
    }};

    this.validParams = new HashSet<String>() {{
      add("term"); add("location"); add("latitude");
      add("longitude"); add("radius"); add("categories");
      add("locale"); add("limit"); add("offset");
      add("sort_by"); add("price"); add("open_now");
      add("open_at"); add("attributes");
      
    }}; 
    // miles to meters
    this.radiusMap = new HashMap<String, String>() {{
      put("fivemi", "8046"); 
      put("tenmi", "16093");
      put("twentymi", "31286"); // capped at max
      put("none", "40000"); // Max radius value
    }};
  
    this.priceMap = new HashMap<String, String>() {{
      put("cheap", "$");
      put("averagecost", "$$");
      put("expensive", "$$$");
      put("veryexpensive", "$$$$"); 
    }};
  }
 
  public YelpQuery(HashMap<String, String> params) throws IOException {
    this.apiKey = readFile(new File("api.key").getAbsolutePath());

    this.params = params;

    this.validParams = new HashSet<String>() {{
      add("term"); add("location"); add("latitude");
      add("longitude"); add("radius"); add("categories");
      add("locale"); add("limit"); add("offset");
      add("sort_by"); add("price"); add("open_now");
      add("open_at"); add("attributes");
      
    }}; 
    this.radiusMap = new HashMap<String, String>() {{
      put("fivemi", "8046"); 
      put("tenmi", "16093");
      put("twentymi", "31286"); // capped at max
      put("none", "40000"); // Max radius value
    }};
  
    this.priceMap = new HashMap<String, String>() {{
      put("cheap", "$");
      put("averagecost", "$$");
      put("expensive", "$$$");
      put("veryexpensive", "$$$$"); 
    }};
  }

  // just uses basic business search; https://www.yelp.com/developers/documentation/v3/business_search
  public String createQuery() throws URISyntaxException, MalformedURLException, IOException {
    URIBuilder builder = new URIBuilder();
    HttpParams params = new BasicHttpParams();
    HttpClient httpClient = new DefaultHttpClient(params);
    builder.setScheme("https").setHost("api.yelp.com").setPath("/v3/businesses/search");
    // Adding parameters to search string: note, we need a location here in the parameters at least.
    for(Map.Entry<String, String> entry : this.params.entrySet()) {
      if(this.validParams.contains(entry.getKey())) {
        builder.setParameter(entry.getKey(), entry.getValue());
      }
      else if(entry.getKey().equals("filter")) {
        builder.setParameter("term", entry.getValue());
      }
      else if(entry.getKey().equals("city_val")) { // A case where we need to convert from Google Maps API name to Yelp name
        builder.setParameter("location", entry.getValue());
      }
      else if(entry.getKey().equals("priceFilter")) {
        String priceName = entry.getValue();
        String priceValue = this.priceMap.get(priceName); // TODO make this map
        builder.setParameter("price", priceValue);
      }
      else if(entry.getKey().equals("radiusFilter")) {
        String radiusValue = this.radiusMap.get(entry.getValue());
        builder.setParameter("radius", radiusValue);
      }
    }
    
    HttpGet httpget = new HttpGet(builder.build());
    httpget.setHeader("Authorization", "Bearer " + this.apiKey);
    httpget.setHeader("Accept", "application/json");
    HttpResponse httpresp = httpClient.execute(httpget);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    httpresp.getEntity().writeTo(outStream);
    return outStream.toString("UTF-8");//uri.toURL();
  }

  public String getQueryResponse(URL url) throws IOException, FileNotFoundException {
    URLConnection connection = url.openConnection();
    InputStream stream = connection.getInputStream();
    String result = IOUtils.toString(stream, StandardCharsets.UTF_8);
    return new Gson().toJson(result); // get data from stream;
  }

  public static String readFile(String path) throws IOException, FileNotFoundException {
    BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + path));
    return reader.readLine(); // TODO: error handling
  }
  
}


