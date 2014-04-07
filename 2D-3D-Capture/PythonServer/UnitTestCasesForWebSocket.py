'''
Created on Mar 6, 2014

@author: tharanga
'''
import unittest
from time import sleep
import EventService as es
from EventService import WebSocketServer as ws
from EventService import EventManager as em
import socket
from base64 import b64encode 
import struct
import MySQLdb
import json
import EventService
import flaskr
import tempfile

def encodeMessage( message):        
        message = b64encode(message)
        b1 =0x80 | 0x1 & 0x0f
        b2 = 0
        header=""
        payload_len = len(message)
        if payload_len < 126 :
            header = struct.pack('>BB', b1, payload_len)
            message= header +message
        elif (payload_len < ((2 ** 16) - 1)):
            b2 |= 126
            header += chr(b1)
            header += chr(b2)
            l = struct.pack(">H", payload_len)
            header += l
            message = header +message
        else:            
            b2 |= 127
            header += chr(b1)
            header += chr(b2)
            l = struct.pack(">Q", payload_len)
            header += l
            message = header +message
        return message

class TestWebSockets(unittest.TestCase):    

    def setUp(self):
        self.wsServer = ws('',12345,'127.0.0.1')
        self.wsServer.setRunning(True);
        sleep(1)
        self.testsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.testsocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)        # Create a socket object
        host = 'localhost' # Get local machine name
        port = 12345     
        self.testsocket.connect((host, port))

    def tearDown(self):
        self.wsServer.closeConnection();
        self.testsocket.close()
        sleep(1)

    def test_webSocketServerOBject(self):
        self.assertEqual(self.wsServer.SERVER, '', "Server set to the desired value")
        self.assertEqual(self.wsServer.PORT, 12345, "Server port is set correctly")
        self.assertEqual(self.wsServer.LOCALHOST, "127.0.0.1", "Localhost set to 127.0.0.1")
        
    def test_invalid_Request(self):
        message= "Test Message"
        self.testsocket.send(message)
        data = repr(self.testsocket.recv(1024))
        #print 'Response to invalid message<TestMessage>  %s'%(data)
        self.assertEqual(data, '\'CONNECTION_REJECTED\'', "Invalid Message rejected")
        
    def test_valid_WS_Request(self):
        message = "GET /mychat HTTP/1.1\nHost: server.example.com\nUpgrade: websocket\nConnection: Upgrade\nSec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\nSec-WebSocket-Protocol: chat\nSec-WebSocket-Version: 13\nOrigin: localhost\n\n"
#         message = "Test message"
        self.testsocket.sendall(message)
        wsresponse = repr(self.testsocket.recv(1024))        
        #print 'Response to valid ws request %s'%wsresponse
        self.assertNotEqual(wsresponse, '\'CONNECTION_REJECTED\'', "Connection is not rejected")
        self.assertIsNotNone(wsresponse, "Connection Response is not Empty")
        self.testsocket.sendall(("Test Message"))
        data = repr(self.testsocket.recv(1024))
        #print 'Response to un encoded Request  %s'%(data)
        self.assertEqual(data, "\'Un expected opcode\'", "In valid Message rejected")
        
    def test_invalid_Messge(self):
        message = "GET /mychat HTTP/1.1\nHost: server.example.com\nUpgrade: websocket\nConnection: Upgrade\nSec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\nSec-WebSocket-Protocol: chat\nSec-WebSocket-Version: 13\nOrigin: localhost\n\n"
        self.testsocket.sendall(message)
        wsresponse = repr(self.testsocket.recv(1024))
        sleep(1)
        self.testsocket.sendall("Test Message")
        data = repr(self.testsocket.recv(1024))
        self.assertEqual(data, "\'Un expected opcode\'", "In valid Message rejected")
        
    def test_malformed_Message(self):
        message = "GET /mychat HTTP/1.1\nHost: server.example.com\nUpgrade: websocket\nConnection: Upgrade\nSec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\nSec-WebSocket-Protocol: chat\nSec-WebSocket-Version: 13\nOrigin: localhost\n\n"
        self.testsocket.sendall(message)
        wsresponse = repr(self.testsocket.recv(1024))
#         print wsresponse
        self.testsocket.send(encodeMessage("Test Message"))#This line seems to get stuck at times. Solution is to use sendAll, use \n at the end
        data = repr(self.testsocket.recv(1024))
        self.assertEqual(data, "\'MISFORMATED MESSAGE\'", "Messages with out a type is rejected")
        
    def test_wellformed_Message_for_Text(self):
        message = "GET /mychat HTTP/1.1\nHost: server.example.com\nUpgrade: websocket\nConnection: Upgrade\nSec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\nSec-WebSocket-Protocol: chat\nSec-WebSocket-Version: 13\nOrigin: localhost\n\n"
        self.testsocket.sendall(message)
        wsresponse = repr(self.testsocket.recv(1024))
#         print wsresponse
        self.testsocket.send(encodeMessage("1<---->Test Message"))#This line seems to get stuck at times. Solution is to use sendAll, use \n at the end
        data = repr(self.testsocket.recv(1024))
        print data
        self.assertEqual(data, "\'Text received\'", "Text Messages is  identified and accepted")
        
    def test_wellformed_Message_for_Json(self):
        message = "GET /mychat HTTP/1.1\nHost: server.example.com\nUpgrade: websocket\nConnection: Upgrade\nSec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\nSec-WebSocket-Protocol: chat\nSec-WebSocket-Version: 13\nOrigin: localhost\n\n"
        self.testsocket.sendall(message)
        wsresponse = repr(self.testsocket.recv(1024))
        self.testsocket.send(encodeMessage("2<---->Test Message"))#This line seems to get stuck at times. Solution is to use sendAll, use \n at the end
        data = repr(self.testsocket.recv(1024))
#         print data
        self.assertEqual(data, "\'json is received\'", "json Messages is  identified and accepted")

##TO RUN THE FOLLOWING UNIT TESTS IT IS EXPECTED HAVE THE DATABASE 
##CREATED. DATABASE SCRIPT IS PROVIDED TO CREATE THE NECESSARY DATABASES AND TABLES
##ASSISCIATED DATA IS NOT PROVIDED.      
class TestDatabase(unittest.TestCase):    
    
    def setUp(self):
        self.connection = es.dbConnect()

    def tearDown(self):
        self.connection.close()
    
    def test_data_insert_data_Read(self):
        self.assertIsInstance(self.connection, MySQLdb.connection, "Database connection accurately set")
        jsondata ={"type":"image", "time":"2014.3.4_14.40.30", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4583105, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        alt = str(jsondata["position"]["alt"]);
        if alt=="None":
            alt = '0'       
        heading = '0'
        speed = '0'
        width = jsondata["vwidth"]
        height =jsondata["vheight"]
        if width > height :
            screenorientation= 1.00#landscape
        else :
            screenorientation= 0.00#potrait
        filename =  jsondata["type"]+"_"+jsondata["time"]+"."+jsondata["ext"]
        sqlstring1 = "INSERT INTO Imagedata values (\'"+filename+"\',GeomFromText ('POINT("+ str(jsondata["position"]["lat"])+" "+str(jsondata["position"]["lon"])+")'),"+str(jsondata["position"]["alt"])+","+str(jsondata["position"]["acc"])
        sqlstring2 =","+str(jsondata["device"]["gx"])+","+str(jsondata["device"]["gy"])+","+str(jsondata["device"]["gz"])
        sqlstring3 = ","+str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+","+str(screenorientation)+",\'"+jsondata["device"]["orientation"]+"\',now(),\'"+str(jsondata["deviceOS"])+"\',\'"+str(jsondata["browsertype"])+"\',\'"+str(jsondata["deviceType"])+"\');"
        sqlstring = sqlstring1 + sqlstring2+ sqlstring3
        #print(sqlstring)
        es.dbInsert(sqlstring)        
        sqlreadsting = 'select imagename, Browser,devicetype,X(location) as latitude, Y(location) as longitude  from Imagedata where time=\'2014.3.4_14.40.31\''
        result = es.dbRead(sqlreadsting)
        self.assertIsNotNone(result, "Inserted data is retrieved and it is not null")
        for row in result:
            self.assertEqual(row[0], "image_2014.3.4_14.40.30.png", "Image name is correctly set and saved")
            self.assertEqual(row[1], 65.0600797, "Latitudes are saved")
            self.assertEqual(row[2], 25.4583105, "Longitude are saved")            

        
HOST = '127.0.0.1'    # The remote host
PORT = 17322
      
class RestServerTestCase(unittest.TestCase):
     
    def setUp(self):
        self.db_fd, flaskr.app.config['DATABASE'] = tempfile.mkstemp()
        EventService.app.config['TESTING'] = True
        self.app = EventService.app.test_client()
        flaskr.init_db()
        #self.socketServer = self.app.WebSocketServer('',wsport,'127.0.0.1')        
         
    def test_rootpath(self):
        rv = self.app.get('/')
        assert 'This is a REST Service for 2D3DCapture Server.' in rv.data
         
    def test_post_image(self):
        rv = self.app.post('/postImage')
        assert 'READY' in rv.data
        
    def test_clossing_websocket(self):
        rv =self.app.post('/closewebsocketserver')
        assert 'CLOSED' or 'ALREADY_CLOSSED'  in rv.data  
        
    def test_start_websocket(self):
        rv =self.app.get('/startwebsocketserver')
#         print rv.data
        assert 'READY' in rv.data       
    
    def test_post_binary_image(self):
        rv =self.app.post('/postBinaryImage')
        assert 'READY' or '415 Unsupported Media Type' in rv.data
    
    def test_get_All_Image_Data(self):
        rv =self.app.get('/getAllImageData')
        jsonmsg = json.loads(rv.data)
        self.assertIsNotNone(jsonmsg['imageList'] , "getImageData returns a non None list")
        
    def test_get_location_Image_Data(self):
        rv =self.app.get('/getLocationImageData?lat=65.0600797&lon=25.4583105')
        jsonmsg = json.loads(rv.data)
        self.assertIsNotNone(jsonmsg['imageList'] , "getLocationImageData returns a non None list.This is a feature test for location based image data")
    
    def test_closest_Image_retrieval(self):
        jsondata1 ={"type":"image", "time":"2014.3.4_14.40.31", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4583105, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        jsondata2 ={"type":"image", "time":"2014.3.4_14.40.32", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4582115, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        jsondata3 ={"type":"image", "time":"2014.3.4_14.40.33", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4584104, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        jsondata4 ={"type":"image", "time":"2014.3.4_14.40.34", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4586115, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        jsondata5 ={"type":"image", "time":"2014.3.4_14.40.35", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4587125, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        jsondata6 ={"type":"image", "time":"2014.3.4_14.40.36", "ext":"png", "deviceType":"Mobile", "deviceOS":"Badda", "browsertype":"Firefox", "position":{"lon":25.4588125, "lat":65.0600797, "alt":-1000, "acc":48.38800048828125}, "device":{"ax":0, "ay":0, "az":0, "gx":0, "gy":0, "gz":0, "ra":210.5637, "rb":47.5657, "rg":6.9698, "orientation":"potrait"}, "vwidth":480, "vheight":800}
        es.saveData(jsondata1)
        es.saveData(jsondata2)
        es.saveData(jsondata3)
        es.saveData(jsondata4)
        es.saveData(jsondata5)
        es.saveData(jsondata6)
        radius = 0.0001              
        photoList = es.getClosestImages( 65.0601787,  25.4583107, radius  )
        self.assertEqual(len(photoList), 4, "Length of the list should be equal of the first test")
        for row in photoList:
            assert 'image_2014.3.4_14.40.32.png' or 'image_2014.3.4_14.40.31.png' in row[0] 
        photoList2 = es.getClosestImages( 65.0601787,  25.4587107, radius  )
        self.assertEqual(len(photoList2), 2, "Length of the list should be equal of the second test")
        for row in photoList2:
            assert 'image_2014.3.4_14.40.34.png' or 'image_2014.3.4_14.40.35.png' in row[0]
            
        
  
def suite():
    testsuit =unittest.TestSuite()
    testsuit.addTest(TestWebSockets('test_webSocketServerOBject'))    
    testsuit.addTest(TestWebSockets('test_valid_WS_Request'))
    testsuit.addTest(TestWebSockets('test_invalid_Messge'))
    testsuit.addTest(TestWebSockets('test_invalid_Request'))
    testsuit.addTest(TestWebSockets('test_malformed_Message'))
    testsuit.addTest(TestWebSockets('test_wellformed_Message_for_Text'))
    testsuit.addTest(TestWebSockets('test_wellformed_Message_for_Json'))
    testsuit.addTest(TestDatabase('test_data_insert_data_Read'))
    testsuit.addTest(RestServerTestCase('test_rootpath'))
    testsuit.addTest(RestServerTestCase('test_post_image'))
    testsuit.addTest(RestServerTestCase('test_start_websocket'))    
    testsuit.addTest(RestServerTestCase('test_clossing_websocket'))
    testsuit.addTest(RestServerTestCase('test_post_binary_image'))
    testsuit.addTest(RestServerTestCase('test_get_All_Image_Data'))
    testsuit.addTest(RestServerTestCase('test_closest_Image_retrieval')) 
    return testsuit

suite = suite()
runner = unittest.TextTestRunner(verbosity=3)
runner.run(suite)
        
# if __name__ == "__main__":
#     #import sys;sys.argv = ['', 'Test.testName']
#     unittest.main()