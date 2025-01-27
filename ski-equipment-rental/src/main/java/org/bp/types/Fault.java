package org.bp.types;

public class Fault  extends Exception {

    protected int code;
    protected String text;
    // Constructor with message only
    public Fault(String text) {
        super(text);
        this.text = text;
    }

    // Constructor with code and message
    public Fault(int code, String text) {
        super(text);
        this.code = code;
        this.text = text;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

}