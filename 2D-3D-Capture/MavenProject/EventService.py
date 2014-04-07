'''
Created on Jan 21, 2014

@author: tharanga
'''
#for pbsub server

import logging,math
import MySQLdb
import argparse
from flask import Flask,jsonify
from flask import abort
from flask import request
import threading
from base64 import b64encode,b64decode
import struct
from hashlib import sha1
import re
from PIL import Image
import json
import sys
import traceback
import thread
import socket
import httplib
import urllib
from datetime import timedelta
from flask import make_response, request, current_app
from functools import update_wrapper
import ConfigParser as cp
# import time

tomcatport = "9090"
wsport = 17322
restport = 17321
lhost = "localhost"
serverurl="dev.cyberlightning.com"
repo_url ="http://dev.cyberlightning.com/~twijethilake/images"
dbuser = "twijethilake"
dbpassword = "twj1672$1"
dbname = "2d3dcapture"
local_image_repo="../public_html/images"

def crossdomain(origin=None, methods=None, headers=None,
                max_age=21600, attach_to_all=True,
                automatic_options=True):
    if methods is not None:
        methods = ', '.join(sorted(x.upper() for x in methods))
    if headers is not None and not isinstance(headers, basestring):
        headers = ', '.join(x.upper() for x in headers)
    if not isinstance(origin, basestring):
        origin = ', '.join(origin)
    if isinstance(max_age, timedelta):
        max_age = max_age.total_seconds()

    def get_methods():
        if methods is not None:
            return methods

        options_resp = current_app.make_default_options_response()
        return options_resp.headers['allow']

    def decorator(f):
        def wrapped_function(*args, **kwargs):
            if automatic_options and request.method == 'OPTIONS':
                resp = current_app.make_default_options_response()
            else:
                resp = make_response(f(*args, **kwargs))
            if not attach_to_all and request.method != 'OPTIONS':
                return resp

            h = resp.headers

            h['Access-Control-Allow-Origin'] = origin
            h['Access-Control-Allow-Methods'] = get_methods()
            h['Access-Control-Max-Age'] = str(max_age)
            if headers is not None:
                h['Access-Control-Allow-Headers'] = headers
            return resp

        f.provide_automatic_options = False
        return update_wrapper(wrapped_function, f)
    return decorator

def dbConnect():
    dbconn = MySQLdb.connect (host = "localhost", user = dbuser, passwd = dbpassword, db = dbname)
    return dbconn;

def dbInsert(sqlString):
    logging.debug("Inserting messages to database")
    dbconn = dbConnect();    
    cursor = dbconn.cursor ()
    try:
        cursor.execute (sqlString)
        dbconn.commit()
    except MySQLdb.Error,e :
        dbconn.rollback()
        logging.critical("Error %d: %s" % (e.args[0],e.args[1]))
        cursor.close ()
        dbconn.close ()
        logging.info( "Database transaction completed")
        
def dbRead(sqlstring):
    logging.debug("This is the database read")
    dbconn = dbConnect();
    cursor = dbconn.cursor()    
    logging.debug("sql String %s" , sqlstring)
    cursor.execute (sqlstring)
    results = cursor.fetchall()
    cursor.close ()
    dbconn.close ()
    return results;

def dbDelete(sqlstring):
    logging.debug("Deleting info form the database")    
    dbconn = dbConnect();
    cursor = dbconn.cursor()
    
    logging.debug("sql String %s" , sqlstring)
    cursor.execute (sqlstring)
    dbconn.commit()
    cursor.close ()
    dbconn.close ()

def encodeMessage(message):
    logging.debug("Encoding The message ")        
    msg = b64encode(message)
    b1 = 0x80 | 0x1 & 0x0f
    b2 = 0
    header=""
    payload_len = len(msg)
    logging.debug("Encoding The message %d"%(payload_len))
    if payload_len < 126 :
        header = struct.pack('>BB', b1, payload_len)
        message= header +msg
    elif (payload_len < ((2 ** 16) - 1)):
        b2 |= 126
        header += chr(b1)
        header += chr(b2)
        l = struct.pack(">H", payload_len)
        header += l
        message = header +msg
    else:            
        b2 |= 127
        header += chr(b1)
        header += chr(b2)
        l = struct.pack(">Q", payload_len)
        header += l
        message = header +msg
    logging.debug(message)
    return message
    
class EventManager:
    
    def __init__(self):
        logging.debug("Class initialized")
        self.connections = {}

    def dbInsert(self , sqlString):
        sub_id = None
        dbconn = dbConnect();
        cursor = dbconn.cursor ()
        try:
            cursor.execute (sqlString)
            dbconn.commit()
            cursor.execute ("SELECT LAST_INSERT_ID();")
            result  = cursor.fetchall()
            if not result :
                dbconn.rollback()
                logging.critical("Unable to retrieve id of the last entered subscription. Error returned")
                cursor.close ()
                dbconn.close ()
                logging.info( "Database transaction completed")
            else :
                sub_id = result[0][0]
                logging.info(sub_id )
                #                logging.info(row[0])
        except MySQLdb.Error,e :
            dbconn.rollback()
            logging.critical("Error %d: %s" % (e.args[0],e.args[1]))
            cursor.close ()
            dbconn.close ()
            logging.info( "Database transaction completed")
        return sub_id
    
    def subscribe_public_agents(self, jsondata , wsconnection):
        logging.debug("Analyse subscription data ")        
        subscriptiontype = 0;        
        if(jsondata["type"]=="Any"):
            subscriptiontype=1
        elif (jsondata["type"]=="Local"):
            subscriptiontype=2
        elif (jsondata["type"]=="Local-Directional"):
            subscriptiontype=3
        elif (jsondata["type"]=="Directional"):
            subscriptiontype=4
        subscribe_sql =  'INSERT INTO Subscriptions(subscriptiontype) values (%s);'%(subscriptiontype)       
        sub_id =self.dbInsert(subscribe_sql)
        logging.debug("%s  %s"%(subscriptiontype,sub_id))
        if (subscriptiontype == 2 or subscriptiontype == 3 or subscriptiontype == 4):
            add_subcsribe_data_sql = "INSERT INTO SubscriptionData (subid, location,pitch, yaw, roll) values (%s,GeomFromText('POINT(%s %s)'),'%s','%s','%s');"%(sub_id,jsondata["data"]["lon"],jsondata["data"]["lat"],jsondata["data"]["pitch"],jsondata["data"]["yaw"],jsondata["data"]["roll"])
            logging.debug(add_subcsribe_data_sql)
            self.dbInsert(add_subcsribe_data_sql)
        self.connections[sub_id] = wsconnection
#         sent = wsconnection.send(encodeMessage("This is a Miracle that works or might not"))
#         sent = wsconnection.send(encodeMessage("This is a Miracle that works or might notKJHHFIWHEJKBHFKJASKLHASKDBHKAJSDIHAEjihekrtjhwihrfijsefklshjhn    KJHWEFHJWEIORJHJWHERIWEJHIORJWEKFBWKLH"))
        sent = wsconnection.send(encodeMessage("{ \"type\": 1, \"subscribe\" : \"True\", \"sub_id\" : %s}"%(sub_id)))
        logging.debug("sent %s"%(sent))
        if sent == 0:
            raise RuntimeError("socket connection broken")
        return sub_id        
    
    def eventBroadcast(self,event):
        logging.debug("Publishing Event")
        conns = self.connections.viewvalues()
        logging.debug("Broad casting to %s client"%(len(conns)))
        for conn in conns:
            conn.send(encodeMessage(event))
            
    def unsubscribe(self, sub_id):
        logging.debug("Unsubscribing %s"%(sub_id))
        try:
            sqlString = "delete from Subscriptions where subid=%s"%(sub_id)
            dbDelete(sqlString);
            conn = self.connections.pop(sub_id)
            conn.send(encodeMessage("{ \"type\" : 2 }"))
            conn.close()
        except KeyError :
            pass
       
    def sendSingleEvent(self, sub_id,event):
        logging.debug("Sending a single Event")
        conn = self.connections[sub_id]
        conn.send(encodeMessage(event))
        
    def handleNewContent(self, jsondata):
        logging.debug("New Image uploaded")
        sqlString = "select subid from Subscriptions where subscriptiontype = 1"
        results = dbRead(sqlString);
        logging.debug(len(self.connections))
        keys = self.connections.keys()
        for key in keys:
            logging.debug("key %s"%(key));
        for row in results:
            #logging.debug(row[0])            
            if(self.connections.has_key(row[0])):           
                conn = self.connections.get(row[0])
                filename = jsondata["type"]+"_"+jsondata["time"]+"."+jsondata["ext"]
    #             message ="{ \"type\" : 3, data : { \"url\" : \"%s/%s\" , \"alpha\" : %s , \"beta\" :%s , \"gamma\" :%s, \"lat\" :%s , \"long\" :%s }}"%(repo_url,filename,jsondata["device"]["ra"],jsondata["device"]["rb"],jsondata["device"]["rg"],["position"]["lat"],jsondata["position"]["lon"])
                message ="{ \"type\" : 3, \"url\" : \"%s/%s\", \"alpha\" : %s , \"beta\" :%s , \"gamma\" :%s, \"lat\" :%s , \"long\" :%s, \"height\" : %s , \"width\": %s} "%(repo_url, filename, str(jsondata["device"]["ra"]), str(jsondata["device"]["rb"]), str(jsondata["device"]["rg"]),str(jsondata["position"]["lat"]), str(jsondata["position"]["lon"]),str(jsondata["vheight"]),str(jsondata["vwidth"]))
                logging.debug(message) 
                conn.send(encodeMessage(message))                      
        sqlString = "select * from Imagedata where "
        
#===========WEB SOCKET SERVER=====

class RequestHandler(threading.Thread):
    
    def __init__(self,eventManager):
        threading.Thread.__init__(self)
        self.response_header =('HTTP/1.1 101 Switching Protocols','Upgrade: websocket','Connection: Upgrade','Sec-WebSocket-Accept: {key}\r\n\r\n',)
        self.magic_string = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        self.WS_Accept = False
        self.eMan = eventManager
        
    def run(self):
        logging.debug("This might work")
        self.processConnection(self.conn, self.addr)        
        
    def handShakeResponse(self,key):
        if key is not None:
            response_key = b64encode(sha1(key + self.magic_string).digest())
            response = '\r\n'.join(self.response_header).format(key=response_key)
            logging.debug('##################################### \n %s \n  #####################################',response)
        return response
    
    def shakeHand(self,data):
        logging.info("Parameters are ")
        key = re.search('Sec-WebSocket-Key:\s+(.*?)[\n\r]+', data)
        if key != None:
            key = key.groups()[0].strip()
        else : 
            return False        
        logging.debug("Key Found %s"%(key))
        upgrade = re.search('Upgrade:\s+(.*?)[\n\r]', data)
        if upgrade != None:
            upgrade = upgrade.groups()[0].strip()
        else : 
            return False
        logging.debug("Upgrade Found %s"%(upgrade))
        version =re.search('Sec-WebSocket-Version:\s+(.*?)[\n\r]', data)
        if version != None:
            version = version.groups()[0].strip()
        else : 
            return False
        logging.debug("Sec-WebSocket-Version Found %s"%(version))
        host = re.search('Host:\s+(.*?)[\n\r]', data)
        if host != None:
            host = host.groups()[0].strip()
        else : 
            return False
        logging.debug("Host Found %s"%(host))
        if key is not None  and version is not None and host is not None and upgrade is not None and upgrade == 'websocket':
            self.WS_Accept = True
            response = self.handShakeResponse(key)
            self.conn.send(response)
            return True
        else :
            logging.info("CONNECTION REJECTED")            
            return False
            
    def sendMessage(self, message):
        self.conn.send(encodeMessage(message))
        
    def messageReader( self ,conn, addr):
        self.conn = conn
        self.addr = addr
        self.buffer = ''
        logging.info(  'Connected with %s : %s',  self.addr[0] , str(self.addr[1]))
        run = True
        while run:
            try:
            #WEB SOCKET HANDSHAKE MESSAGE IS LESS THAN 4096. HENCE IT IS POSIBLE TO USE DATA DIRECTLY.                 
                data = self.conn.recv(4096)      
                logging.debug("recived data length %s",len(data))
                #logging.debug("%s",data)
                if len(data) == 0:
                    return
                if self.WS_Accept == False :
                    logging.info('Shakehand procedure initiated...')
                    self.shakeHand(data)
                    if self.WS_Accept == False :
                        logging.info('CONNECTION_REJECTED')
                        self.conn.send("CONNECTION_REJECTED")
                        self.conn.close()
                    else :
                        logging.debug("Server communication initiated")
                        message = "READY"
                        self.conn.send(encodeMessage(message))
                elif (self.WS_Accept==True):
                    logging.info('Reading data from accepted connection.. %s'%(len(data)))
                    self.buffer += data;
                    data = ""                              
                    #== THIS PART DECODES THE WEB SOCKET PACKETS.==========
                    completeMessageNotRead = True
                    while completeMessageNotRead : 
                        payload_start = 2
                        length, opcode , mask, fin  = self.processWSHeader();
                        logging.debug("OPCODE %s",opcode)
                        logging.debug("Length %s", length)                
                        logging.debug("FIN %s", fin)
                        logging.debug("Buffer Length %s"%len(self.buffer))                        
                        if len(self.buffer) < payload_start + 4:
                            break
                        elif length == 126:
                            length, = struct.unpack_from('>xxH', self.buffer)
                            payload_start += 2
                            logging.debug ("LENGTH OF THE PAYLOAD 126:%s",length)
                        elif length == 127:
                            length, = struct.unpack_from('>xxQ', self.buffer)
                            payload_start += 8
                            logging.debug ("LENGTH OF THE PAYLOAD 127:%s",length)        
                        if mask:
                            mask_bytes = [ord(b) for b in self.buffer[payload_start:payload_start + 4]]
                            payload_start += 4
                        full_len = payload_start + length
                        logging.debug("FULL LENGTH :%s , Buffer length %s "%(full_len,len(self.buffer)))
                        #logging.info("received %s % "%((len(self.buffer)/full_len)*100))
                        #IF THE BUFFER CONSISTS OF ONE FULL MESSAGE AND ONE ONLY               
                        if len(self.buffer) == full_len:
                            logging.debug("Full Message received")                                               
                            payload = self.buffer[payload_start:full_len]
                            self.buffer ='';
                            if(opcode == 1 or opcode == 2) :
                                logging.debug("Binary or text recieced")
                            elif opcode == 8 :
                                logging.debug("Connection closed")
                                self.conn.send("Connection closed")
                                self.conn.close()
                                self.WS_Accept = False
                                break
                            elif opcode > 15:
                                logging.debug("Un expected OPCODE")
                                self.conn.send("Un expected opcode")
                                self.conn.close()
                                self.WS_Accept = False
                                break                          
                            if mask:
                                logging.debug("unmasking")           
                                unmasked = [mask_bytes[i % 4] ^ ord(b) for b, i in zip(payload, range(len(payload)))]
                                payload = "".join([chr(c) for c in unmasked])                            
                                datatype = self.processMessageBody(payload, opcode)
                                if(datatype=="image"):
                                    run = False
                            else :
                                datatype = self.processMessageBody(payload, opcode)
                            completeMessageNotRead = False                            
                        #IF THE BUFFER CONSISTS OF ONE FULL MESSAGE AND ANOTHER PARTIAL MESSAGE
                        elif len(self.buffer) > full_len:
                            logging.debug("More than One message is received")                                             
                            payload = self.buffer[payload_start:full_len]                        
                            rest= self.buffer[full_len:]
                            self.buffer = rest
                            if mask:
                                unmasked = [mask_bytes[i % 4] ^ ord(b) for b, i in zip(payload, range(len(payload)))]
                                payload = "".join([chr(c) for c in unmasked])                            
                                datatype = self.processMessageBody(payload, opcode)
                                if(datatype=="image"):
                                    run = False
                        #IF THE BUFFER CONSISTS OF A PARTIAL MESSAGE ONLY
                        elif  len(self.buffer) < full_len:
                            logging.debug("Partial Message Received")    
                            completeMessageNotRead = False
                            break
                        data = ""
            except KeyboardInterrupt :
                self.conn.close()
                logging.critical(" keyboard Interruption. Program exiting.")                
                sys.exit(0)
            except :                
                pass
    
    #PROCESSING MAJOR ELEMENTS TO EXTRACT 
    #LENGTH AND THE CONTENT TYPE                    
    def processWSHeader(self):
        logging.info("Processing Header")
        b1 = ord(self.buffer[0])
        fin = b1 & 0x80
        logging.debug("FIN %s"%(fin))
        opcode = b1 & 0x0f
        b2 = ord(self.buffer[1])
        mask = b2 & 0x80 
        length = b2 & 0x7f   
        return length, opcode , mask, fin
                        
    #PROCESSING WEBSOCKET MESSAGE BODY
    def processMessageBody(self , payload , opcode):        
        logging.debug("Opcode %d"%(opcode))        
        if opcode == 1:
        #FOCUSING ON TEXT MESSAGING
            logging.debug("Handling Text values %s"%payload)
            temp = re.search("<---->",payload)
            if temp :
                logging.debug("Unencoded Message accepted %s"%temp.group())
                start, end = temp.start(), temp.end()
                logging.debug(" %d %d"%(start, end))
                self.processPayload( payload[end:] ,payload[0:start])
            else :
                messagebody = payload.decode("UTF8")
                self.messagebody = b64decode(self.messagebody)                
                logging.debug("Decodded payleoad :%s", messagebody)
                temp = re.search("<---->",messagebody)
                if temp  :
                    logging.debug("Encoded Message accepted %d"%(temp))
#                 else:
#                     logging.debug("PUblsish type")
#                     if(messagebody =="publish") :
#                         self.eMan.eventBroadcast()
                else :
                    logging.debug("MISFORMATED MESSAGE")
                    self.conn.send(encodeMessage("MISFORMATED MESSAGE"))
                    self.conn.close()
                    return            
            # 1 Text
            # 2 Json
            # 3 image
            temp = messagebody.find("<---->")
            logging.debug("Header length %d"%temp)
            messageTypeID= messagebody[0:temp]
            bfmessageBody = messagebody[len("<---->")+1:]
            logging.debug("Json Message %s"%bfmessageBody)
            if (messageTypeID != -1):
                logging.debug("Message Type ID %s"%messageTypeID)
            else :
                self.conn.send(encodeMessage("INVALID FORMAT"))
            logging.debug("Message Type ID %s"%messageTypeID)                            
            return "Text"
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
            logging.debug("Video resolution %d : %d"%(jsonmetadata["vwidth"],jsonmetadata["vheight"]) )
            size = (int(jsonmetadata["vwidth"]) ,int(jsonmetadata["vheight"]))
            img = Image.frombuffer('RGBA', size, payload[headerlen:],'raw','RGBA',0,1)
            filename= jsonmetadata["type"]+"_"+jsonmetadata["time"]+"_temp."+jsonmetadata["ext"]
            img.save("%s/%s"%(local_image_repo, filename))
            #img.save("./%s"%(filename))
            logging.debug("Saving data %s",metadata)
            saveData(jsonmetadata);            
            try :                
                headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}
                tagconn = httplib.HTTPConnection("localhost:%s"%(tomcatport))
                params = urllib.urlencode({'data': metadata})
                print params
                tagconn.request("POST", "/TwoDThreeDCapture/ImageTaggingServlet", params, headers)
                response = tagconn.getresponse()
                print response.status, response.reason
                data = response.read()
                logging.debug(data)
                self.conn.send(encodeMessage("SAVED"))
                logging.debug('Closing the connection')                
                #self.conn.close()
                self.eMan.handleNewContent(jsonmetadata)
                filename = ""
                jsonmetadata =""
            except Exception, e:
                traceback.print_exc()
                logging.critical("Error %d: %s" % (e.args[0],e.args[1]))
                self.conn.send(encodeMessage("ERROR"))
                logging.debug('Closing the connection')
                self.conn.close()
            self.conn.send("Message Received")
            return "image"
        else :
            self.conn.send("Unacceptable OPCODE")
            self.conn.close();
            return
        
    def processPayload(self, bfmessageBody ,messageTypeID):
        if messageTypeID  == 1 or messageTypeID  == '1' :
            logging.debug("Type is text")
            self.conn.send(encodeMessage("Text received"))              
        elif messageTypeID ==2 or messageTypeID  == '2':
#             self.conn.send(encodeMessage("json is received"))
            logging.debug("Message type %s"%messageTypeID)           
            logging.debug("Message data %s"%bfmessageBody)
            jsonmetadata = json.loads(bfmessageBody)                 
            if(jsonmetadata["type"] =="subscribe") :
                logging.debug(jsonmetadata["data"])
                self.sub_id = self.eMan.subscribe_public_agents(jsonmetadata["data"], self.conn)
                logging.debug("Event subscription success--> %s"%(self.sub_id)) 
#                 self.conn.send(encodeMessage("Ill be damned"))   
            elif(jsonmetadata["type"] =="unsubscribe") :
                logging.debug(jsonmetadata["sub_id"])
                logging.debug("%s %s"%(jsonmetadata["sub_id"],self.sub_id))
                self.eMan.unsubscribe(self.sub_id)
            elif(jsonmetadata["type"] =="test") :
                logging.debug(jsonmetadata["data"]["type"])
                if(jsonmetadata["data"]["type"]=="publish") :
                    self.eMan.eventBroadcast("{\"type\" : \"test\" }")
                elif (jsonmetadata["data"]["type"]=="single") :
                    self.eMan.sendSingleEvent(self.sub_id,"{\"type\" : \"test\" }")
                elif messageTypeID == '3':
                    logging.debug("Expecting image content")
                else :
                    logging.debug("Data not found")                

###WEB SOCKET SERVER#######
class WebSocketServer(threading.Thread) :
    
    def frameHanlder(self)  :
        print 'this is not implemented yet'
        
    def closeConnection(self):
        self.running = False
        try: 
            s = socket.socket()         # Create a socket object
            host = 'localhost' # Get local machine name
            port = 17322
            s.connect((host, port))
        except socket.error as message:
            print 'Socket Closed : '  + str(message[0]) + ' Message ' + message[1]
            s.close();
        
    def sendMessage(self, message):
        self.rh.sendMessage(message)
                 
    def __init__(self, s, p, l):
        threading.Thread.__init__(self)
        self.SERVER = s
        self.PORT = p
        self.LOCALHOST = l
        self.eMan = EventManager()    
        self.running = False
             
        
    def setRunning(self, value):
        logging.debug("SETTING THE STARTING PARAMETERS..")
        self.running = value
        if(self.running) :
            self.start()

    def run(self):
        try:            
            sock= socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            sock.bind((self.SERVER,self.PORT))
            sock.listen(5)
            logging.info("Web Socket server lunched%s : %s"%(self.SERVER,self.PORT))
            while self.running:
                logging.debug("Wating For Requests...." )
                conn, addr = sock.accept()
                logging.info("Request Arrived..")
                rh = RequestHandler(self.eMan)            
                thread.start_new_thread(rh.messageReader, (conn, addr))
            sock.close();
            logging.debug("SERVER SHUTDOWN. gOOd bYe......!")
        except socket.error as message:
            print 'Bind failed. Error Code : '  + str(message[0]) + ' Message ' + message[1]
            sock.close();
#==========END OF WEBSOCKET COMPONENT==
###FLASK SERVER AND RELATED FUNCTIONALITY####

global wsrunning
wsrunning = False
#lhost = "dev.cyberlightning.com"
app = Flask(__name__)

@app.route('/')
@crossdomain(origin='*')
def index():
    return "This is a REST Service for 2D3DCapture Server."

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

@app.route('/startwebsocketserver')
@app.route('/postImage', methods = ['POST'])
@crossdomain(origin='*')
def startWSServer():
    logging.debug("POST CALLED TO START SERVER...")
    global wsthread
    global wsrunning
    if wsrunning== False :
        logging.debug("Starting the server ....")
        wsthread = WebSocketServer('',wsport,'127.0.0.1')
        wsthread.setRunning(True)
        wsrunning = True
    elif wsrunning ==True :
        logging.debug("Server is Running")
    return "READY" 

@app.route('/closewebsocketserver', methods = ['GET'])
@crossdomain(origin='*')
def shutDownWSServer():
    logging.debug("WEB SOCKET SERVER SHUTTING DOWN")
    global wsrunning
    global wsthread
    if wsrunning == True :
        wsthread.closeConnection();
        wsrunning = False
        return "CLOSED"
    else :     
        return "ALREADY_CLOSSED"

def saveData(jsondata):
    dbconn = dbConnect();
    cursor = dbconn.cursor ()
    filename = jsondata["type"]+"_"+jsondata["time"]+"."+jsondata["ext"]
    deviceType = jsondata["deviceType"]
    width = jsondata["vwidth"]
    height =jsondata["vheight"]
    if width > height :
        screenorientation= 1.00#landscape
    else :
        screenorientation= 0.00#potrait
    alt = str(jsondata["position"]["alt"]);    
    if(deviceType=='Mobile'):
        if alt=="None":
            alt = '0'       
        heading = '0'
        speed = '0'
        sqlstring1 = "INSERT INTO Imagedata values (\'"+filename+"\',GeomFromText ('POINT("+ str(jsondata["position"]["lat"])+" "+str(jsondata["position"]["lon"])+")'),"+alt+","+str(jsondata["position"]["acc"])
        sqlstring2 =","+str(jsondata["device"]["gx"])+","+str(jsondata["device"]["gy"])+","+str(jsondata["device"]["gz"])
        sqlstring3 = ","+str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+","+str(screenorientation)+",\'"+jsondata["device"]["orientation"]+"\',now(),\'"+str(jsondata["deviceOS"])+"\',\'"+str(jsondata["browsertype"])+"\',\'"+str(jsondata["deviceType"])+"\');"
    else :
        sqlstring1 = "INSERT INTO Imagedata values (\'"+filename+"\',GeomFromText ('POINT("+ str(jsondata["position"]["lat"])+" "+str(jsondata["position"]["lon"])+")'),"+alt+","+str(jsondata["position"]["acc"])
        sqlstring2 =","+'0.0'+","+'0.0'+","+'0.0'
        sqlstring3 = ","+'0.0'+","+'0.0'+","+'0.0'+","+'0.0'+",\'"+"Potrait"+"\',now(),\'"+str(jsondata["deviceOS"])+"\',\'"+str(jsondata["browsertype"])+"',\'"+str(jsondata["deviceType"])+"\');"
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
@crossdomain(origin='*')
def saveBinaryImage():
    logging.debug("POST BINARY IMAGE CALLED")
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
        img.save("%s/%s"%(local_image_repo,filename))
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
        global wsthread
        wsthread.eMan.handleNewContent(jsonmetadata)
    except RuntimeError as e:
        logging.critical("Error %d: %s" % (e.args[0],e.args[1]))
        return "415 Unsupported Media Type "
    return "SUCCESS"

@app.route('/getLocationImageData', methods= ['GET'])
@crossdomain(origin='*')
def getImageLinks():
    radius = 0.0001   
    lat =  request.args.get('lat',default=None, type=float)
    lng =  request.args.get('lon',default=None, type=float)
    logging.debug("%.5f  %.5f"%(lat, lng))
    photoList = getClosestImages(lat,lng,radius)
    logging.debug(photoList)     
    return jsonify(imageList=photoList),200

def getClosestImages(lat, lon, radius):
    photoList = []
    origin = (lat, lon)
    maxlat = lat+radius
    minlat = lat-radius
    maxlon = lon+radius
    minlon = lon-radius
    sqlstring =  "select imagename ,x(location) as latitude, y(location) as longitude,rotationalpha, rotationbeta, rotationgamma,screenorientation, deviceorientation, Browser,accelerationgx,accelerationgy,accelerationgz  from Imagedata where (%f>x(location) AND x(location)>%f) AND (%f> y(location) AND y(location)> %f)"%(maxlat,minlat,maxlon,minlon);
#     print sqlstring
    locationList = dbRead(sqlstring)
    if not locationList:
        if radius >= 0.01 :
            abort(400)
        else :
            radius = radius +0.0001
            logging.debug("Search for %f"%(radius*110000))
            photoList = getClosestImages(lat, lon, radius)        
    else :
        for row in locationList:
            destination = (row[1],row[2])
            distance = distance_calc(origin,destination)
            #row[7] deviceorientation
            tempvar = row[6]
            #row[8] browser
            #gx = row[9]
            #gy = row[10]
            #gz = row[11]
            pitch = 0.00 #beta
            roll = 0.00 #gamma
            yaw = 0.00  #alpha
#             if tempvar == 0 :
#                 screenorientation = "potrait"
#             elif tempvar == 1 :
#                 screenorientation = "landscape"
#             pitch = -1 * row[4] #beta x
#             roll = row[5] #gamma y
#             yaw = 360 - row[3]  #alpha z
            pitch = row[4] #beta x
            roll = row[5] #gamma y
            yaw =  row[3]  #alpha z                                                                    
            url = "%s/%s"%(repo_url,row[0])
            imagedata = {'imagename' : row[0], 'pitch' : pitch,'roll' : roll , 'yaw' : yaw ,'distance': distance, 'latitude' : row[1] , 'longitude' :row[2] , 'url' : url , 'alpha' :row[3] ,'beta' : row[4],'gamma' : row[5] , 'deviceorientation' : row[7]  }
            photoList.append(json.dumps(imagedata))
    return photoList
            
@app.route('/getAllImageData' , methods= ['GET'])
@crossdomain(origin='*')
def getAllImageData():
    sqlstring = "SELECT imagename,X(location) as latitude, Y(location) as longitude,rotationalpha, rotationbeta, rotationgamma,screenorientation, deviceorientation, Browser,accelerationgx,accelerationgy,accelerationgz FROM Imagedata"    
    gpsList = dbRead(sqlstring);
    photoList = []
    if not gpsList:
        logging.debug("No match Found")
    else :
        for row in gpsList:
#             deviceorientation = row[7]
#             tempvar = row[6]
#             browser = row[8]
#             gx = row[9]
#             gy = row[10]
#             gz = row[11]
# 
#             if tempvar == 0 :
#                 screenorientation = "potrait"
#             elif tempvar == 1 :
#                 screenorientation = "landscape"
            pitch = 0.00 #beta
            roll = 0.00 #gamma
            yaw = 0.00  #alpha
            pitch = -1 * row[4] #beta x
            roll = row[5] #gamma y
            yaw = 360 - row[3]  #alpha z                                   
            url = "http://dev.cyberlightning.com/~twijethilake/images/%s"%(row[0])
            imagedata = {'imagename' : row[0], 'pitch' : pitch,'roll' : roll , 'yaw' : yaw ,'latitude' : row[1] , 'longitude' :row[2] , 'url' : url , 'alpha' :row[3] ,'beta' : row[4],'gamma' : row[5] , 'deviceorientation' : row[7]  }                       
            photoList.append(json.dumps(imagedata))
    return jsonify(imageList=photoList),200

##END OF FLASK SERVER CODE##

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

def setup_parms():
    config = cp.RawConfigParser()
    config.read('server.properties')
    global tomcatport
    tomcatport = config.get('SectionPython', 'tomcatport')
    logging.debug("tomcatport %s",tomcatport)
    global wsport
    wsport = config.getint('SectionPython', 'wsport')
    logging.debug("wsport %s",wsport)
    global restport
    restport = config.getint('SectionPython', 'restport')
    logging.debug("restport %s",restport)
    global lhost
    lhost = config.get('SectionPython', 'lhost')
    logging.debug("lhost %s",lhost)
    global serverurl
    serverurl=config.get('SectionPython', 'serverurl')
    logging.debug("serverurl %s",serverurl)
    global repo_url
    repo_url =config.get('SectionPython', 'repo_url')
    logging.debug("repo_url %s",repo_url)
    global dbuser
    dbuser = config.get('SectionPython', 'dbuser')
    logging.debug("dbuser %s",dbuser)
    global dbpassword
    dbpassword = config.get('SectionPython', 'dbpassword')
    logging.debug("dbpassword %s",dbpassword)
    global dbname
    dbname = config.get('SectionPython', 'dbname')
    logging.debug("dbname %s",dbname)
    global local_image_repo
    local_image_repo=config.get('SectionPython', 'local_image_repo')
    logging.debug("local_image_repo %s",local_image_repo)   
    
def main():    
    setup_logging()
    setup_parms()
    logging.info("Logging Set up")
    app.run(host= serverurl , port = restport , debug = True)
    logging.info("Rest Server Running")    
        
if __name__ == "__main__":
    main()