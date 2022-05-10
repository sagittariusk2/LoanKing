package com.sagittariusk2.mailsend;

import java.time.LocalDate;

public class ModifiedMessage extends MessageBody {

    private String senderName;
    private String senderEmail;
    private String receiverEmail;
    private String receiverName;
    private String loanRequestID;
    private String loanAmount, modifiedLoanAmount;
    private String loanRate, modifiedLoanRate;
    private String loanDuration, modifiedLoanDuration;
    private String senderContact;

    public ModifiedMessage(String senderName, String senderEmail, String receiverEmail, String receiverName, String loanRequestID, String loanAmount, String modifiedLoanAmount, String loanRate, String modifiedLoanRate, String loanDuration, String modifiedLoanDuration, String senderContact) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.receiverName = receiverName;
        this.loanRequestID = loanRequestID;
        this.loanAmount = loanAmount;
        this.modifiedLoanAmount = modifiedLoanAmount;
        this.loanRate = loanRate;
        this.modifiedLoanRate = modifiedLoanRate;
        this.loanDuration = loanDuration;
        this.modifiedLoanDuration = modifiedLoanDuration;
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

    public String getModifiedLoanAmount() {
        return modifiedLoanAmount;
    }

    public void setModifiedLoanAmount(String modifiedLoanAmount) {
        this.modifiedLoanAmount = modifiedLoanAmount;
    }

    public String getLoanRate() {
        return loanRate;
    }

    public void setLoanRate(String loanRate) {
        this.loanRate = loanRate;
    }

    public String getModifiedLoanRate() {
        return modifiedLoanRate;
    }

    public void setModifiedLoanRate(String modifiedLoanRate) {
        this.modifiedLoanRate = modifiedLoanRate;
    }

    public String getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(String loanDuration) {
        this.loanDuration = loanDuration;
    }

    public String getModifiedLoanDuration() {
        return modifiedLoanDuration;
    }

    public void setModifiedLoanDuration(String modifiedLoanDuration) {
        this.modifiedLoanDuration = modifiedLoanDuration;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    @Override
    public String getMessage() {
        return "<div style=\"margin: 30px;\">\n" +
                "        <h2>Loan Modification Letter</h2>\n" +
                "        <br>\n" +
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
                "            Hi, "+receiverName+"<br>\n" +
                "            <a href=\"mailto: "+receiverEmail+"\">"+receiverEmail+"</a><br>\n" +
                "        </h5>\n" +
                "        \n" +
                "        <div>\n" +
                "            <strong>"+senderName+"</strong> has requested to modify your loan request ID <strong>"+loanRequestID+"</strong>. Your original loan request was :\n" +
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
                "            The modification request by <strong>"+senderName+"</strong> is as follows :\n" +
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
                "                    <td style=\"border: 1px solid black;\">"+modifiedLoanAmount+"</td>\n" +
                "                    <td style=\"border: 1px solid black;\">"+modifiedLoanRate+"%</td>\n" +
                "                    <td style=\"border: 1px solid black;\">"+modifiedLoanDuration+"</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "\n" +
                "        <div>\n" +
                "            If you are interested in the suggested request, you can proceed for further action in the mobile app.\n" +
                "        </div><br>\n" +
                "\n" +
                "        <div>\n" +
                "            If you are facing any issues then reach out to us <a href=\"mailto: loankingshivamritesh@gmail.com\">loankingshivamritesh@gmail.com </a>.\n" +
                "        </div>\n" +
                "\n" +
                "    </div>";
    }

    @Override
    public String getSubject() {
        return "Update!!! "+senderName+" has requested to UPDATE your loan ID "+loanRequestID;
    }
}
