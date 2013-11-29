package com.cyberlightning.realvirtualinteraction.backend;

import static org.junit.Assert.*;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.cyberlightning.realvirtualinteraction.backend.Application;

public class Test {

	  @Mock
	  Application server;

	  @Before
	  protected void setUp() throws Exception {
	    MockitoAnnotations.initMocks(this);
	  }

	  @org.junit.Test
	  public void testQuery()  {
	    // assume there is a class called ClassToTest
	    // which could be tested
	    Application t  = new Application();
	    String[] d= {""};
	    // call a method
	    

	    // test the return type
	    //assertTrue(check);

	    // test that the query() method on the 
	    // mock object was called
	    //Mockito.verify(mock).query("* from t");
	  }

}
