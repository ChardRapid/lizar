package lizar.email.domain;

import java.util.LinkedList;
import java.util.List;

import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.lizar.util.StringHelper;

public class EmailContainer {
	
	
	private LinkedList<InternetAddress> reply_to = new LinkedList<InternetAddress>();
	private LinkedList<String> send_to = new LinkedList<String>();
	private String subject = "";
	private String sender_name = "";
	
	private String sender = "";
	
	private String content;
	
	private String encode_type;
	
	public void reply_to(List<String> address) throws AddressException {
		if(address != null){
			for(String add:address){
				InternetAddress internet_address = new InternetAddress(add);
				reply_to.add(internet_address);
			}
		}
	}
	
	public void send_to(List<String> address) throws AddressException {
		send_to.addAll(address);
	}
	
	public void append_reply_to(String addresses) throws AddressException {
		reply_to.add(new InternetAddress(addresses));
	}
	
	public void append_send_to(String addresses) throws AddressException {
		send_to.add(addresses);
	}
	
	
	public List<InternetAddress> get_reply_to(){
		return reply_to;
	}
	
	public InternetAddress[] get_reply_to_array(){
		InternetAddress[] array=new InternetAddress[reply_to.size()];
		int i=0;
		for (InternetAddress addr : reply_to) {
			array[i]=addr;
			i++;
		}
		return array;
	}
	
	public List<String> get_send_to(){
		return send_to;
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSender_name() {
		return sender_name;
	}

	public void setSender_name(String sender_name) {
		this.sender_name = sender_name;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	

	public String getEncode_type() {
		return encode_type;
	}

	public void setEncode_type(String encode_type) {
		this.encode_type = encode_type;
	}

	public static String mail_url(String email){
		if(StringHelper.isNull(email))return "";
		if(email.endsWith("@qq.com")||email.equals("@vip.qq.com"))return "http://mail.qq.com";
		if(email.endsWith("@163.com"))return "http://mail.163.com";
		if(email.endsWith("@126.com"))return "http://mail.126.com";
		if(email.endsWith("@sina.com")||email.endsWith("@vip.sina.com"))return "http://mail.sina.com";
		if(email.endsWith("@hotmail.com"))return "http://hotmail.com";
		if(email.endsWith("@gmail.com"))return "http://mail.google.com"; 
		if(email.endsWith("@sohu.com"))return "http://mail.sohu.com";
		if(email.endsWith("@yahoo.cn"))return "http://mail.yahoo.cn";
		if(email.endsWith("@139.com"))return "http://mail.139.com";
		if(email.endsWith("@189.cn"))return "http://webmail4.189.cn/webmail/";
		if(email.endsWith("@wo.com.cn"))return "http://mail.wo.com.cn";
		return "";
	}
}
