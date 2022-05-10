package com.devwarriors.loanking.Data;

import java.io.Serializable;

public class User implements Serializable  {
    private String name;
    private String dob;
    private String id;
    private String email;
    private String phone;
    private String aadharNo;
    private String aadharImage;
    private String panNo;
    private String panImage;
    private String salary;
    private String salaryImage;
    private String profileImage;
    private String bankAccountNo;
    private String bankIFSC;
    private String bankBalance;
    private String ctc;
    private int cibilScore;
    private int loanAmtReqSoFar;
    private int loanFreq;

    public User()
    {
        this.name = "";
        this.dob = "";
        this.id = "";
        this.email = "";
        this.phone = "";
        this.aadharNo = "";
        this.aadharImage = "";
        this.panNo = "";
        this.panImage = "";
        this.salary = "";
        this.salaryImage = "";
        this.profileImage = "";
        this.bankAccountNo = "";
        this.bankIFSC = "";
        this.bankBalance = "";
        this.ctc = "";
        this.cibilScore=300;
        this.loanAmtReqSoFar=0;
        this.loanFreq=0;
    }

    public User(String id) {
        this.id = id;
    }

    public User(String name, String dob, String id, String email, String phone, String aadharNo, String aadharImage, String panNo, String panImage, String salary, String salaryImage, String profileImage, String bankAccountNo, String bankIFSC, String bankBalance, String ctc) {
        this.name = name;
        this.dob = dob;
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.aadharNo = aadharNo;
        this.aadharImage = aadharImage;
        this.panNo = panNo;
        this.panImage = panImage;
        this.salary = salary;
        this.salaryImage = salaryImage;
        this.profileImage = profileImage;
        this.bankAccountNo = bankAccountNo;
        this.bankIFSC = bankIFSC;
        this.bankBalance = bankBalance;
        this.ctc = ctc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }

    public String getAadharImage() {
        return aadharImage;
    }

    public void setAadharImage(String aadharImage) {
        this.aadharImage = aadharImage;
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getPanImage() {
        return panImage;
    }

    public void setPanImage(String panImage) {
        this.panImage = panImage;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getSalaryImage() {
        return salaryImage;
    }

    public void setSalaryImage(String salaryImage) {
        this.salaryImage = salaryImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public String getBankIFSC() {
        return bankIFSC;
    }

    public void setBankIFSC(String bankIFSC) {
        this.bankIFSC = bankIFSC;
    }

    public String getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(String bankBalance) {
        this.bankBalance = bankBalance;
    }

    public String getCtc() {
        return ctc;
    }

    public void setCtc(String ctc) {
        this.ctc = ctc;
    }

    public int getCibilScore() {
        return cibilScore;
    }

    public void setCibilScore(int cibilScore) {
        this.cibilScore = cibilScore;
    }

    public int getLoanAmtReqSoFar() {
        return loanAmtReqSoFar;
    }

    public void setLoanAmtReqSoFar(int loanAmtReqSoFar) {
        this.loanAmtReqSoFar = loanAmtReqSoFar;
    }

    public int getLoanFreq() {
        return loanFreq;
    }

    public void setLoanFreq(int loanFreq) {
        this.loanFreq = loanFreq;
    }
}
