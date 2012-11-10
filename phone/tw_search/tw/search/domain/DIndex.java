package tw.search.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import tw.search.persistence.CelebrityIndexDao;
import tw.search.persistence.IndexInfoDao;
import tw.search.persistence.MovieIndexDao;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.config.Keys;
import com.mongodb.Entity;
import com.tw.persistence.movie.MovieDao;

public class DIndex extends Thread {
	
	private IndexInfoDao index_info_dao=Web.get(IndexInfoDao.class);
	private MovieIndexDao movie_update_dao=Web.get(MovieIndexDao.class);
	private CelebrityIndexDao celebrity_update_dao=Web.get(CelebrityIndexDao.class);
	private MovieDao movie_dao=Web.get(MovieDao.class);

	public static  boolean run=false;
	
	public static  String event;
	
	public static  long start_time;
	
	public static String exception;
	
	public static boolean create_new=false;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(run){
			if("movie_index_syn".equals(event)){
				movie_index();
			}else if("celebrity_index_syn".equals(event)){
				celebrity_index();
			}
		}
	}
	
	private void movie_index(){
		long id=0;
		File docDir = new File(Config.path_filter(Keys._str("index.movie_index_dir")));
	    if (!docDir.exists()) {
	    	docDir.mkdirs();
	    	create_new=true;
	    }
	    IndexWriter writer=null;
	    try {
	      Directory dir = FSDirectory.open(docDir);
	      Analyzer analyzer=new org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer(Version.LUCENE_40);
	      IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
	      if (create_new) {
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }
	      writer = new IndexWriter(dir, iwc);
	    }catch(Exception e){
	    	e.printStackTrace();
	    	exception=e.getLocalizedMessage();
	    	go_to_end();
	    	return;
	    }
		while(run){
			id=movie_update_dao.next();
			index_movie(movie_dao.get(id),writer);
			movie_update_dao.remove(id);
			if(id==0)break;
			try {
				Thread.sleep(Keys._int("index.thread_interval"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(writer!=null){
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				exception=e.getLocalizedMessage();
			}
		}
		go_to_end();
	}

	private void go_to_end(){
		index_info_dao.end_event(start_time,movie_update_dao.count(),exception);
		run=false;
		event=null;
		start_time=0;
		exception=null;
	}
	
	private void celebrity_index(){
		long id=0;
		while(run){
			id=celebrity_update_dao.next();
			index_celebrity(null);
			celebrity_update_dao.remove(id);
			if(id==0)break;
			try {
				Thread.sleep(Keys._int("index.thread_interval"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		index_info_dao.end_event(start_time,celebrity_update_dao.count(),exception);
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(run){
			run=false;
			if("movie_index_syn".equals(event)){
				index_info_dao.end_event(start_time,movie_update_dao.count(),exception);
			}else if("celebrity_index_syn".equals(event)){
				index_info_dao.end_event(start_time,celebrity_update_dao.count(),exception);
			}
			
		}
	}
	
	private void index_movie(Entity e,IndexWriter writer){
        Document doc = new Document();
        //movie_id 	as key
        //keys	 	as index string
        //
        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize 
        // the field into separate words and don't index term frequency
        // or positional information:
        Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
        doc.add(pathField);

        // Add the last modified date of the file a field named "modified".
        // Use a LongField that is indexed (i.e. efficiently filterable with
        // NumericRangeFilter).  This indexes to milli-second resolution, which
        // is often too fine.  You could instead create a number based on
        // year/month/day/hour/minutes/seconds, down the resolution you require.
        // For example the long value 2011021714 would mean
        // February 17, 2011, 2-3 PM.
        doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.
        doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
          // New index, so we just add the document (no old document can be there):
          System.out.println("adding " + file);
          writer.addDocument(doc);
        } else {
          // Existing index (an old copy of this document may have been indexed) so 
          // we use updateDocument instead to replace the old one matching the exact 
          // path, if present:
          System.out.println("updating " + file);
          writer.updateDocument(new Term("path", file.getPath()), doc);
        }
	}
	
	private void index_celebrity(Entity e){
		
	}
	
	 /** Index all text files under a directory. */
	  public static void main(String[] args) {
	    String usage = "java org.apache.lucene.demo.IndexFiles"
	                 + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
	                 + "This indexes the documents in DOCS_PATH, creating a Lucene index"
	                 + "in INDEX_PATH that can be searched with SearchFiles";
	    String indexPath = "index";
	    String docsPath = null;
	    boolean create = true;
	    for(int i=0;i<args.length;i++) {
	      if ("-index".equals(args[i])) {
	        indexPath = args[i+1];
	        i++;
	      } else if ("-docs".equals(args[i])) {
	        docsPath = args[i+1];
	        i++;
	      } else if ("-update".equals(args[i])) {
	        create = false;
	      }
	    }

	    if (docsPath == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    }

	    final File docDir = new File(docsPath);
	    if (!docDir.exists() || !docDir.canRead()) {
	      System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + indexPath + "'...");

	      Directory dir = FSDirectory.open(new File(indexPath));
	      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
	      IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);

	      if (create) {
	        // Create a new index in the directory, removing any
	        // previously indexed documents:
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        // Add new documents to an existing index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }

	      // Optional: for better indexing performance, if you
	      // are indexing many documents, increase the RAM
	      // buffer.  But if you do this, increase the max heap
	      // size to the JVM (eg add -Xmx512m or -Xmx1g):
	      //
	      // iwc.setRAMBufferSizeMB(256.0);

	      IndexWriter writer = new IndexWriter(dir, iwc);
	      indexDocs(writer, docDir);

	      // NOTE: if you want to maximize search performance,
	      // you can optionally call forceMerge here.  This can be
	      // a terribly costly operation, so generally it's only
	      // worth it when your index is relatively static (ie
	      // you're done adding documents to it):
	      //
	      // writer.forceMerge(1);

	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	  }

	  /**
	   * Indexes the given file using the given writer, or if a directory is given,
	   * recurses over files and directories found under the given directory.
	   * 
	   * NOTE: This method indexes one document per input file.  This is slow.  For good
	   * throughput, put multiple documents into your input file(s).  An example of this is
	   * in the benchmark module, which can create "line doc" files, one document per line,
	   * using the
	   * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	   * >WriteLineDocTask</a>.
	   *  
	   * @param writer Writer to the index where the given file/dir info will be stored
	   * @param file The file to index, or the directory to recurse into to find files to index
	   * @throws IOException If there is a low-level I/O error
	   */
	  static void indexDocs(IndexWriter writer, File file)
	    throws IOException {
	    // do not try to index files that cannot be read
	    if (file.canRead()) {
	      if (file.isDirectory()) {
	        String[] files = file.list();
	        // an IO error could occur
	        if (files != null) {
	          for (int i = 0; i < files.length; i++) {
	            indexDocs(writer, new File(file, files[i]));
	          }
	        }
	      } else {

	        FileInputStream fis;
	        try {
	          fis = new FileInputStream(file);
	        } catch (FileNotFoundException fnfe) {
	          // at least on windows, some temporary files raise this exception with an "access denied" message
	          // checking if the file can be read doesn't help
	          return;
	        }

	        try {

	          // make a new, empty document
	          Document doc = new Document();

	          // Add the path of the file as a field named "path".  Use a
	          // field that is indexed (i.e. searchable), but don't tokenize 
	          // the field into separate words and don't index term frequency
	          // or positional information:
	          Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
	          doc.add(pathField);

	          // Add the last modified date of the file a field named "modified".
	          // Use a LongField that is indexed (i.e. efficiently filterable with
	          // NumericRangeFilter).  This indexes to milli-second resolution, which
	          // is often too fine.  You could instead create a number based on
	          // year/month/day/hour/minutes/seconds, down the resolution you require.
	          // For example the long value 2011021714 would mean
	          // February 17, 2011, 2-3 PM.
	          doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

	          // Add the contents of the file to a field named "contents".  Specify a Reader,
	          // so that the text of the file is tokenized and indexed, but not stored.
	          // Note that FileReader expects the file to be in UTF-8 encoding.
	          // If that's not the case searching for special characters will fail.
	          doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

	          if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	            // New index, so we just add the document (no old document can be there):
	            System.out.println("adding " + file);
	            writer.addDocument(doc);
	          } else {
	            // Existing index (an old copy of this document may have been indexed) so 
	            // we use updateDocument instead to replace the old one matching the exact 
	            // path, if present:
	            System.out.println("updating " + file);
	            writer.updateDocument(new Term("path", file.getPath()), doc);
	          }
	          
	        } finally {
	          fis.close();
	        }
	      }
	    }
	  }

	
}
