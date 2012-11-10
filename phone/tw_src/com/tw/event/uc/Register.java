package com.tw.event.uc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import lizar.email.domain.EmailContainer;
import lizar.email.domain.MailBox;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.bson.types.ObjectId;

import com.lizar.util.FileTool;
import com.lizar.util.MyMath;
import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.config.Group;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.domain.uc.UserCenter;
import com.tw.domain.uc.element.Email;
import com.tw.domain.uc.element.Gender;
import com.tw.domain.uc.element.Name;
import com.tw.domain.uc.element.Password;
import com.tw.domain.uc.element.Status;
import com.tw.event.tool.Vcode;
import com.tw.persistence.AutoTokenDao;
import com.tw.persistence.RegisterDao;
import com.tw.persistence.UserDetailDao;

import freemarker.template.TemplateException;

public class Register extends Event {
	private UserCenter ucenter;
	private RegisterDao register_dao;
	private UserDetailDao user_detail_dao;
	private AutoTokenDao auto_token_dao;
	private MailBox mail_box;
	
	private static Properties props = new Properties();  
	  
    static {  
        props.setProperty(Velocity.INPUT_ENCODING, "UTF-8");  
        props.setProperty(Velocity.RESOURCE_LOADER, "class");  
        props.setProperty("class.resource.loader.class",  
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");  
    }  
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		ucenter=Web.get(UserCenter.class);
		user_detail_dao=Web.get(UserDetailDao.class);
		register_dao=Web.get(RegisterDao.class);
		auto_token_dao=Web.get(AutoTokenDao.class);
		mail_box=Web.get(MailBox.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/register/*";
	}

	@Override
	public void handle(EventLoader event_loader) throws ServletException,
			IOException {
		if(event_loader.get_attr("uc")!=null){
			event_loader.response(Config.xpath_str("server_info.root"));
		}else{
			if(event_loader.request_path(1).equals("validate"))validate(event_loader);
			else if(event_loader.request_path(1).equals("verified"))verified(event_loader);
			else{
				String email=event_loader._str("email","").toLowerCase().trim();
				String name= event_loader._str("name","").trim();
				String password=event_loader._str("password","").trim();
				String vcode=event_loader._str("vcode","").toLowerCase().trim();
				String agree=event_loader._str("agree","");
				if(StringHelper.isNull(email)){
					event_loader.template("/WEB-INF/template/register.vm");
					return;
				}
				String msg=validate(event_loader.lan,email,name,password,vcode,event_loader.get_session_attr(Vcode.NAME,""),agree);
				if(StringHelper.isNull(msg)){
					register_step1(event_loader,email,name,password);
					return;
				}
				event_loader.set_attr("msg", msg);
				event_loader.set_attr("email", email);
				event_loader.set_attr("name",name);
				event_loader.template("/WEB-INF/template/register.vm");
			}
		}
	}
	
	private void register_step1(EventLoader el,String email,String name,String password) throws IOException, ServletException{
		Entity e=new MEntity();
		String code=MyMath.encryptionWithMD5(el.session_id()+System.currentTimeMillis());
		e.put("email", email);
		e.put("name", name);
		e.put("password", MyMath.encryptionWithMD5(password));
		e.put("code", code);
		register_dao.upsert(e);
		try{
			send_email(email,name,code,el);
		}catch(Exception e1){
			e1.printStackTrace();
			el.template("/WEB-INF/template/register_send_email_failed.vm");
			return;
		}
		//send email
		el.template("/WEB-INF/template/register_send_email.vm");
	}

	private void send_email(String email,String name,String code,EventLoader el) throws MessagingException, IOException, TemplateException{
		EmailContainer mail =new EmailContainer();
		String url=Config.xpath_str("server_info.root")+"/register/validate?code="+code;
		mail.setSender(Group._str("mailbox.sender_email"));
		mail.setSender_name("甜味使者");
		mail.setEncode_type(this.encode_type);
		mail.setSubject("欢迎注册36甜味");
		mail.setContent(translate_register_template(url,name,email));
		mail.append_send_to(email);
		mail_box.send_mail(mail);
	}
	
	private String translate_register_template(String url,String name,String email) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, FileNotFoundException, IOException{
		        VelocityEngine engine = new VelocityEngine(props);  
		        VelocityContext context = new VelocityContext();
	            context.put("name", name);
	            context.put("email", email); 
	            context.put("url", url); 
		        StringWriter writer = new StringWriter(); 
		        String template=FileTool.read_it(new File(Config.path_filter("/WEB-INF/email_template/register_template.vm")),this.encode_type);
		        engine.evaluate(context, writer, "",template );  
		        return writer.toString();
	}
	
	private void verified(EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/register_email_verified.vm");
	}
	
	private void validate(EventLoader el) throws IOException, ServletException{
		String code=el._str("code","");
		if(StringHelper.isNotNull(code)){
			Entity user=register_dao.find_and_remove_by_code(code);
			if(user==null){
				el.template("/WEB-INF/template/register_email_validate_failed.vm");
				return;
			}
			String session_id= MyMath.encryptionWithMD5(el.session_id()+System.currentTimeMillis());
			user.removeField("code");
			user.put("desc", "");
			user.put("gender", Gender.UN_KNOWN);
			user.put("head", "");
			user.put("new_msg_no", 0);
			user.put("status", Status.NORMAL);
			user.put("_id",new ObjectId());
			user_detail_dao.insert(user);
			ucenter.register_login(session_id,user);
			auto_token_dao.insert(user._obj_id("_id"),session_id, System.currentTimeMillis()+Keys._int("session_auto_time_out")*1000);
			Cookie c=new Cookie("token",session_id);
			c.setMaxAge(Keys._int("session_auto_time_out"));
			el.response(Config.xpath_str("server_info.root")+"/register/verified", c);
			return;
		}
		el.template("/WEB-INF/template/register_email_validate_failed.vm");
	}
	
	
	
	private String validate(String lan,String email,String name,String password,String vcode,String svcode,String agree){
			String msg = Vcode.validate( lan,vcode,svcode);
			if (msg != null)
				return msg;
			msg = Password.validate(lan,password);
			if (msg != null)
				return msg;
			msg = Name.validate(lan, name);
			if (msg != null)
				return msg;
			if(!StringHelper.equals(agree, "true")){
				return Web.i18.get(lan, "register.agreement_needed");
			}
			msg = Email.validate(lan, email);
			if (msg != null)
				return msg;
			return msg;
	}
	
	

	
	
	@Override
	public void handle_jsonp(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle_json(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		System.out.println("register json:"+event_loader.request_path());
	}

	@Override
	public void handle_xml(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void before(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}



}
