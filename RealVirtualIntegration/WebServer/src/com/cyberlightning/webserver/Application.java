package com.cyberlightning.webserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

import com.cyberlightning.webserver.services.Gzip;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.DataStorageService;
import com.cyberlightning.webserver.services.TranslationService;
import com.cyberlightning.webserver.sockets.MessageObject;
import com.cyberlightning.webserver.sockets.UdpSocket;
import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;


public class Application  {

	public static void main(String[] args) throws Exception, IOException {
		
		
		
		Runnable dataBase = DataStorageService.getInstance();
		Thread dbThread = new Thread(dataBase);
		dbThread.start();
		
		Runnable websocket = new WebSocket();
		Thread webThread = new Thread(websocket);
		webThread.start();
		
		Runnable httpSocket = new HttpSocket();
		Thread httpThread = new Thread(httpSocket);
		httpThread.start();

		Runnable udpSocket = new UdpSocket();
		Thread udpThread = new Thread(udpSocket);
		udpThread.start();
		
		String sample = "{\"d23c0586984d35eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"orientation\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.003545045852661133,0.05859129875898361,-0.5206212997436523]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"orientation\",\"power\":1.5,\"vendor\":\"Samsung Inc.\",\"name\":\"Orientation Sensor\"}},{\"value\":{\"unit\":\"rad/s\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[9.9683837890625,0.23239292204380035,-1.8811875581741333]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gyroscope\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL Gyro\"}},{\"value\":{\"unit\":\"lx\",\"primitive\":\"double\",\"time\":\"2013-11-15 14:56\",\"values\":357.77637},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"light\",\"power\":0.75,\"vendor\":\"Capella\",\"name\":\"CM3663 Light sensor\"}},{\"value\":{\"unit\":\"uT\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[357.81671142578125,0.5156025290489197,-1.8891750574111938]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"magneticfield\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}},{\"value\":{\"unit\":\"m/s2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.3239738643169403,-0.09122344106435776,9.800872802734375]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"linearacceleration\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL linear accel\"}},{\"value\":{\"unit\":\"m/s2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.32213011384010315,-0.0398171991109848,9.804611206054688]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"accelerometer\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL accel\"}},{\"value\":{\"unit\":\"quaternion\",\"primitive\":\"array\",\"time\":\"2013-11-15 14:56\",\"values\":[357.8206787109375,0.5172339677810669,-1.8906971216201782]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"rotationvector\",\"power\":1.5,\"vendor\":\"Google Inc.\",\"name\":\"Rotation Vector Sensor\"}},{\"value\":{\"unit\":\"m/s2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.0030828863382339478,-8.415747433900833E-4,0.003661018330603838]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gravity\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL gravity\"}}],\"attributes\":{\"gps\":[65.5,25.3],\"name\":\"Android device\"}}}}";
		TranslationService.decodeSensorJson(sample);
		MessageService.getInstance().startThread();
		
		for(String s : args) {
			if(s.contentEquals("-simulate")){
			Runnable test = new TestRoutine();
			Thread t = new Thread(test);
			t.start();
			}
		}
		

	}
	
	
	
	
}
