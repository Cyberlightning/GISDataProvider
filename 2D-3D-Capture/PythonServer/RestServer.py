from flask import Flask ,jsonify
from flask import abort
from flask import request
import json
import logging, math
import argparse
import MySQLdb

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

def read_data(jsondata):
    #origin = []
    sqlstring = "SELECT X(location) as latitude, Y(location) as longitude, Imagename FROM Imagedata"
    gpsList = dbRead(sqlstring);
    if not gpsList:
        logging.debug("No match Found")
    else :
        for row in gpsList:
            imagename = row[2]
            logging.debug("Imagename=%s : Latitude=%s : Longitude : %s"%(imagename,row[0],row[1]))
            

#http://www.platoscave.net/blog/2009/oct/5/calculate-distance-latitude-longitude-python/
def distance_calc(origin, destination):
    lat1, lon1 = origin
    lat2, lon2 = destination
    radius = 6371 # km

    dlat = math.radians(lat2-lat1)
    dlon = math.radians(lon2-lon1)
    a = math.sin(dlat/2) * math.sin(dlat/2) + math.cos(math.radians(lat1)) \
        * math.cos(math.radians(lat2)) * math.sin(dlon/2) * math.sin(dlon/2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    d = radius * c

    return d
      

@app.route('/imagelinks', methods= ['GET'])
def get_image_links():
    if not request.json or not 'lon' in request.json or not 'lat' in request.json or not 'facing' in request.json:
        abort(400)
    read_data(request.json)
    return (request.json['lon']+ ","+ request.json['lat']+ "," +request.json['facing']+ ",")

@app.route('/googlemap' , methods= ['GET'])
def mark_photos():
    sqlstring = "SELECT imagename,X(location) as latitude, Y(location) as longitude,rotationalpha, rotationbeta, rotationgamma,screenorientation, deviceorientation FROM Imagedata"
    gpsList = dbRead(sqlstring);
    photoList = []
    if not gpsList:
        logging.debug("No match Found")
    else :
        for row in gpsList:
            url = "http://dev.cyberlightning.com/~twijethilake/images/%s"%(row[0])
            imagedata = {'imagename' : row[0], 'latitude' : row[1] , 'longitude' :row[2] , 'url' : url , 'alpha' :row[3] ,'beta' : row[4],'gamma' : row[5] , 'deviceorientation' : row[7]}            
            photoList.append(json.dumps(imagedata))
       # print photoList
            #logging.debug("Imagename=%s : Latitude=%s : Longitude : %s"%(imagename,row[0],row[1]))            
    return jsonify(imageList=photoList)
    

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
    
if __name__ == '__main__':
    setup_logging();
    app.run(host= "dev.cyberlightning.com", port = 17000 , debug = True)
    #app.run(host= "localhost", port = 17000 , debug = True)
    

