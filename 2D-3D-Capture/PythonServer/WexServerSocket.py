import socket ,re  
import struct   
from base64 import b64encode
from hashlib import sha1
import thread
from PIL import Image
import io


'''
Created on Sep 4, 2013

@author: tharanga
'''

response_header =('HTTP/1.1 101 Switching Protocols',
    'Upgrade: websocket',
    'Connection: Upgrade',
    'Sec-WebSocket-Accept: {key}\r\n\r\n',)

magic_string = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

class BackEndServerSocket :
    ANY = ''
    PORT = 17000
    LOCALHOST = "127.0.0.1"
    BUFFER = ""
    
    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)        
            
    def setup(self):
        try :
            self.sock.bind((self.ANY, self.PORT))
            self.sock.listen(10)
        except socket.error as message:
            print 'Bind failed. Error Code : '  + str(message[0]) + ' Message ' + message[1] 
    
    def connectionHandler(self, conn, addr):
        f = {'fin'          : 0,
             'opcode'       : 0,
             'masked'       : False,
             'hlen'         : 2,
             'length'       : 0,
             'payload'      : None,
             'left'         : 0,
             'close_code'   : 1000,
             'close_reason' : ''}
        
        print 'Connected with ' + addr[0] + ':' + str(addr[1])
        while True:
            #It is easier to define this rather than using numbers. Reason is the value should be updated on the value of the second byte(unmaksed)
            print 'receiving data'  
            data = conn.recv(4096)
            self.BUFFER += data   
            buf = self.BUFFER           
            print 'Received data length'
            print len(data)
             
            #working
            if len(data) == 0:
                break 
            key = re.search('Sec-WebSocket-Key:\s+(.*?)[\n\r]+', data)
            if key is not None:
                print data
                key = key.groups()[0].strip()
                if key is not None:
                    response_key = b64encode(sha1(key + magic_string).digest())
                    response = '\r\n'.join(response_header).format(key=response_key)
                    print '#####################################'
                    print response
                    print len(response)
                    print '#####################################'
                    conn.send(response)
                key = ""
                self.BUFFER = ""                
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
                    length, = struct.unpack_from('>xH', buf)
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
                    print "check the buffer"
                    print len(self.BUFFER)
                    #print self.BUFFER
                    #print "<!---------------------------------------!>"
                        # is there a complete frame in the buffer?
                                      
                        # remove leading bytes, decode if necessary, dispatch
                        
                    payload = buf[payload_start:full_len]
                    # use xor and mask bytes to unmask data
                    if mask:  
                        print mask_bytes                        
                        print "<!======================================!>"
                        unmasked = [mask_bytes[i % 4] ^ ord(b) for b, i in zip(payload, range(len(payload)))]
                        print unmasked[1:250]
                        print "<!======================================!>"
                        payload = "".join([chr(c) for c in unmasked])
                        #print binascii.hexlify(payload)
                        print "<!======================================!>"
                                
                    if opcode == 1:
                        s = payload.decode("UTF8")
                        print s
                        self.BUFFER=""
                        buf= ""
                        conn.close()
                        break 
                    if opcode == 2:                        
                        print 'Handling binary data'
                        #print payload
                        stream = io.BytesIO(payload);
#                         img = Image.open(stream)
#                         img.save("a_test.png")
                        size = 640 ,427
                        img = Image.frombuffer('RGBA', size, payload)
                        img.save('image.png')
                        #print payload.decode("UTF8")
                        #print binascii.b2a_qp(payload[0:20])
                        #print payload[0:20]                                                        
                        print '##### frame processed###'        
                        print 'Closing the connection'
                        conn.close()
                        self.BUFFER=""
                        buf= ""
                        break
        
    def frameHanlder(self)  :
        print 'this is not implemented yet'
        
    def runServer(self):
        while True:
            #wait to accept a connection - blocking call
            conn, addr = self.sock.accept()
            self.connectionHandler(conn, addr)    
            
    def closeConnection(self):
        self.sock.close()
              
def main():
    ss = BackEndServerSocket();
    ss.setup();
    ss.runServer() 
              
    
if __name__ == '__main__':
    main()