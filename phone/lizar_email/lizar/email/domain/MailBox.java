package lizar.email.domain;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.lizar.web.config.Group;
import com.lizar.web.loader.Cell;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

import freemarker.template.TemplateException;

public class MailBox implements Cell {

	private Authenticator authenticator;
	
	public void send_mail(EmailContainer email) throws MessagingException, IOException, TemplateException {
		this._send(email);
	}
	

	private void _send( EmailContainer email) throws AddressException,MessagingException, IOException, TemplateException {
		Properties props = new Properties();
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.debug", "false"); 
		props.put("mail.smtp.host", "smtp.exmail.qq.com");
		props.put("mail.smtp.port","465");
		props.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(props, authenticator);
		MimeMessage message = new MimeMessage(session);
		message.setReplyTo(email.get_reply_to_array());
		message.setSubject(email.getSubject());
		message.setContent(email.getContent(), "text/html;charset="+email.getEncode_type());
		message.setSentDate(new Date());
		message.setFrom(new InternetAddress(email.getSender(), MimeUtility.encodeText(email.getSender_name())));

		for (String addr : email.get_send_to()) {
			message.addRecipient(Message.RecipientType.BCC,new InternetAddress(addr));
		}
		Transport.send(message);
	}


	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		Group.set_up("mailbox", "sender_email", "594382300@qq.com");
		Group.set_up("mailbox", "sender_password", "dwbMIANju");
		authenticator=new Authenticator(){
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Group._str("mailbox.sender_email"), Group._str("mailbox.sender_password"));
			}
		};
	}

	

}
