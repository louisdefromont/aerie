package org.eaa690.aerie.model;

public class Avea {

    private String msg = "";

    private boolean granted = false;

    private int seconds = 5;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><AVEA>");
        if (isGranted()) {
            //sb.append("MSG=");
            //sb.append(getMsg());
            sb.append("GRNT=");
            sb.append(String.format("%02d", seconds));
        } else {
            sb.append("DENY");
        }
        sb.append("</AVEA></body></html>");
        return sb.toString();
    }
}
