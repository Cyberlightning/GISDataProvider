package org.FIware.cyber.d2d3.web.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;

/**
 * Servlet implementation class ImageTaggingServlet
 */
@WebServlet("/ImageTaggingServlet")
public class ImageTaggingServlet extends HttpServlet {
	PrintWriter out;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageTaggingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	public BufferedImage readImage(File f){
		BufferedImage in;
        try {
                in = ImageIO.read(f);
        } catch (IOException e) {
                throw new RuntimeException(e);
        }
        return in;
	}
	
	public boolean tagImage(String imagename, String extension,String tag) {

		String path ="/home/twijethilake/public_html/images/";
//		String path ="/home/tharanga/workspace/pub-sub-example/public_html/images/";
		String filename = path+imagename+"."+extension;
		String tempfilename = path +imagename+"_temp."+extension;
		File f = new File(tempfilename);
		if(f.exists()){
			try {
				PngReader pngr = new PngReader(f);
		        PngWriter pngw = new PngWriter(new File(filename), pngr.imgInfo);
		        pngw.copyChunksFrom(pngr.getChunksList());
		        pngw.getMetadata().setText(PngChunkTextVar.KEY_Title, imagename+"."+extension);
		        pngw.getMetadata().setText("AdvancedGeoTag", tag);
		        for (int row = 0; row < pngr.imgInfo.rows; row++) {
		        	pngw.writeRow(pngr.readRow());
		        }	        	        
		        pngw.end();
		        pngr.end();
		        f.delete();
		        return true;
			}catch (Exception e){
				out.println(e.getMessage());
				return false;
			}	        
		} else{
			System.out.println("No File");
			return false;			
		}
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
	
	public void parseJson(){
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		out = response.getWriter();
		String  data = request.getParameter("data");
		if(data == null){
			out.print("Null data");
		}else if(data != "" ){
			JSONParser parser=new JSONParser();
			String imagename = null;
			String extension = null;
			
			try {
				Object obj = parser.parse(data);
				JSONObject jsonObject = (JSONObject) obj;
				imagename = (String) jsonObject.get("type")+"_"+(String) jsonObject.get("time");
				extension = (String) jsonObject.get("ext");
				if(tagImage(imagename,extension,data)){
					out.println("DONE");
				}else{
					out.println("ERROR");
				}
			} catch (ParseException e1) {
				out.println(e1.getMessage());
			}catch (Exception e){
				out.println(e.getMessage());
			}
		}else {
			out.print("No data");
		}

	}
	
	

}