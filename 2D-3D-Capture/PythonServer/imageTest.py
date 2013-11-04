'''
Created on Oct 23, 2013

@author: tharanga
'''
import png,random, os



def create_bw_image():
    l = [range(0,255) for y in range(0,255)]    
#     print l
    f = open('ramp.png', 'wb')      # binary mode is important
    w = png.Writer(255,255 , greyscale=True)
    w.write(f, l)
    f.close()
    
def create_colour_image():
    image_raw = range(256*3*256)    
    values = map(lambda x: random.randint(0, 255),image_raw)   
    image_array =[]
    for x in range(0,255):
        image_array.append(values[x:x+(256*3)]) 
#     f = open('swatch.png', 'wb')
#     w = png.Writer(255, 255)
#     w.write(f, image_array) ;
    info = {'width' : 255, 'height' : 255 , }
    png.from_array(image_array, 'RGB' , info ).save('foo1.png')
    
def file_read():
    file = open("newfile.txt", "w")
 
    file.write("hello world in the new file\n")
     
    file.write("and another line\n")
     
    file.close()
    

if __name__ == '__main__':
    file_read()