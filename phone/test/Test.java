import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.lizar.util.FileTool;
import com.lizar.util.StringHelper;


public class Test {
	public static String read_it(File file){
		FileInputStream file_input = null;
		BufferedReader br=null;
		StringBuilder result=new StringBuilder("");
		try {
			file_input = new FileInputStream(file); 
			 InputStreamReader read = new InputStreamReader(file_input);
			 br = new BufferedReader(read); 
			String line = br.readLine(); 
			while(line!=null){
				result.append(line).append("\n");
				line=br.readLine();
			}
		} catch (IOException e) {
			System.err.println("config file read failed. pls check with "+file.toString());
			e.printStackTrace();
		}finally{
			if(file_input!=null)
				try {
					file_input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(br!=null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		return result.toString();
	}
	  
	public static void write_to_file(String data,File file) throws IOException{
        OutputStream out =new FileOutputStream(file);
        byte[] b=data.getBytes();
        try{
        	 for (int i = 0; i < b.length; i++) {
 	            out.write(b[i]);
 	        }
        }finally{
        	if(out!=null)out.close();
        }
}
	
	  private static void translate_file(File f) throws IOException{
		  String s=read_it(f);
		  if(StringHelper.isNotNull(s)){
			  s=s.replaceAll("\n\t", "\n");
			  s=s.replaceAll("\r", "\n");
			  
		  }
		  write_to_file(s, f);
	  }
	
		
		public static void main(String[] args) throws IOException{
			File dir=new File("I:/resources");
			File[] fs=dir.listFiles();
			if(fs!=null){
				for(File f:fs){
					System.out.println(f.getName());
					if(f.isFile()){
						 translate_file(f);
					}
				}
				
			}
		}
}
