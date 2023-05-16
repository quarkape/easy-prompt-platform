package club.hue.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.MailSSLSocketFactory;

public class SendMailUtil {
    //邮件服务器主机名
    // QQ邮箱的 SMTP 服务器地址为: smtp.qq.com
    private static String myEmailSMTPHost = "smtp.qq.com";

    //发件人邮箱，这里替换成你自己的邮箱就可以了，不过注意qq邮箱每天发邮件的数量是有限制的
    private static String myEmailAccount = "quarkape@qq.com";

    //发件人邮箱密码（授权码）
    //在开启SMTP服务时会获取到一个授权码，把授权码填在这里
    private static String myEmailPassword = "cspyhaclxvcadife";

    public static void sendEmail(String receiver, String message, String topic) throws Exception {

        Properties props = new Properties();

        props.setProperty("mail.debug", "false");

        props.setProperty("mail.smtp.auth", "true");

        props.put("mail.smtp.port", 465);

        props.setProperty("mail.smtp.host", myEmailSMTPHost);

        props.setProperty("mail.transport.protocol", "smtp");

        // SSL认证
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);

        // ssl安全连接
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.socketFactory", sf);

        // 创建会话
        Session session = Session.getInstance(props);

        // 获取邮件对象
        Message msg = new MimeMessage(session);

        // 设置邮件标题
        msg.setSubject(topic);

        // 设置邮件内容
        StringBuilder builder = new StringBuilder();

        // 写入内容
        builder.append(message);

        // 设置显示的发件时间
        msg.setSentDate(new Date());

        // 设置邮件内容
        msg.setContent(builder.toString(), "text/html;charset=UTF-8");

        // 设置发件人邮箱
        msg.setFrom(new InternetAddress(myEmailAccount, "Easy Prompt", "UTF-8"));

        // 得到邮差对象
        Transport transport = session.getTransport();

        // 连接自己的邮箱账户
        transport.connect(myEmailSMTPHost, myEmailAccount, myEmailPassword);

        // 发送邮件
        transport.sendMessage(msg, new Address[]{new InternetAddress(receiver)});

        //将该邮件保存到本地
//        OutputStream out = new FileOutputStream("MyEmail.eml");
//        msg.writeTo(out);
//        out.flush();
//        out.close();

        transport.close();
    }
}
