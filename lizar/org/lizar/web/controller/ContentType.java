package org.lizar.web.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ContentType {
	private static Map<String,String> type;
	
	public static void init(){
		type=new HashMap<String,String>();
		type.put("3gp","video/3gpp");
		type.put("a","application/octet-stream");
		type.put("ai","application/postscript");
		type.put("aif","audio/x-aiff");
		type.put("aiff","audio/x-aiff");
		type.put("asc","application/pgp-signature");
		type.put("asf","video/x-ms-asf");
		type.put("asm","text/x-asm");
		type.put("asx","video/x-ms-asf");
		type.put("atom","application/atom+xml");
		type.put("au","audio/basic");
		type.put("avi","video/x-msvideo");
		type.put("bat","application/x-msdownload");
		type.put("bin","application/octet-stream");
		type.put("bmp","image/bmp");
		type.put("bz2","application/x-bzip2");
		type.put("c","text/x-c");
		type.put("cab","application/vndms-cab-compressed");
		type.put("cc","text/x-c");
		type.put("chm","application/vndms-htmlhelp");
		type.put("class","application/octet-stream");
		type.put("com","application/x-msdownload");
		type.put("conf","text/plain");
		type.put("cpp","text/x-c");
		type.put("crt","application/x-x509-ca-cert");
		type.put("css","text/css");
		type.put("csv","text/csv");
		type.put("cxx","text/x-c");
		type.put("deb","application/x-debian-package");
		type.put("der","application/x-x509-ca-cert");
		type.put("diff","text/x-diff");
		type.put("djv","image/vnddjvu");
		type.put("djvu","image/vnddjvu");
		type.put("dll","application/x-msdownload");
		type.put("dmg","application/octet-stream");
		type.put("doc","application/msword");
		type.put("dot","application/msword");
		type.put("dtd","application/xml-dtd");
		type.put("dvi","application/x-dvi");
		type.put("ear","application/java-archive");
		type.put("eml","message/rfc822");
		type.put("eps","application/postscript");
		type.put("exe","application/x-msdownload");
		type.put("f","text/x-fortran");
		type.put("f77","text/x-fortran");
		type.put("f90","text/x-fortran");
		type.put("flv","video/x-flv");
		type.put("for","text/x-fortran");
		type.put("gem","application/octet-stream");
		type.put("gemspec","text/x-scriptruby");
		type.put("gif","image/gif");
		type.put("gz","application/x-gzip");
		type.put("h","text/x-c");
		type.put("hh","text/x-c");
		type.put("htm","text/html");
		type.put("html","text/html");
		type.put("ico","image/vndmicrosofticon");
		type.put("ics","text/calendar");
		type.put("ifb","text/calendar");
		type.put("iso","application/octet-stream");
		type.put("jar","application/java-archive");
		type.put("java","text/x-java-source");
		type.put("jnlp","application/x-java-jnlp-file");
		type.put("jpeg","image/jpeg");
		type.put("jpg","image/jpeg");
		type.put("js","application/javascript");
		type.put("json","application/json");
		type.put("log","text/plain");
		type.put("m3u","audio/x-mpegurl");
		type.put("m4v","video/mp4");
		type.put("man","text/troff");
		type.put("mathml","application/mathml+xml");
		type.put("mbox","application/mbox");
		type.put("mdoc","text/troff");
		type.put("me","text/troff");
		type.put("mid","audio/midi");
		type.put("midi","audio/midi");
		type.put("mime","message/rfc822");
		type.put("mml","application/mathml+xml");
		type.put("mng","video/x-mng");
		type.put("mov","video/quicktime");
		type.put("mp3","audio/mpeg");
		type.put("mp4","video/mp4");
		type.put("mp4v","video/mp4");
		type.put("mpeg","video/mpeg");
		type.put("mpg","video/mpeg");
		type.put("ms","text/troff");
		type.put("msi","application/x-msdownload");
		type.put("odp","application/vndoasisopendocumentpresentation");
		type.put("ods","application/vndoasisopendocumentspreadsheet");
		type.put("odt","application/vndoasisopendocumenttext");
		type.put("ogg","application/ogg");
		type.put("p","text/x-pascal");
		type.put("pas","text/x-pascal");
		type.put("pbm","image/x-portable-bitmap");
		type.put("pdf","application/pdf");
		type.put("pem","application/x-x509-ca-cert");
		type.put("pgm","image/x-portable-graymap");
		type.put("pgp","application/pgp-encrypted");
		type.put("pkg","application/octet-stream");
		type.put("pl","text/x-scriptperl");
		type.put("pm","text/x-scriptperl-module");
		type.put("png","image/png");
		type.put("pnm","image/x-portable-anymap");
		type.put("ppm","image/x-portable-pixmap");
		type.put("pps","application/vndms-powerpoint");
		type.put("ppt","application/vndms-powerpoint");
		type.put("ps","application/postscript");
		type.put("psd","image/vndadobephotoshop");
		type.put("py","text/x-scriptpython");
		type.put("qt","video/quicktime");
		type.put("ra","audio/x-pn-realaudio");
		type.put("rake","text/x-scriptruby");
		type.put("ram","audio/x-pn-realaudio");
		type.put("rar","application/x-rar-compressed");
		type.put("rb","text/x-scriptruby");
		type.put("rdf","application/rdf+xml");
		type.put("roff","text/troff");
		type.put("rpm","application/x-redhat-package-manager");
		type.put("rss","application/rss+xml");
		type.put("rtf","application/rtf");
		type.put("ru","text/x-scriptruby");
		type.put("s","text/x-asm");
		type.put("sgm","text/sgml");
		type.put("sgml","text/sgml");
		type.put("sh","application/x-sh");
		type.put("sig","application/pgp-signature");
		type.put("snd","audio/basic");
		type.put("so","application/octet-stream");
		type.put("svg","image/svg+xml");
		type.put("svgz","image/svg+xml");
		type.put("swf","application/x-shockwave-flash");
		type.put("t","text/troff");
		type.put("tar","application/x-tar");
		type.put("tbz","application/x-bzip-compressed-tar");
		type.put("tcl","application/x-tcl");
		type.put("tex","application/x-tex");
		type.put("texi","application/x-texinfo");
		type.put("texinfo","application/x-texinfo");
		type.put("text","text/plain");
		type.put("tif","image/tiff");
		type.put("tiff","image/tiff");
		type.put("torrent","application/x-bittorrent");
		type.put("tr","text/troff");
		type.put("txt","text/plain");
		type.put("vcf","text/x-vcard");
		type.put("vcs","text/x-vcalendar");
		type.put("vrml","model/vrml");
		type.put("war","application/java-archive");
		type.put("wav","audio/x-wav");
		type.put("wma","audio/x-ms-wma");
		type.put("wmv","video/x-ms-wmv");
		type.put("wmx","video/x-ms-wmx");
		type.put("wrl","model/vrml");
		type.put("wsdl","application/wsdl+xml");
		type.put("xbm","image/x-xbitmap");
		type.put("xhtml","application/xhtml+xml");
		type.put("xls","application/vndms-excel");
		type.put("xml","application/xml");
		type.put("xpm","image/x-xpixmap");
		type.put("xsl","application/xml");
		type.put("xslt","application/xslt+xml");
		type.put("yaml","text/yaml");
		type.put("yml","text/yaml");
		type.put("zip","application/zip");
	}
	
	public static String is(String postfix){
		if(postfix.equals(""))return "text/html";
		String tt=type.get(postfix);
		if(tt==null)return "text/html";
		return tt;
	}
	
	
	public static void main(String[] args) throws ParseException{
		
	}
}
