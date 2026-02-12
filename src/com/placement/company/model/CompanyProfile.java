package com.placement.company.model;

public class CompanyProfile {
    private final String companyName;
    private final String phone;
    private final String website;
    private final String industry;
    private final String companySize;
    private final String address;

    public CompanyProfile(String companyName, String phone, String website,
                          String industry, String companySize, String address) {
        this.companyName = companyName;
        this.phone = phone;
        this.website = website;
        this.industry = industry;
        this.companySize = companySize;
        this.address = address;
    }

    public String getCompanyName() { return companyName; }
    public String getPhone() { return phone; }
    public String getWebsite() { return website; }
    public String getIndustry() { return industry; }
    public String getCompanySize() { return companySize; }
    public String getAddress() { return address; }
}
