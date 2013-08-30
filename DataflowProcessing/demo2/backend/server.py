# install gevent: sudo apt-get install python-gevent
# dependecies: sudo apt-get install libevent-dev

from PIL import Image
import StringIO
import re, base64
from urllib import unquote
import gevent.socket
from gevent.pywsgi import WSGIServer
from gevent.pool import Pool
from urllib import quote
import os.path

save_path = 'images/'

PORT = 8088
ADDRES = '127.0.0.1'
default_headers = [('Content-Type', 'text/plain'), ('Access-Control-Allow-Origin', '*'), ('Access-Control-Allow-Headers', 'Content-Type')]

def handleConnection(environ, start_response):
    if environ['REQUEST_METHOD'] == 'POST' or environ['HTTP_ACCESS_CONTROL_REQUEST_METHOD'] == 'POST':
        start_response('200 OK', default_headers)
        http_origin = environ['HTTP_ORIGIN'];
        remote_addr = str(environ['REMOTE_ADDR'])
        responseMessage = saveImage(remote_addr, environ['wsgi.input'].read(), 'JPEG');
        return [responseMessage]
    else:
        start_response('404 Not Found', default_headers)
        return ['No Luck']

def saveImage(filename, imageStream, imageFormat):
    try:
        imageStream = re.search(r'base64,(.*)', unquote(imageStream)).group(1)
        tempImg = StringIO.StringIO(imageStream.decode('base64'))
        im = Image.open(tempImg)

        i = 1
        completeName = os.path.join(save_path, filename)
        completeNameTmp = completeName
        while os.path.isfile(completeName):
            completeName = completeNameTmp + '(' + str(i) +')'
            i += 1

        im = im.convert('1') #convert image to black and white.

        if not os.path.exists(save_path):
            os.makedirs(save_path)

        im.save(completeName, format = imageFormat)
        tempImg.close()
        return'Image received. Image saved'
    except Exception, e:
        return 'Image received. Failed to save image'

if __name__ == '__main__':

    print 'Gevent version: %s' % gevent.__version__
    print 'Starting server on port %s...' % PORT
    pool = Pool(10000) # set a maximum for concurrency
#    print 'using timeout', gevent.socket.getdefaulttimeout()
    server = WSGIServer((ADDRES, PORT), handleConnection, spawn=pool)
    server.serve_forever()

