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
PORT = 17000
LOCALHOST = "127.0.0.1"


def encodeMessage( message):
    message = b64encode(message)
    b1 =0x80 | 0x1 & 0x0f
    payload_len = len(message)
    header = struct.pack('>BB', b1, payload_len)
    message= header +message
    return message
    
def connectionHandler( conn, addr):
    filename= ""
    BUFFER = ""
    done=""             
    print 'Connected with ' + addr[0] + ':' + str(addr[1])
    while True:
        try:
                    #It is easier to define this rather than using numbers. Reason is the value should be updated on the value of the second byte(unmaksed)
            print 'receiving data :' +filename 
            data = conn.recv(4096)
            BUFFER += data   
            buf = BUFFER           
                #print 'Received data length'
                #print len(data)
                     
                #working
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
#                     print '#####################################'
#                     print response
#                     print len(response)
#                     print '#####################################'
                    conn.send(response)
                    message = "Hello Client\n..Server is listening..!"                                              
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
                #print "OPCODE"
                #print opcode
                #print "Length"                
                #print length
                #print "FIN"
                #print fin
                        # check that enough bytes remain
                if len(buf) < payload_start + 4:
                    return
                elif length == 126:
                    length, = struct.unpack_from('>xxH', buf)
                    payload_start += 2
                elif length == 127:
                    length, = struct.unpack_from('>xxQ', buf)
                    payload_start += 8
                #print "LENGTH OF THE PAYLOAD"
                #print length
                        #print "Un-masking"                                
                if mask:
                    mask_bytes = [ord(b) for b in buf[payload_start:payload_start + 4]]
                            #print "MASK"
                            #print mask_bytes
                    payload_start += 4
                full_len = payload_start + length
                #print "FULL LENGTH"
                #print full_len
                #print len(buf)
                if len(buf) < full_len:
                    print "Not a complete frame"
                else:
                    print 'There is a complete frame now' 
                    #print "check the buffer"
                    print len(buf)
                    #print self.BUFFER
                    #print "<!---------------------------------------!>"
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
                                jsondata = json.loads(s)
                                filename= jsondata["type"]+"_"+jsondata["time"]+"."+jsondata["ext"]
                                print(filename)
                                #time.sleep(1)
                                message= "FILENAME"                            
                                sent = conn.send(encodeMessage(message))                           
                                print "BYTES SENT"
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
                                #print payload
                                try :
                                    payload = b64decode(payload)
                                    file_like = StringIO(payload)
                                    i = Image.open(file_like)
                                    i.save(filename)
                                except RuntimeError as e:
                                    conn.close();
                                    print e.strerror                                    
                                    sys.exit(0)                                
                                conn.close()
                                BUFFER=""
                                buf= ""
                                filename= ""
                                done= ""
                                break 
                        elif opcode == 2:                        
                            print 'Handling binary data'                           
                            size = 640 ,427
                            img = Image.frombuffer('RGBA', size, payload,'raw','RGBA',0,1)
                            img.save(filename)      
                            print '##### frame processed###'        
                            print 'Closing the connection'
                            conn.close()
                            BUFFER=""
                            buf= ""
                            filename = ""
                            return
        except KeyboardInterrupt:
            conn.close();
            print "INTERRUPTED!"
            sys.exit(0)   
    
def frameHanlder()  :
    print 'this is not implemented yet'
   
def closeConnection(self):
    sock.close()
             
try :
    sock= socket.socket()
    sock.bind((ANY, PORT))
    sock.listen(500)
    while True:
    #wait to accept a connection - blocking call
        conn, addr = sock.accept()
        gevent.spawn(connectionHandler, conn, addr)
except socket.error as message:
    print 'Bind failed. Error Code : '  + str(message[0]) + ' Message ' + message[1]              
