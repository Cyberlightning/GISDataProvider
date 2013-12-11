'''
Created on Dec 5, 2013
@author: tharanga
'''
from flask.ext.testing import TestCase
from flask import Flask
from RestfullService import WebSocketServer
import unittest

wsport = 17320
class WebSocketServerTestCase((unittest.TestCase)):
    def setUp(self):
        
        self.socketServer = WebSocketServer('',wsport,'127.0.0.1')
        
    def tearDown(self):
        self.socketServer.closeConnection();
        
    def test_startServer(self):
        self.socketServer.start();

# class RestServerTestCase(unittest.TestCase):
#     
#     def setUp(self):
#         RestfullService.app.config['TESTING'] = True
#         self.app = RestfullService.app.test_client()
#         #self.socketServer = self.app.WebSocketServer('',wsport,'127.0.0.1')        
#         
#     def test_rootpath(self):
#         rv = self.app.get('/')
#         assert 'This is a REST Service for 2D3DCapture Server.' in rv.data
#         
#     def test_post_image(self):
#         rv = self.app.post('/postImage')
#         assert 'READY' in rv.data

if __name__ =="__main__":
    unittest.main();