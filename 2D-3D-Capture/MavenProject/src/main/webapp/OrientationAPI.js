/** THIS COMPONENT IS DEVICE DEPENDANT 
 	Tested on 
 	1. Samsung Galaxy s2
 	2. Samsunga Galaxy Note 3
 	3. LG Stereoscopic
 */

var adjustOrientation = function (mobileType, alpha ,beta ,gamma, gx, gy, gz){
	 if(mobileType=="LG_stereoscopic"){
		  if(gz >6.0 & gz > gy  & gz > gx) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
		  } else if  (gz < (-6.0) & gz < gx & gz < gy ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha+180); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				 mbeta = -degToRad(beta); malpha = -degToRad(alpha-180); mgamma =degToRad(gamma);
			  }
		  } else if(gx > 6.0 & gx > gy & gx > gz){
			  if(gz >0 ) {
				  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
					  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
				  } else if ((alpha>90.0 & alpha <270.0) ){
					  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
				  }
		  	} else{
		  		 if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
					  if(beta < 0)
					  		mbeta = -degToRad(beta+180);
					  else
					  		bmeta = -degToRad(beta-180);
					  malpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
				  } else if ((alpha>90.0 & alpha <270.0) ){
					  if(beta < 0)
						  mbeta = -degToRad(beta+180);
					  else
						  mbeta = -degToRad(beta-180);
					  mlpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
				  }
		  	}
		  } else if  (gx < (-6.0) & gx < gy & gx < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha-180); mgamma =-degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mmbeta = -degToRad(beta); malpha = degToRad(alpha-180); mgamma =-degToRad(gamma);
			  }
		  } else if (gy >6.0 & gy > gz  & gy > gx) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else  if(alpha>90.0 & alpha <270.0) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  }
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma = degToRad(gamma);
	//		  } else if ((alpha>140.0 & alpha <20.0) ){
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
		  }
	  } /**
		 * CALIBRATED FOR GALAXY S2
		 * TODO GZ < 0 LANDSCAPE MODE
		 * TODO POTRATAIT MODE AT 90 AND 270
		 */	
	  else if(mobileType=="galaxy_s2"){	  
		  if(gz >6.0 & gz > gy  & gz > gx) {
			  //beta <45 at this points beta sesnsor value is negative
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);			  
			  }
			  //DONE
		  } else if  (gz < (-6.0) & gz < gx & gz < gy ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
			  }
			  /**END OF gz */
		  } else if(gx > 6.0 & gx > gy & gx > gz){
			  if(gz >0 ) {				  
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
		  	} else{
		  		mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
		  			 malpha = -degToRad(alpha); mgamma = degToRad(180-gamma);
					  if(beta < 0)
						  mbeta = -degToRad(beta+180);
					  else
						  beta = -degToRad(beta-180);
		  	}
		  } else if  (gx < (-6.0) & gx < gz & gx < gy ) {
			//beta >135 at this points beta sesnsor value is negative
			  if(gz > 0) {
					  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);				  
			  } else {
					  malpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
					  if(beta < 0)
						  mbeta = degToRad(beta+180);
					  else
						 mbeta = degToRad(beta-180);
			  }
			  /**END OF gx */
		  } else if (gy >6.0 & gy > gz  & gy > gx) {			 
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha); mgamma =degToRad(gamma);	
			  } else  if(alpha>90.0 & alpha <270.0) {
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);	
			  }
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);	
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
		  }
	} else if  (mobileType=="galaxy_tab_note3") {
		/**
		 * CALIBRATED FOR GALAXY NOTE 3
		 * TODO GZ <0 ALL SCENARIOS 
		 * TODO POTRATAIT MODE AT 90 AND 270
		 */
		 if(gz >6.0 & gz > gy  & gz > gx) {
			//beta <45 at this points beta sesnsor value is negative
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);		  
			  }
			  //DONE
		  } else if  (gz < (-6.0) & gz < gx & gz < gy ) {
			//beta >135 at this points beta sesnsor value is negative
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =degToRad(gamma);
			  }
			  /**END OF gz */
		  } else if(gx > 6.0 & gx > gy & gx > gz){
			  if(gz >0 ) {
					  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
		  	} else{
		  		mbeta = -degToRad(beta); malpha = -degToRad(alpha-180); mgamma =degToRad(gamma);
		  	}
		  } else if  (gx < (-6.0) & gx < gy & gx < gz ) {
			//beta >135 at this points beta sesnsor value is negative			  
				mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);				  
				malpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
				
			  /**END OF gx */
		  } else if (gy >6.0 & gy > gz  & gy > gx) {			
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha); mgamma =degToRad(gamma);			 
			  } else  if(alpha>90.0 & alpha <270.0) {
				  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
			  }
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  }
			  
			  /**END OF gz */
		  }
	}
	 var temp = new Object();
	 temp.alpha = malpha;
	 temp.beta = mbeta;
	 temp.gamma = mgamma;
	 temp.abeta = degToRad(beta);
	 return temp;
};
