package com.tw.event.tool;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;

import com.lizar.util.MyMath;
import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;

public class Vcode extends Event{

	public static final String NAME="vcode";
	
	private Color _get_rand_color(int fc,int bc){//给定范围获得随机颜色
        Random random = new Random();
        if(fc>255) fc=255;
        if(bc>255) bc=255;
        int r=fc+random.nextInt(bc-fc);
        int g=fc+random.nextInt(bc-fc);
        int b=fc+random.nextInt(bc-fc);
        return new Color(r,g,b);
    }

	private String generate_vcode(){
		long timestamp=Math.round(Math.random()*System.nanoTime());
		String key=MyMath._10_to_60(timestamp).substring(0, 4);
		return key;
	}
	
	
	public void handle(EventLoader eventLoader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
		// 在内存中创建图象
		int width=60, height=30;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// 获取图形上下文
		Graphics g = image.getGraphics();

		//生成随机类
		Random random = new Random();

		// 设定背景色
		g.setColor(_get_rand_color(200,250));
		g.fillRect(0, 0, width, height);

		//设定字体
		g.setFont(new Font("Times New Roman",Font.PLAIN,18));


		// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(_get_rand_color(160,200));
		for (int i=0;i<155;i++)
		{
		 int x = random.nextInt(width);
		 int y = random.nextInt(height);
		        int xl = random.nextInt(12);
		        int yl = random.nextInt(12);
		 g.drawLine(x,y,x+xl,y+yl);
		}

		// 取随机产生的认证码(4位数字)
		String sRand=generate_vcode();
		eventLoader.set_session_attr(NAME,sRand);
		for (int i=0;i<4;i++){
			String rand=null;
			try{
		     rand=String.valueOf(sRand.charAt(i));
			}catch(Exception e){
				e.printStackTrace();
			}
		    g.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
		    g.drawString(rand,13*i+6,25);
		}

		// 绘制图象
		g.dispose();
		eventLoader.write_image(image,eventLoader.JPEG);
		
	}
	
	public static String validate(String lan,String vcode,String svcode) {
		if (StringHelper.isNull(vcode))
			return Web.i18.get(lan, "register.vcode_is_null");
		if (vcode.equals(svcode)) {
			return  Web.i18.get(lan, "register.vcode_is_wrong");
		}
		return null;
	}


	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/vcode";
	}


	@Override
	public void handle_jsonp(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handle_json(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
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
		event_loader.setHeader("Pragma","No-cache");
		event_loader.setHeader("Cache-Control","no-cache");
		event_loader.setDateHeader("Expires", 0);
	}
}
