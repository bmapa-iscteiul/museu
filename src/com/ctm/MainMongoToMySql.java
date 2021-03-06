package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class MainMongoToMySql {
	
	static Properties MongoToMysqlIni = new Properties();
	static Properties mongoIni = new Properties();
	
	public static void loadIni() {
        try {
			MongoToMysqlIni.load(new FileInputStream("MongoToMysql.ini"));
			mongoIni.load(new FileInputStream("CloudToMongo.ini"));
		} catch (FileNotFoundException  e2) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public static String getMysqlProperty(String key) {
		loadIni();
		return MongoToMysqlIni.getProperty(key);
	}
	
	public static String getMongoProperty(String key) {
		loadIni();
		return mongoIni.getProperty(key);
	}
	

	public static void main(String[] args) {
		loadIni();
		ShareResourceMedicoes share = new ShareResourceMedicoes();
		SepararMedicoesThread link = new SepararMedicoesThread(share);
		ShareResourceRegisto share2 = new ShareResourceRegisto();
		MedicaoThread tmp = new ThreadTemperatura(share,share2);
		MedicaoThread hum = new ThreadHumidade(share, share2);
		MedicaoThread cell = new ThreadLuminosidade(share, share2);
		MedicaoThread mov = new ThreadMovimento(share, share2);
		SendToMysql stm= new SendToMysql(share2);
		share2.setSendTomysql(stm);
		link.start();
		tmp.start();
		hum.start();
		cell.start();
		mov.start();
		stm.start();
	}

}
