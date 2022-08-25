package model;

public class MessageDefs {
	final static public class MessageTypes {
        final static public short MT_LOGIN_REQ = 1;
        final static public short MT_LOGIN_RES = 2;
        
        final static public short MT_SEND_TEXT_REQ = 3; // co result code
        final static public short MT_SEND_TEXT_RES = 4;
        
        final static public short MT_BEGIN_FILE_TRANSFER_REQ = 5; // co result code
        final static public short MT_BEGIN_FILE_TRANSFER_RES = 6;
        
        final static public short MT_END_FILE_TRANSFER_REQ = 7; // co result code
        final static public short MT_END_FILE_TRANSFER_RES = 8;
        
        final static public short MT_RESUME_FILE_TRANSFER_REQ = 9; // co result code
        final static public short MT_RESUME_FILE_TRANSFER_RES = 10;
        
        final static public short MT_FILE_FRAGMENT_REQ = 11; 
        final static public short MT_FILE_FRAGMENT_RES = 12;
        
        final static public short MT_FILE_DELETE_REQ = 13;
        final static public short MT_FILE_DELETE_RES = 14;
        
        final static public short MT_HEAD_BEAT = 15;
        
        final static public short MT_SYNCHRONIZE_REQ = 16;
        final static public short MT_SYNCHRONIZE_RES = 17;
        
        final static public short MT_LIST_CLIENT_FILE = 17;
        // ...
    }
    
    final static public class FieldTypes {
        final static public short FT_RESULT_CODE = 1;
        
        final static public short FT_USERID = 2;
        final static public short FT_USERPASS = 3;
        final static public short FT_BODY_TEXT = 4;
        
        final static public short FT_FILE_NAME = 5;
        final static public short FT_FILE_CHECKSUM = 6;
                
        final static public short FT_FILE_FRAGMENT = 7;        
        final static public short FT_FILE_CURRENT_POS = 8;
        final static public short FT_TRANSID = 9;
        // ...
    }
    
    final static public class CodeType {
        final static public short LOGIN_FAIL = 1;
        final static public short LOGIN_SUCCESS = 2;
        
        final static public short CLIENT_UPDATE = 3;
        final static public short SERVER_INIT = 4;
        
        final static public short FILE_EXIST = 5;
        final static public short FILE_NOT_EXIST = 6;
    }
}
