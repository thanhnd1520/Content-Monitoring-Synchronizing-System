package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import config.Configuration;
import model.TLVMessage;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

import java.nio.file.StandardWatchEventKinds.*;


import java.nio.file.WatchEvent;

public class WatchingFolder implements Runnable{
	private static final String path = Configuration.getProperties("client.folderDirectory");
	private ClientHandler consumer;
	final static Logger logger = Logger.getLogger(WatchingFolder.class);
	
	public static boolean checkUpdate; // true: chế độ bthg
											  // false: đang update lên server
	
	public WatchingFolder(ClientHandler consumer) {
		this.consumer = consumer;
		logger.info("start watching folder: " + path);
	}
	@Override
	public void run() {
		try {
			File folder = new File(path);
			if (!folder.exists()){
				folder.mkdirs();
			}
			System.out.println("start WatchingFolder");
			sample();
		} catch (JNotifyException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sample() throws JNotifyException, InterruptedException  {
        int mask =  JNotify.FILE_CREATED |
                JNotify.FILE_DELETED |
                JNotify.FILE_MODIFIED|
                JNotify.FILE_RENAMED;
        boolean watchSubtree = true;
        int watchID = JNotify.addWatch(path, mask, watchSubtree, new JNotifyListener()
        {
            public void fileRenamed(int wd, String rootPath, String oldName, String newName){
                if(checkUpdate && check(oldName) && check(newName) && check(oldName) && check(newName)) {
                	consumer.addFileRename(oldName, newName);
                	System.out.println("WatchingFolder: System rename file: " + oldName);
                }
            }
            public void fileModified(int wd, String rootPath, String name){
            	if(checkUpdate && check(name) && check(name)) {
            		System.out.println("WatchingFolder: System modifile file: " + name);
                	consumer.addFileUpdate(name);
                }
            }
            public void fileDeleted(int wd, String rootPath, String name){
            	if(checkUpdate && check(name) && check(name)) {
                	consumer.addFileDelete(name);
            		System.out.println("WatchingFolder: System delete file: " + name);
                }
            }
            public void fileCreated(int wd, String rootPath, String name){
            	if(checkUpdate && check(name) && check(name)) {
                	consumer.addFileCreate(name);
            		System.out.println("WatchingFolder: System create file: " + name);
                }
            }
        });
        while (true){
            Thread.sleep(500);
        }
    }
	private boolean check(String fileName) {
		if(fileName.contains("~") || 
			fileName.toLowerCase().contains(".tmp") ||
			!fileName.contains(".")) {
			return false;
		}
		return true;
	}
}
