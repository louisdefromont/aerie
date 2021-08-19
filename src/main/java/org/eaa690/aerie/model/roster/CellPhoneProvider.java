package org.eaa690.aerie.model.roster;

public enum CellPhoneProvider {
    ATT("txt.att.net"),
    BOOST_MOBILE("sms.myboostmobile.com"),
    CRICKET_WIRELESS("mms.cricketwireless.net"),
    GOOGLE_POJECT_FI("msg.fi.google.com"),
    REPUBLIC_WIRELESS("text.republicwireless.com"),
    SPRINT("messaging.sprintpcs.com"),
    STRAIGHT_TALK("vtext.com"),
    T_MOBILE("tmomail.net"),
    TING("message.ting.com"),
    US_CELLULAR("email.uscc.net"),
    VERIZON("vtext.com"),
    VIRGIN_MOBILE("vmol.com");

    private String cellPhoneProviderEmailDomain;
    private CellPhoneProvider(String cellPhoneProviderEmailDomain) {
        this.cellPhoneProviderEmailDomain = cellPhoneProviderEmailDomain;
    }

    public String getCellPhoneProviderEmailDomain() {
        return cellPhoneProviderEmailDomain;
    }
}
