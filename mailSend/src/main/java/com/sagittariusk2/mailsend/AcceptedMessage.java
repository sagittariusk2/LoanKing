package com.sagittariusk2.mailsend;

import java.time.LocalDate;

public class AcceptedMessage extends MessageBody{

    private String senderName;
    private String senderEmail;
    private String receiverEmail;
    private String receiverName;
    private String loanRequestID;
    private String loanAmount;
    private String loanRate;
    private String loanDuration;
    private String senderContact;

    public AcceptedMessage(String senderName, String senderEmail, String receiverEmail, String receiverName, String loanRequestID, String loanAmount, String loanRate, String loanDuration, String senderContact) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.receiverName = receiverName;
        this.loanRequestID = loanRequestID;
        this.loanAmount = loanAmount;
        this.loanRate = loanRate;
        this.loanDuration = loanDuration;
        this.senderContact = senderContact;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getLoanRequestID() {
        return loanRequestID;
    }

    public void setLoanRequestID(String loanRequestID) {
        this.loanRequestID = loanRequestID;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getLoanRate() {
        return loanRate;
    }

    public void setLoanRate(String loanRate) {
        this.loanRate = loanRate;
    }

    public String getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(String loanDuration) {
        this.loanDuration = loanDuration;
    }

    @Override
    public String getMessage() {
        return "<div style=\"margin: 30px;\">\n" +
                "        <h2>Acceptance Letter</h2>\n" +
                "        <br><br>\n" +
                "        <hr>\n" +
                "        <div style=\"line-height:1.5;font-style: italic;font-size:16px\">\n" +
                "            "+senderName+"<br>\n" +
                "            <a href=\"mailto: "+senderEmail+"\">"+senderEmail+"</a><br>\n" +
                "            Contact : "+senderContact+"\n" +
                "        </div>\n" +
                "        <hr>\n" +
                "            <h6>\n" +
                "                Date : "+ LocalDate.now().toString() +"\n" +
                "            </h6>\n" +
                "        <hr>\n" +
                "        <h5>\n" +
                "            Hi "+receiverName+"</Name><br>\n" +
                "            <a href=\"mailto: receiverEmail\">"+receiverEmail+"</a><br><br>\n" +
                "        </h5>\n" +
                "        \n" +
                "        <div>\n" +
                "            Your Loan Request with ID <strong>"+loanRequestID+"</strong> has been <strong style=\"color: green\">accepted</strong> by <strong>"+senderName+"</strong>. The contact details of <strong>"+senderName+"</strong> has been shared with you in this mail.\n" +
                "        </div>\n" +
                "\n" +
                "        <div style=\"margin: 20px;width:100%\">\n" +
                "            <table style=\"border: 1px solid black;width:70%\">\n" +
                "                <tr>\n" +
                "                    <th style=\"border: 1px solid black;\">Amount</th>\n" +
                "                    <th style=\"border: 1px solid black;\">Rate</th>\n" +
                "                    <th style=\"border: 1px solid black;\">Duration</th>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"border: 1px solid black;\">"+loanAmount+"</td>\n" +
                "                    <td style=\"border: 1px solid black;\">"+loanRate+"%</td>\n" +
                "                    <td style=\"border: 1px solid black;\">"+loanDuration+"</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "\n" +
                "        <div>\n" +
                "            If you are facing any issues then reach out to us <a href=\"mailto: loankingshivamritesh@gmail.com\">loankingshivamritesh@gmail.com </a>.\n" +
                "        </div>\n" +
                "\n" +
                "    </div>";
    }

    @Override
    public String getSubject() {
        return "Hey!!! "+senderName+" has ACCEPTED your loan request "+loanRequestID;
    }
}
