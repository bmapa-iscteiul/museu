package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;


public class CloudToMongo implements MqttCallback {
    MqttClient mqttclient;
    static MongoClient mongoClient;
    static DB db;
    static DBCollection mongocol_sensor;
    static DBCollection mongocol_invalidas;
    static String cloud_server = new String();
    static String cloud_topic = new String();
    static String mongo_host = new String();
    static String mongo_database = new String();
    static String mongo_collection_invalidas = new String();
    static String mongo_collection_sensor = new String();
 //   static MongoToMysql mysql = new MongoToMysql();
    static Properties CloudToMongoIni = new Properties();
    
    //thresh_holds
    static int TMP_MAX;
    static int TMP_MIN;
    static int HUM_MAX;
    static int HUM_MIN;
    static int CELL_MAX ;
    static int CELL_MIN;
       
    static final int LIST_MAX_SIZE = 20;
    ArrayList<MedicaoSensor> medicoesSensorQueue = new ArrayList<MedicaoSensor>(); 

    public static void main(String[] args) {
    	loadIni();
        new CloudToMongo().connecCloud();
        new CloudToMongo().connectMongo();
    }

    public void connecCloud() {
		int i;
        try {
			i = new Random().nextInt(100000);
            mqttclient = new MqttClient(cloud_server, "CloudToMongo_"+String.valueOf(i)+"_"+cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(cloud_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectMongo() {
		mongoClient = new MongoClient(new MongoClientURI(mongo_host));
		db = mongoClient.getDB(mongo_database);
        mongocol_sensor = db.getCollection(mongo_collection_sensor);
        mongocol_invalidas = db.getCollection(mongo_collection_invalidas);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void messageArrived(String topic, MqttMessage c) throws Exception {
        try {
                DBObject message = (DBObject) JSON.parse(clean(c.toString()));
                DBObject original_msg =  (DBObject) JSON.parse(clean(c.toString()));
                if(hasAllFields(message)) {
                	if(!isValidMessage(message).toString().equals(original_msg.toString())) {
                		mongocol_invalidas.insert(original_msg);
                		System.out.println("Inserida na colecao invalidas");
                		System.out.println("yo");
                	}
                	mongocol_sensor.insert(message);
                	System.out.println("Inserida na colecao sensor");
                }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    
    @Override
    public void connectionLost(Throwable cause) {
    
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
  
    public String clean(String message) {
    	String old = "\""+"mov"+"\""+":"+"\""+"0"+"\"";
    	message = message.replace(old,",");
		return message.replace("\""+"\"", "\""+","+"\"");// (message.replaceAll("\"\"", "\","));   
    }	
    
    public static Properties getIniFile() {
        try {
			CloudToMongoIni.load(new FileInputStream("CloudToMongo.ini"));
		} catch (FileNotFoundException  e2) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return CloudToMongoIni;
    }
    
    
    //Loads all the info of the file into variables
    public static void loadIni() {
    	try {
            CloudToMongoIni = getIniFile();
            TMP_MAX = Integer.parseInt(CloudToMongoIni.getProperty("tmp_max"));   
            TMP_MIN = Integer.parseInt(CloudToMongoIni.getProperty("tmp_min"));
            HUM_MAX = Integer.parseInt(CloudToMongoIni.getProperty("hum_max"));
            HUM_MIN = Integer.parseInt(CloudToMongoIni.getProperty("hum_min"));
            CELL_MAX = Integer.parseInt(CloudToMongoIni.getProperty("cell_max"));
            CELL_MIN = Integer.parseInt(CloudToMongoIni.getProperty("cell_min"));
            
            cloud_server = CloudToMongoIni.getProperty("cloud_server");
            cloud_topic = CloudToMongoIni.getProperty("cloud_topic");
            mongo_host = CloudToMongoIni.getProperty("mongo_host");
            mongo_database = CloudToMongoIni.getProperty("mongo_database");
            mongo_collection_invalidas = CloudToMongoIni.getProperty("mongo_collection_invalidas");
            mongo_collection_sensor = CloudToMongoIni.getProperty("mongo_collection_sensor");
            System.out.println("Loaded the ini file");
        } catch (Exception e) {

            System.out.println("Error reading CloudToMongo.ini file " + e);
            JOptionPane.showMessageDialog(null, "The CloudToMongo.ini file wasn't found.", "CloudToMongo", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean hasAllFields(DBObject message) {
    	try {
	    	if(message.get("tmp") == "null" ||
	    			message.get("hum") == "null" ||
	    					message.get("cell") == "null" ||
	    							message.get("dat") == "null" ||
	    								message.get("tim") == "null" ||
	    								 message.get("sens") == "null"
	    		) {
	    		return false;
	    	};
    	}catch(Exception e) {
    		System.out.println("N�o tem os campos todos");
    	}
    	return true;
    }
    
    public DBObject isValidMessage(DBObject message) {
    	double tmp = Double.parseDouble(message.get("tmp").toString());
    	double hum = Double.parseDouble(message.get("hum").toString());
        int cell = Integer.parseInt(message.get("cell").toString());
        //int mov = Integer.parseInt(message.get("mov").toString());
        
        if(tmp < TMP_MIN || tmp > TMP_MAX) {message.put("tmp", "NA");}
        if(hum < HUM_MIN || hum > HUM_MAX) {message.put("hum", "NA");}
        if(cell < CELL_MIN || cell > CELL_MAX) {message.put("cell", "NA");}
        //if(mov < 0 || mov > 1) {return false;}
    	return message;
    }
    
     
}