package com.lizar.web.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.SimplePool;

import com.lizar.json.JSON;
import com.lizar.web.Controller;
import com.lizar.web.config.Config;

public class VelocitySupport  extends TemplateSupport {
	private ServletContext context;
	
	@Override
	public void handle(EventLoader eventLoader) throws ServletException,
			IOException {
        handle(eventLoader.request().getPathInfo(),eventLoader);
	}

	@Override
	public void handle(String path, EventLoader eventLoader)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				Context context = null;
		        try
		        {
		            context = createContext( eventLoader.request(), eventLoader.response() );
		            setContentType( eventLoader.request(), eventLoader.response() );
		            Template template = this.getTemplate(path, Controller.encode_type);
		            if ( template == null )
		            {
		                return;
		            }
		            Enumeration em=eventLoader.request().getAttributeNames();
		            HttpServletRequest request=eventLoader.request();
		            HttpSession session=request.getSession();
		            ServletContext app=session.getServletContext();
		            for(;em.hasMoreElements();){
		            	Object o=em.nextElement();
		            	context.put(o.toString(), request.getAttribute(o.toString()));
		            }
		            em=session.getAttributeNames();
		            for(;em.hasMoreElements();){
		            	Object o=em.nextElement();
		            	if(request.getAttribute(o.toString())==null){
		            	context.put(o.toString(), session.getAttribute(o.toString()));
		            	}
		            }
		            em=app.getAttributeNames();
		            for(;em.hasMoreElements();){
		            	Object o=em.nextElement();
		            	if(request.getAttribute(o.toString())==null&&session.getAttribute(o.toString())==null){
		            	context.put(o.toString(), app.getAttribute(o.toString()));
		            	}
		            }
		            mergeTemplate( template, context, eventLoader.response() );
		        }
		        catch (Exception e)
		        {
		            error( eventLoader.request(), eventLoader.response(), e);
		        }
		        finally
		        {
		            /*
		             *  call cleanup routine to let a derived class do some cleanup
		             */

		            
		        }
	}
	
	
	@Override
	public String default_listen() {
		// TODO Auto-generated method stub
		return "vm,tpl";
	}

	
	@Override
	public void init(ServletContext context,JSON params) throws ServletException {
		// TODO Auto-generated method stub
		this.context=context;
        try
        {
            /*
             *  call the overridable method to allow the
             *  derived classes a shot at altering the configuration
             *  before initializing Runtime
             */
            Properties props = new Properties();
            props.putAll(params.toMap());
            props.put(Velocity.FILE_RESOURCE_LOADER_PATH, context.getRealPath(params._string("file.resource.loader.path", "/")));
            Velocity.init( props );
        }
        catch( Exception e )
        {
            throw new ServletException("Error initializing Velocity: " + e, e);
        }
        /*
         *  Now that Velocity is initialized, cache some config.
         */
        defaultContentType =
                RuntimeSingleton.getString(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
	}
	
	
	
	 /**
     * The context key for the HTTP request object.
     */
    public static final String REQUEST = "req";

    /**
     * The context key for the HTTP response object.
     */
    public static final String RESPONSE = "res";

    /**
     * The HTTP content type context key.
     */
    public static final String CONTENT_TYPE = "default.contentType";

    /**
     *  The default content type for the response
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";


    /**
     *  Encoding for the output stream
     */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * The default content type, itself defaulting to {@link
     * #DEFAULT_CONTENT_TYPE} if not configured.
     */
    private static String defaultContentType;

    /**
     * This is the string that is looked for when getInitParameter is
     * called (<code>org.apache.velocity.properties</code>).
     */
    protected static final String INIT_PROPS_KEY =
        "org.apache.velocity.properties";


    /**
     * Cache of writers
     */

    private static SimplePool writerPool = new SimplePool(40);

  
   


    /**
     *  merges the template with the context.  Only override this if you really, really
     *  really need to. (And don't call us with questions if it breaks :)
     *
     *  @param template template object returned by the handleRequest() method
     *  @param context  context created by the createContext() method
     *  @param response servlet reponse (use this to get the output stream or Writer
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    protected void mergeTemplate( Template template, Context context, HttpServletResponse response )
        throws ResourceNotFoundException, ParseErrorException,
               MethodInvocationException, IOException, UnsupportedEncodingException, Exception
    {
        ServletOutputStream output = response.getOutputStream();
        VelocityWriter vw = null;
        // ASSUMPTION: response.setContentType() has been called.
        String encoding = response.getCharacterEncoding();

        try
        {
            vw = (VelocityWriter) writerPool.get();

            if (vw == null)
            {
                vw = new VelocityWriter(new OutputStreamWriter(output,
                                                               encoding),
                                        4 * 1024, true);
            }
            else
            {
                vw.recycle(new OutputStreamWriter(output, encoding));
            }

            template.merge(context, vw);
        }
        finally
        {
            if (vw != null)
            {
                try
                {
                    /*
                     *  flush and put back into the pool
                     *  don't close to allow us to play
                     *  nicely with others.
                     */
                    vw.flush();
                }
                catch (IOException e)
                {
                    // do nothing
                }

                /*
                 * Clear the VelocityWriter's reference to its
                 * internal OutputStreamWriter to allow the latter
                 * to be GC'd while vw is pooled.
                 */
                vw.recycle(null);
                writerPool.put(vw);
            }
        }
    }

    /**
     * Sets the content type of the response, defaulting to {@link
     * #defaultContentType} if not overriden.  Delegates to {@link
     * #chooseCharacterEncoding(HttpServletRequest)} to select the
     * appropriate character encoding.
     *
     * @param request The servlet request from the client.
     * @param response The servlet reponse to the client.
     */
    protected void setContentType(HttpServletRequest request,
                                  HttpServletResponse response)
    {
        String contentType = defaultContentType;
        int index = contentType.lastIndexOf(';') + 1;
        if (index <= 0 || (index < contentType.length() &&
                           contentType.indexOf("charset", index) == -1))
        {
            // Append the character encoding which we'd like to use.
            String encoding = chooseCharacterEncoding(request);
            //RuntimeSingleton.debug("Chose output encoding of '" +
            //                       encoding + '\'');
            if (!DEFAULT_OUTPUT_ENCODING.equalsIgnoreCase(encoding))
            {
                contentType += "; charset=" + encoding;
            }
        }
        response.setContentType(contentType);
        //RuntimeSingleton.debug("Response Content-Type set to '" +
        //                       contentType + '\'');
    }

    /**
     * Chooses the output character encoding to be used as the value
     * for the "charset=" portion of the HTTP Content-Type header (and
     * thus returned by <code>response.getCharacterEncoding()</code>).
     * Called by {@link #setContentType(HttpServletRequest,
     * HttpServletResponse)} if an encoding isn't already specified by
     * Content-Type.  By default, chooses the value of
     * RuntimeSingleton's <code>output.encoding</code> property.
     *
     * @param request The servlet request from the client.
     * @return The chosen character encoding.
     */
    protected String chooseCharacterEncoding(HttpServletRequest request)
    {
        return RuntimeSingleton.getString(RuntimeConstants.OUTPUT_ENCODING,
                                          DEFAULT_OUTPUT_ENCODING);
    }

    /**
     *  Returns a context suitable to pass to the handleRequest() method
     *  <br><br>
     *  Default implementation will create a VelocityContext object,
     *   put the HttpServletRequest and HttpServletResponse
     *  into the context accessable via the keys VelocityServlet.REQUEST and
     *  VelocityServlet.RESPONSE, respectively.
     *
     *  @param request servlet request from client
     *  @param response servlet reponse to client
     *
     *  @return context
     */
    protected Context createContext(HttpServletRequest request,  HttpServletResponse response )
    {
        /*
         *   create a new context
         */

        VelocityContext context = new VelocityContext();

        /*
         *   put the request/response objects into the context
         *   wrap the HttpServletRequest to solve the introspection
         *   problems
         */

        context.put( REQUEST,  request );
        context.put( RESPONSE, response );

        return context;
    }

    /**
     * Retrieves the requested template.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @return     The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate( String name )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate(name);
    }

    /**
     * Retrieves the requested template with the specified
     * character encoding.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @param encoding the character encoding of the template
     *
     * @return     The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     *
     *  @since Velocity v1.1
     */
    public Template getTemplate( String name, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate( name, encoding );
    }

   

    

    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     *
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param cause  Exception that was thrown by some other part of process.
     * @throws ServletException
     * @throws IOException
     */
    protected void error( HttpServletRequest request, HttpServletResponse response, Exception cause )
        throws ServletException, IOException
    {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<title>Error</title>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>VelocityServlet: Error processing the template</h2>");
        html.append("<pre>");
        String why = cause.getMessage();
        if (why != null && why.trim().length() > 0)
        {
            html.append(why);
            html.append("<br>");
        }

        StringWriter sw = new StringWriter();
        cause.printStackTrace( new PrintWriter( sw ) );

        html.append( sw.toString()  );
        html.append("</pre>");
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print( html.toString() );
    }





	
}
