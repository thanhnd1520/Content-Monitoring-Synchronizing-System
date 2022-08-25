/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.nio.ByteBuffer;

/**
 *
 * @author phanpc@gmail.com
 */
public class TLVAttribute {

    /**
     * @return the tag
     */
    public short getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(short tag) {
        this.tag = tag;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return (short)(length + 2 + 2);
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void flat(ByteBuffer outBuf) {             
        outBuf.putShort(length);
        outBuf.putShort(tag);
        
        if (this.data != null)
            outBuf.put(data);
    }
    
    public void parse(ByteBuffer inBuf) {        
        this.length = inBuf.getShort();
        this.tag = inBuf.getShort(); 
        
        if (this.length > 0) {
            this.data = new byte[this.length];
            inBuf.get(this.data);
        }
    }
    
    public TLVAttribute(short tag, short length, byte [] data) {
        this.tag = tag;
        this.length = length;
        this.data = data;
    }
    
    public TLVAttribute() {
        
    }
    
    private short tag;
    private short length;
    private byte [] data;
}
