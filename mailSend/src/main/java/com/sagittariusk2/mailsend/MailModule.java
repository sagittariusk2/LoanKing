package com.sagittariusk2.mailsend;

public class MailModule {

    public MailModule() { }

    public void sendMail(String email, MessageBody messageBody) {
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender("loankingshivamritesh@gmail.com", "shivamritesh");
                sender.sendMail(messageBody.getSubject(), "<b>"+messageBody.getMessage()+"</b>", "loankingshivamritesh@gmail.com", email);
            } catch (Exception ignored) {

            }
        }).start();
    }
}
