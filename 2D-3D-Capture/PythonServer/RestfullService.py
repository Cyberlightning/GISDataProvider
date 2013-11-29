'''
Created on Nov 7, 2013

@author: tharanga
'''
import re  
import struct   
from base64 import b64encode,b64decode
from hashlib import sha1
from cStringIO import StringIO
# import gevent
# from gevent import socket
import socket
from PIL import Image
import json
import sys 
import MySQLdb
import argparse
from flask import Flask ,jsonify
from flask import abort
from flask import request
import logging, math
import threading
import thread
import httplib
import urllib

wsrunning = False
tomcatport = "9090"
wsport = 17324
restport = 17323
#lhost = "localhost"
lhost = "dev.cyberlightning.com"
app = Flask(__name__)


@app.route('/')
def index():
    return "This is a REST Service for 2D3DCapture Server."

def dbRead(sqlstring):
    print("This is the database read")
    dbconn = MySQLdb.connect (host = "localhost",
                       user = "twijethilake",
                       passwd = "twj1672$1",
                       db = "2d3dcapture")
    cursor = dbconn.cursor()    
    logging.debug("sql String %s" , sqlstring)
    cursor.execute (sqlstring)
    results = cursor.fetchall()
    cursor.close ()
    dbconn.close ()
    return results;

#http://www.platoscave.net/blog/2009/oct/5/calculate-distance-latitude-longitude-python/
def distance_calc(origin, destination):
    lat1, lon1 = origin
    lat2, lon2 = destination
    radius = 6371000

    dlat = math.radians(lat2-lat1)
    dlon = math.radians(lon2-lon1)
    a = math.sin(dlat/2) * math.sin(dlat/2) + math.cos(math.radians(lat1)) \
        * math.cos(math.radians(lat2)) * math.sin(dlon/2) * math.sin(dlon/2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    d = radius * c
    return d


@app.route('/postImage', methods = ['POST'])
def startWSServer():
    logging.debug("POST CALLED")
    global wsrunning
    if wsrunning== False :
        logging.debug("Running the Server")
        wsthread = WebSocketServer('',wsport,'127.0.0.1')
        wsthread.start()
        wsrunning = True
    elif wsrunning ==True :
        logging.debug("Server is Running")
    return "READY" 

def saveData(jsondata):
    dbconn = MySQLdb.connect (host = "localhost",
                            user = "twijethilake",
                            passwd = "twj1672$1",
                            db = "2d3dcapture")
    cursor = dbconn.cursor ()
    filename = jsondata["type"]+"_"+jsondata["time"]+"."+jsondata["ext"]
    heading = str(jsondata["device"]["ax"]);
    width = jsondata["vwidth"]
    height =jsondata["vheight"]
    if width > height :
        screenorientation= 1.00#landscape
    else :
        screenorientation= 0.00#potrait
    alt = str(jsondata["position"]["alt"]);
    if alt=="None":
        alt = '0'
#        heading = str(jsondata["motion"]["heading"])        
#        if heading=="None":
    heading = '0'
 #       speed = str(jsondata["motion"]["speed"])    
  #      if speed=="None":
    speed = '0'
    sqlstring1 = "INSERT INTO Imagedata values (\'"+filename+"\',GeomFromText ('POINT("+ str(jsondata["position"]["lat"])+" "+str(jsondata["position"]["lon"])+")'),"+alt+","+str(jsondata["position"]["acc"])
    sqlstring2 =","+str(jsondata["device"]["gx"])+","+str(jsondata["device"]["gy"])+","+str(jsondata["device"]["gz"])
    sqlstring3 = ","+str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+","+str(screenorientation)+",\'"+jsondata["device"]["orientation"]+"\',now(),\'"+str(jsondata["devicetype"])+"\',\'"+str(jsondata["browsertype"])+"\');"
    sqlstring = sqlstring1 + sqlstring2+ sqlstring3
    logging.debug( "sql String %s" , sqlstring)
    try:
        cursor.execute (sqlstring)
        dbconn.commit()
    except MySQLdb.Error,e :
        dbconn.rollback()
        logging.critical("Error %d: %s" % (e.args[0],e.args[1]))
        cursor.close ()
        dbconn.close ()
        logging.info( "Database transaction completed")

@app.route('/postBinaryImage', methods = ['POST'])
def saveBinaryImage():
    logging.debug("POST BINARY CALLED")
    if request.headers['Content-Type'] == 'binary/octet-stream':        
    	logging.info("Post Request Arrived. Content length %s"%(request.headers['Content-Length']))      
        payload = request.data
        jsonlen = payload[0:3]
        logging.debug("Tag Length %s"%(jsonlen))
        headerlen = int(jsonlen)+3
        logging.debug(headerlen)
        logging.debug("**************************")
        metadata = payload[3:headerlen]
        jsonmetadata = json.loads(metadata)        
        try:
            logging.debug("%s : %s"%(jsonmetadata["vwidth"],jsonmetadata["vheight"]))
            size = (int(jsonmetadata["vwidth"]) ,int(jsonmetadata["vheight"]))
            img = Image.frombuffer('RGBA', size, payload[headerlen:],'raw','RGBA',0,1)
            filename= jsonmetadata["type"]+"_"+jsonmetadata["time"]+"_temp."+jsonmetadata["ext"]
            img.save("../public_html/images/%s"%(filename))
            saveData(jsonmetadata);
            logging.info('Sending the tagging Request..!')
            headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}	
            tagconn = httplib.HTTPConnection("localhost:%s"%(tomcatport))
            print jsonmetadata
            params = urllib.urlencode({'data': metadata})
            print params
            tagconn.request("POST", "/RestClient/ImageTaggingServlet", params, headers)
            response = tagconn.getresponse()
            print response.status, response.reason
            data = response.read()
            logging.debug(data)
        except RuntimeError :
            logging.critical("Error %d: %s" % (e.args[0],e.args[1]))
            return "415 Unsupported Media Type "
        
        return "READY"
    else:
        return "415 Unsupported Media Type "

@app.route('/getLocationImageData', methods= ['GET'])
def getImageLinks():    
    if not request.json or not 'lon' in request.json or not 'lat' in request.json :
        abort(400)
#    sqlstring = "imagename,X(location) as latitude, Y(location) as longitude, rotationalpha, rotationbeta, rotationgamma, screenorientation, deviceorientation, Browser,accelerationgx,accelerationgy,accelerationgz FROM Imagedata WHERE "
#     maxlat = request.json['lat']+0.0001
#     minlat = request.json['lat']-0.0001
#     maxlon = request.json['lon']+0.0001
#     minlon = request.json['lon']-0.0001
    radius = 0.0001 
    photoList = getClosestImages(request.json['lat'],request.json['lon'],radius)
#     origin = (request.json['lat'] , request.json['lon'])
#     sqlstring =  "select imagename ,x(location) as latitude, y(location) as longitude  from Imagedata where (%f>x(location) AND x(location)>%f) AND (%f> y(location) AND y(location)> %f)"%(maxlat,minlat,maxlon,minlon);
#     print sqlstring
#     print origin
#     print request.json['lon']-0.0001
#     print request.json['lat']-0.0001
#     locationList = dbRead(sqlstring)  
#     if not locationList:
#         logging.debug("No match Found")
#         return "NONE"
#     else :        
#         for row in locationList:
#             imagename = row[2]
#             print "Imagename=%s"%(imagename)
#             destination = (row[1],row[2])
#             print destination
#             distance = distance_calc(origin,destination)
#             url = "http://dev.cyberlightning.com/~twijethilake/images/%s"%(row[0])
#             imagedata = {'imagename' : row[0] ,'latitude'  : row[1] , 'longitude'  : row[2] , 'distance'  : distance , 'url' :url}
#             print imagedata
#             photoList.append(imagedata)
    return jsonify(imageList=photoList),200

def getClosestImages(lat, lon, radius):
    photoList = []
    origin = (lat, lon)
    maxlat = lat+radius
    minlat = lat-radius
    maxlon = lon+radius
    minlon = lon-radius
    sqlstring =  "select imagename ,x(location) as latitude, y(location) as longitude  from Imagedata where (%f>x(location) AND x(location)>%f) AND (%f> y(location) AND y(location)> %f)"%(maxlat,minlat,maxlon,minlon);
    locationList = dbRead(sqlstring)
    if not locationList:
        if radius >= 0.01 :
            abort(400)
        else :
            radius = radius +0.0001
            getClosestImages(lat, lon, radius)        
    else :
        for row in locationList:
            imagename = row[2]
            destination = (row[1],row[2])
            distance = distance_calc(origin,destination)
            url = "http://dev.cyberlightning.com/~twijethilake/images/%s"%(row[0])
            imagedata = {'imagename' : row[0] ,'latitude'  : row[1] , 'longitude'  : row[2] , 'distance'  : distance , 'url' :url}
            logging.debug( imagedata)
            photoList.append(imagedata)
    return photoList
            
@app.route('/getAllImageData' , methods= ['GET'])
def markPhotos():
    sqlstring = "SELECT imagename,X(location) as latitude, Y(location) as longitude,rotationalpha, rotationbeta, rotationgamma,screenorientation, deviceorientation, Browser,accelerationgx,accelerationgy,accelerationgz FROM Imagedata"    
    gpsList = dbRead(sqlstring);
    photoList = []
    if not gpsList:
        logging.debug("No match Found")
    else :
        for row in gpsList:
            deviceorientation = row[7]
            tempvar = row[6]
            browser = row[8]
            gx = row[9]
            gy = row[10]
            gz = row[11]
            pitch = 0.00 #beta
            roll = 0.00 #gamma
            yaw = 0.00  #alpha
            if tempvar == 0 :
                screenorientation = "potrait"
            elif tempvar == 1 :
                screenorientation = "landscape"
            pitch = -1 * row[4] #beta x
            roll = row[5] #gamma y
            yaw = 360 - row[3]  #alpha z                                   
            url = "http://dev.cyberlightning.com/~twijethilake/images/%s"%(row[0])
            imagedata = {'imagename' : row[0], 'pitch' : pitch,'roll' : roll , 'yaw' : yaw ,'latitude' : row[1] , 'longitude' :row[2] , 'url' : url , 'alpha' :row[3] ,'beta' : row[4],'gamma' : row[5] , 'deviceorientation' : row[7]  }            
            photoList.append(json.dumps(imagedata))
       # print photoList
            #logging.debug("Imagename=%s : Latitude=%s : Longitude : %s"%(imagename,row[0],row[1]))            
    return jsonify(imageList=photoList)

class RequestHandler(threading.Thread): 
    
    def __init__(self,conn, addr):
        threading.Thread.__init__(self)
        self.conn = conn
        self.addr = addr
        self.response_header =('HTTP/1.1 101 Switching Protocols','Upgrade: websocket','Connection: Upgrade','Sec-WebSocket-Accept: {key}\r\n\r\n',)
        self.magic_string = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
    
    def encodeMessage( self ,message):
        message = b64encode(message)
        b1 =0x80 | 0x1 & 0x0f
        payload_len = len(message)
        header = struct.pack('>BB', b1, payload_len)
        message= header +message
        return message
    
    def saveData(self ,jsondata):
        dbconn = MySQLdb.connect (host = "localhost",
                            user = "twijethilake",
                            passwd = "twj1672$1",
                            db = "2d3dcapture")
        cursor = dbconn.cursor ()
        filename = jsondata["type"]+"_"+jsondata["time"]+"."+jsondata["ext"]
        heading = str(jsondata["device"]["ax"]);
        width = jsondata["vwidth"] 
        height =jsondata["vheight"]
        if width > height :
            screenorientation= 1.00#landscape
        else :
            screenorientation= 0.00#potrait
        alt = str(jsondata["position"]["alt"]);
        if alt=="None":
            alt = '0';
#        heading = str(jsondata["motion"]["heading"])        
#        if heading=="None":
        heading = '0';
 #       speed = str(jsondata["motion"]["speed"])    
  #      if speed=="None":
        speed = '0';
        sqlstring1 = "INSERT INTO Imagedata values (\'"+filename+"\',GeomFromText ('POINT("+ str(jsondata["position"]["lat"])+" "+str(jsondata["position"]["lon"])+")'),"+alt+","+str(jsondata["position"]["acc"])
        sqlstring2 =","+str(jsondata["device"]["gx"])+","+str(jsondata["device"]["gy"])+","+str(jsondata["device"]["gz"])
        sqlstring3 = ","+str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+","+str(screenorientation)+",\'"+jsondata["device"]["orientation"]+"\',now(),\'"+str(jsondata["devicetype"])+"\',\'"+str(jsondata["browsertype"])+"\');"
        sqlstring = sqlstring1 + sqlstring2+ sqlstring3    
        logging.debug( "sql String %s" , sqlstring)
        try:
            cursor.execute (sqlstring)
            dbconn.commit()
        except MySQLdb.Error,e :
            dbconn.rollback()
            logging.critical("Error %d: %s" % (e.args[0],e.args[1]))      
        cursor.close ()
        dbconn.close ()
        logging.info( "Database transaction completed")
        
    def run(self):
        logging.debug("This might work")
        processConnection(self.conn, self.addr)
        
        
    def handShakeResponse(self,key):
        key = key.groups()[0].strip()
        if key is not None:
            response_key = b64encode(sha1(key + self.magic_string).digest())
            response = '\r\n'.join(self.response_header).format(key=response_key)
            logging.debug('##################################### \n %s \n %s \n #####################################',response,len(response))
        return response
            
                        
    def processConnection( self , conn, addr):
        filename= ""
        BUFFER = ""
        done=""             
        logging.info(  'Connected with %s : %s',  addr[0] , str(addr[1]))
        while True:
            try:
                data = conn.recv(4096)
                BUFFER += data   
                buf = BUFFER  
                logging.debug("recived data length %s",len(data))               
                if len(data) == 0:
                    return            
                key = re.search('Sec-WebSocket-Key:\s+(.*?)[\n\r]+', data)
                if key is not None:
                    response = self.handShakeResponse(key)
                    conn.send(response)
                    message = "SERVER_READY"
                    conn.send(self.encodeMessage(message))
                    key = ""
                    BUFFER = ""                
                else :
                    payload_start = 2
                    b1 = ord(buf[0])
                    fin = b1 & 0x80
                    opcode = b1 & 0x0f
                    b2 = ord(buf[1])
                    mask = b2 & 0x80 
                    length = b2 & 0x7f
                    logging.debug("OPCODE %s",opcode)
                    logging.debug("Length %s", length)                
                    logging.debug("FIN %s", fin)
                    if len(buf) < payload_start + 4:
                        return
                    elif length == 126:
                        length, = struct.unpack_from('>xxH', buf)
                        payload_start += 2
                    elif length == 127:
                        length, = struct.unpack_from('>xxQ', buf)
                        payload_start += 8
                    logging.debug ("LENGTH OF THE PAYLOAD :%s",length)        
                    if mask:
                        mask_bytes = [ord(b) for b in buf[payload_start:payload_start + 4]]
                        payload_start += 4
                    full_len = payload_start + length
                    logging.debug("FULL LENGTH :%s", full_len)
                
                    if len(buf) < full_len:
                        sys.stdout.write('-')
                    else:
                        sys.stdout.write('-|\n')                                
                        payload = buf[payload_start:full_len]
                        if mask:
                            unmasked = [mask_bytes[i % 4] ^ ord(b) for b, i in zip(payload, range(len(payload)))]
                            payload = "".join([chr(c) for c in unmasked])
                                        
                            if opcode == 1:
                                if done=="":                                
                                    s = payload.decode("UTF8")
                                    logging.info("Decodded payleoad :%s", s)
                                    jsonmetadata = json.loads(s)
                                    filename= jsonmetadata["type"]+"_"+jsonmetadata["time"]+"_temp."+jsonmetadata["ext"]
                                    logging.debug("File name %s", filename)
                                    logging.debug("Position ->logitude %s ", str(jsonmetadata["position"]["lon"]))
                                    message= "FILENAME"                            
                                    sent = conn.send(self.encodeMessage(message))                           
                                    logging.debug("Message %s sent.Length %s", message, sent)
                                    BUFFER=""
                                    buf= ""                                
                                    done="true"
                                else :
                                    splitplace=payload.find(",")
                                    payload= payload[splitplace+1:]
                                    logging.debug("printing Payload %s", payload)
                                    try :
                                        payload = b64decode(payload)
                                        file_like = StringIO(payload)
                                        i = Image.open(file_like)
                                        i.save("../public_html/images/%s"%(filename))
                                        self.saveData(jsonmetadata);
                                    except RuntimeError as e:
                                        conn.close();
                                        logging.critical( "Runtime error in decoding bnary system exit %s", e.strerror)                                    
                                        sys.exit(0)
                                    conn.send(self.encodeMessage("SAVED"))                               
                                    conn.close()
                                    BUFFER=""
                                    buf= ""
                                    filename= ""
                                    jsonmetadata =""
                                    done= ""
                                    break 
                            elif opcode == 2:                        
                                logging.debug('Handling binary data' )
    	                        #logging.debug(payload[0:3])
       				jsonlen = payload[0:3]
       				logging.debug(jsonlen)
       				headerlen = int(jsonlen)+3
       				logging.debug(headerlen)
       				logging.debug("**************************")
				metadata = payload[3:headerlen]
       				jsonmetadata = json.loads(metadata)
       				logging.debug(jsonmetadata["vwidth"])
	                        logging.debug(jsonmetadata["vheight"])
		                size = (int(jsonmetadata["vwidth"]) ,int(jsonmetadata["vheight"]))
               			img = Image.frombuffer('RGBA', size, payload[headerlen:],'raw','RGBA',0,1)
		                filename= jsonmetadata["type"]+"_"+jsonmetadata["time"]+"_temp."+jsonmetadata["ext"]
		                img.save("../public_html/images/%s"%(filename))
				try :
				    self.saveData(jsonmetadata);
			            headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}
			            tagconn = httplib.HTTPConnection("localhost:%s"%(tomcatport))
				    print jsonmetadata
				    params = urllib.urlencode({'data': metadata})
	                	    print params
		                    tagconn.request("POST", "/RestClient/ImageTaggingServlet", params, headers)
			            response = tagconn.getresponse()
			            print response.status, response.reason
		        	    data = response.read()
		                    logging.debug(data)
                                    conn.send(self.encodeMessage("SAVED"))
                                    logging.debug('Closing the connection')
                                    conn.close()
			            BUFFER=""
			            buf= ""
			            filename = ""
		        	    jsonmetadata =""
		                    done= ""
			            return
				except :
			            logging.critical("Error ")
				    conn.send(self.encodeMessage("ERROR"))
                                    logging.debug('Closing the connection')
                                    conn.close()
            			    return
            except KeyboardInterrupt :
                conn.close();
                logging.critical(" keyboard Interuption. Program exiting.")
                sys.exit(0) 
            

class WebSocketServer(threading.Thread) :      
  
    
    def frameHanlder(self)  :
        print 'this is not implemented yet'
        
       
    def closeConnection(self):
        sock.close()
                 
    def __init__(self, s, p, l):
        threading.Thread.__init__(self)
        self.SERVER = s
        self.PORT = p
        self.LOCALHOST = l    
#         SERVER = ''
#         PORT = wsport
#         LOCALHOST = "127.0.0.1"
    def setRunning(self, value):
        self.running = value

    def run(self):
        try:
            sock= socket.socket()
            sock.bind((self.SERVER,self.PORT))
            sock.listen(500)
            logging.info("Web Socket server lunched%s : %s"%(self.SERVER,self.PORT))
            while True:
            #wait to accept a connection - blocking call
                logging.info("Request handler lunched 1" )
                conn, addr = sock.accept()
                logging.info("Request handler lunched 2")
                rh = RequestHandler(conn, addr)
                thread.start_new_thread(rh.processConnection, (conn, addr))
                
#                 gevent.spawn(self.processConnection, conn, addr)
        except socket.error as message:
            print 'Bind failed. Error Code : '  + str(message[0]) + ' Message ' + message[1]
            
def setup_logging():
    parser = argparse.ArgumentParser(description='Log level')
    parser.add_argument('--log', help='Setting this would set the log level. Values DEBUG/INFO/WARNING/ERROR/CRITICAL')
    
    args = parser.parse_args()    
    if args.log:
        loglevel = args.log.upper()
        if(loglevel =="DEBUG" or loglevel == "INFO" or loglevel == "WARNING" or loglevel == "ERROR" or loglevel == "CRITICAL"):
            print "Set Log level to ", loglevel
            loglevel_int=getattr(logging, loglevel.upper())
            logging.basicConfig(level=loglevel_int) 
        else :
            print "Invalid Log level. Setting it to default  value INFO."
            logging.basicConfig(level=logging.INFO)        
    else :
        print "Debug Level set to INFO" 
        logging.basicConfig(level=logging.INFO) 
    fh = logging.FileHandler('rest_service.log')
    fh.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    fh.setFormatter(formatter)
    logging.getLogger('').addHandler(fh)          

def main():
    setup_logging()
    logging.info("Logging Set up")
    app.run(host= lhost, port = restport , debug = True)
    logging.info("Rest Server Running")      
        
if __name__ == "__main__":
    main()
