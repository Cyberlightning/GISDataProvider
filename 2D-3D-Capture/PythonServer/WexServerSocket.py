
import re  
import struct   
from base64 import b64encode,b64decode
from hashlib import sha1
from cStringIO import StringIO
import gevent
from gevent import socket
from PIL import Image
import json
import sys 
import MySQLdb
import logging
import argparse


'''
Created on Sep 4, 2013

@author: tharanga
'''

response_header =('HTTP/1.1 101 Switching Protocols',
    'Upgrade: websocket',
    'Connection: Upgrade',
    'Sec-WebSocket-Accept: {key}\r\n\r\n',)

magic_string = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

ANY = ''
PORT = 17324
LOCALHOST = "127.0.0.1"


def encodeMessage( message):
    message = b64encode(message)
    b1 =0x80 | 0x1 & 0x0f
    payload_len = len(message)
    header = struct.pack('>BB', b1, payload_len)
    message= header +message
    return message

def dbsave(jsondata):
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

    logging.debug( heading +str(jsondata["motion"]["heading"])+","+str(jsondata["motion"]["speed"]))
#     print
    sqlstring1 = "INSERT INTO Imagedata values (\'"+filename+"\',GeomFromText ('POINT("+ str(jsondata["position"]["lat"])+" "+str(jsondata["position"]["lon"])+")'),"+str(jsondata["position"]["alt"])+","+str(jsondata["position"]["acc"])
#    sqlstring2 =","+str(jsondata["motion"]["heading"])+","+str(jsondata["motion"]["speed"])+","+str(jsondata["device"]["ax"])+","+str(jsondata["device"]["ay"])+","+str(jsondata["device"]["az"])+",0.00,0.00,0.00,"
#    sqlstring3 = str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+",0.00,\'"+jsondata["device"]["orientation"]+"\',now())"
    sqlstring2 =","+str(jsondata["motion"]["heading"])+","+str(jsondata["motion"]["speed"])+","+str(jsondata["device"]["ax"])+","+str(jsondata["device"]["ay"])+","+str(jsondata["device"]["az"])+","+str(jsondata["device"]["gx"])+","+str(jsondata["device"]["gy"])+","+str(jsondata["device"]["gz"])
#    sqlstring3 = ","+str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+",0.00,\'"+jsondata["device"]["orientation"]+","+str(jsondata["device"]["dTime"])+")"
    sqlstring3 = ","+str(jsondata["device"]["ra"])+","+str(jsondata["device"]["rb"])+","+str(jsondata["device"]["rg"])+","+str(screenorientation)+",\'"+jsondata["device"]["orientation"]+"',now())"
    sqlstring = sqlstring1 + sqlstring2+ sqlstring3	
    logging.info( "sql String %s" , sqlstring)
    try:
        cursor.execute (sqlstring)
        dbconn.commit()
    except MySQLdb.Error,e :
        dbconn.rollback()
        logging.critical("Error %d: %s" % (e.args[0],e.args[1]))      
    cursor.close ()
    dbconn.close ()
    print "Database transaction completed"
    
def connectionHandler( conn, addr):
    filename= ""
    BUFFER = ""
    done=""             
    logging.info(  'Connected with %s : %s',  addr[0] , str(addr[1]))
    while True:
        try:
                    #It is easier to define this rather than using numbers. Reason is the value should be updated on the value of the second byte(unmaksed)
            #print 'receiving data :' +filename 
            data = conn.recv(4096)
            BUFFER += data   
            buf = BUFFER  
            logging.debug("recived data length %s",len(data))               
            if len(data) == 0:
                return            
            key = re.search('Sec-WebSocket-Key:\s+(.*?)[\n\r]+', data)
            if key is not None:
                #print data
                    #print len(key)
                key = key.groups()[0].strip()
                if key is not None:
                    response_key = b64encode(sha1(key + magic_string).digest())
                    response = '\r\n'.join(response_header).format(key=response_key)
                    logging.debug('##################################### \n %s \n %s \n #####################################',response,len(response))
                    conn.send(response)
                    message = "SERVER_READY"
                    conn.send(encodeMessage(message))
                    key = ""
                    BUFFER = ""                
            else :
                payload_start = 2                
                        
                        # try to pull first two bytes
                b1 = ord(buf[0])
                fin = b1 & 0x80      # 1st bit
                        # next 3 bits reserved
                opcode = b1 & 0x0f   # low 4 bits
                b2 = ord(buf[1])
                mask = b2 & 0x80    # high bit of the second byte
                length = b2 & 0x7f   # low 7 bits of the second byte
                        #print "<!---------------------------------------!>"
                logging.debug("OPCODE %s",opcode)
                logging.debug("Length %s", length)                
                logging.debug("FIN %s", fin)
                
                # check that enough bytes remain
                if len(buf) < payload_start + 4:
                    return
                elif length == 126:
                    length, = struct.unpack_from('>xxH', buf)
                    payload_start += 2
                elif length == 127:
                    length, = struct.unpack_from('>xxQ', buf)
                    payload_start += 8
                logging.debug ("LENGTH OF THE PAYLOAD :%s",length)
                        #print "Un-masking"                                
                if mask:
                    mask_bytes = [ord(b) for b in buf[payload_start:payload_start + 4]]
                            #print "MASK"
                            #print mask_bytes
                    payload_start += 4
                full_len = payload_start + length
                logging.debug("FULL LENGTH :%s", full_len)
                #print len(buf)
                
                if len(buf) < full_len:
                    sys.stdout.write('-')
                else:
                    sys.stdout.write('-|\n')
                    # remove leading bytes, decode if necessary, dispatch                                
                    payload = buf[payload_start:full_len]
                            # use xor and mask bytes to unmask data
                    if mask:  
                                #print mask_bytes                        
                                #print "<!======================================!>"
                        unmasked = [mask_bytes[i % 4] ^ ord(b) for b, i in zip(payload, range(len(payload)))]
                        #unmasked = reversed(unmasked)
                        #print "<!======================================!>"
                        payload = "".join([chr(c) for c in unmasked])
                        #print binascii.hexlify(payload)
                        #print "<!======================================!>"
                                        
                        if opcode == 1:
                            if done=="":                                
                                s = payload.decode("UTF8")
                                logging.info("Decodded payleoad :%s", s)
                                jsonmetadata = json.loads(s)
                                filename= jsonmetadata["type"]+"_"+jsonmetadata["time"]+"."+jsonmetadata["ext"]
                                logging.debug("File name %s", filename)
                                logging.debug("Position ->logitude %s ", str(jsonmetadata["position"]["lon"]))
                                #print(jsondata["position"]["lat"])                                
                                #time.sleep(1)
                                message= "FILENAME"                            
                                sent = conn.send(encodeMessage(message))                           
                                logging.debug("Message %s sent.Length %s", message, sent)
                                print sent
                                BUFFER=""
                                buf= ""                                
                                done="true"
                            else :
#                                 payload= payload[5:]
                                #print payload
                                splitplace=payload.find(",")
                                #print(splitplace)
                                payload= payload[splitplace+1:]
                                print "########################################"
                                logging.debug("printing Payload %s", payload)
                                try :
                                    payload = b64decode(payload)
                                    file_like = StringIO(payload)
                                    i = Image.open(file_like)
                                    i.save("../public_html/images/%s"%(filename))
                                    dbsave(jsonmetadata);
                                except RuntimeError as e:
                                    conn.close();
                                    logging.critical( "Runtime error in decoding bnary system exit %s", e.strerror)                                    
                                    sys.exit(0)                                
                                conn.close()
                                BUFFER=""
                                buf= ""
                                filename= ""
                                jsonmetadata =""
                                done= ""
                                break 
                        elif opcode == 2:                        
                            logging.debug('Handling binary data')                           
                            size = jsonmetadata["vwidth"] ,jsonmetadata["vheight"]
                            img = Image.frombuffer('RGBA', size, payload,'raw','RGBA',0,1)
                            img.save("../public_html/images/%s"%(filename))
                            dbsave(jsonmetadata);                                
                            logging.debug('Closing the connection')
                            conn.close()
                            BUFFER=""
                            buf= ""
                            filename = ""
                            jsonmetadata =""
                            done= ""
                            return
        except KeyboardInterrupt:
            conn.close();
            logging.critical(" keyboard Interuption. Program exiting.")
            sys.exit(0)   
    
def frameHanlder()  :
    print 'this is not implemented yet'
   
def closeConnection(self):
    sock.close()

def main():
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
    fh = logging.FileHandler('eventlog.log')
    fh.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    fh.setFormatter(formatter)
    logging.getLogger('').addHandler(fh)   
    try :    
        sock= socket.socket()
        sock.bind((ANY, PORT))
        logging.debug('Server is listening to %s on port %s',ANY, PORT)
        sock.listen(500)
        while True:
        #wait to accept a connection - blocking call
            conn, addr = sock.accept()
            
            gevent.spawn(connectionHandler, conn, addr)
    except socket.error as message:
        logging.error('Bind failed. Error Code : %s Message %s'%( str(message[0]),  message[1]))
        
        
if __name__ == "__main__":
    main()
