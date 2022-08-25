/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author phanpc@gmail.com
 */
public class TLVMessage {

    /**
     * @return the length
     */
    public int getLength() {
        //if (length == 0)
        calculateLength();
        
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

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
    
    public void addString(short attrTag, String val) {        
        addBytes(attrTag, val.getBytes(StandardCharsets.US_ASCII));
    }
    
    public void addBytes(short attrTag, byte [] val) {        
        TLVAttribute attr = new TLVAttribute(attrTag, (short)val.length, val);
        attrMap.put(attrTag, attr);        
    }
    
    public void addInt(short attrTag, int val) {
        ByteBuffer buf = ByteBuffer.allocate(4);        
        buf.putInt(val);
        
        TLVAttribute attr = new TLVAttribute(attrTag, (short)4, buf.array());
        attrMap.put(attrTag, attr);
    }
    
    public void addNull(short attrTag) {
        TLVAttribute attr = new TLVAttribute(attrTag, (short)0, null);
        attrMap.put(attrTag, attr);
    }
    
    public void addShort(short attrTag, short val) {
        ByteBuffer buf = ByteBuffer.allocate(2);      
        buf.putShort(val);
        
        TLVAttribute attr = new TLVAttribute(attrTag, (short)2, buf.array());
        attrMap.put(attrTag, attr);
    }
    
    public void addByte(short attrTag, byte val) {
        ByteBuffer buf = ByteBuffer.allocate(1);        
        buf.put(val);
        
        TLVAttribute attr = new TLVAttribute(attrTag, (short)1, buf.array());
        attrMap.put(attrTag, attr);
    }
    
    public byte [] getBytes(short attrTag) {
        TLVAttribute attr = attrMap.get(attrTag);
        if (attr != null) {
            return attr.getData();
        }
        
        return null;
    }
    
    public Byte getByte(short attrTag) {
        TLVAttribute attr = attrMap.get(attrTag);
        if (attr != null) {            
            ByteBuffer buf = ByteBuffer.allocate(attr.getData().length);
            buf.put(attr.getData());
            buf.flip();
            return buf.get();
        }
        
        return null;
    }
    
    public Integer getInt(short attrTag) {
        TLVAttribute attr = attrMap.get(attrTag);
        if (attr != null) {            
            ByteBuffer buf = ByteBuffer.allocate(attr.getData().length);
            buf.put(attr.getData());
            buf.flip();
            return buf.getInt();
        }
        
        return null;
    }
    
    public Long getLong(short attrTag) {
        TLVAttribute attr = attrMap.get(attrTag);
        if (attr != null) {            
            ByteBuffer buf = ByteBuffer.allocate(attr.getData().length);
            buf.put(attr.getData());
            buf.flip();
            return buf.getLong();
        }
        
        return null;
    }
    
    public Short getShort(short attrTag) {
        TLVAttribute attr = attrMap.get(attrTag);
        if (attr != null) {            
            ByteBuffer buf = ByteBuffer.allocate(attr.getData().length);
            buf.put(attr.getData());
            buf.flip();
            return buf.getShort();
        }
        
        return null;
    }
    
    public String getString(short attrTag) {
        TLVAttribute attr = attrMap.get(attrTag);
        if (attr != null) {           
            return new String(attr.getData(), StandardCharsets.US_ASCII);
        }
        
        return null;
    }
    
    public TLVAttribute getAttribute(short attrTag) {
        return attrMap.get(attrTag);
    }
    
    public void parse(int length, byte [] msgData) {
        ByteBuffer buf = ByteBuffer.wrap(msgData);
        
        this.length = length;
        this.tag = buf.getShort();
        
        while (buf.remaining() >= 4) {
            TLVAttribute attr = new TLVAttribute();
            attr.parse(buf);
            
            attrMap.put(attr.getTag(), attr);
        }
    }
    
    public byte [] flat() {
        //caculate length
        this.length = getLength();
        ByteBuffer buf = ByteBuffer.allocate(length+4);
        buf.putInt(length);
        buf.putShort(tag);
        
        for (Map.Entry<Short, TLVAttribute> entry: attrMap.entrySet()) {
            entry.getValue().flat(buf);
        }
        
        return buf.array();
    }
    
    public TLVMessage(short msgType) {
        this.tag = msgType;
    }
    
    public TLVMessage() {
        
    }
    
    private int length = 0; // invalid length
    private short tag;
    protected Map<Short, TLVAttribute> attrMap = new TreeMap();

    private int calculateLength() {
        length = 2;
        for (Map.Entry<Short, TLVAttribute> entry: attrMap.entrySet()) {
            length += entry.getValue().getLength();
        }
                
        return length;
    }
    
    public static void main(String [] args) {
        TLVMessage msg = new TLVMessage((short)1);
        System.out.println("length=" + msg.getLength());
        
        msg.addString((short)2, "Ph?m Công Phan xóm 8 th? nghi?p");
        System.out.println("length=" + msg.getLength());
        
        // flatting msg to byte array
        byte [] msgData = msg.flat();
        ByteBuffer inBuf = ByteBuffer.wrap(msgData);
        
        TLVMessage newMsg = new TLVMessage((short)1);
        int length = inBuf.getInt();
        byte [] msgBodyData = new byte[length];        
        inBuf.get(msgBodyData);
        
        newMsg.parse(length, msgBodyData);
        //get string field with tag == 2
        String str = newMsg.getString((short)2);
        
        System.out.println("String: " + str);
    }

    public void addLong(short attrTag, long val) {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);        
        buf.putLong(val);
        
        TLVAttribute attr = new TLVAttribute(attrTag, (short)Long.BYTES, buf.array());
        attrMap.put(attrTag, attr);
    }
}
