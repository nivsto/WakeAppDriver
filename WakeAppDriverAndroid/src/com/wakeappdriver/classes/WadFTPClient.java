package com.wakeappdriver.classes;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.content.Context;
import android.util.Log;

public class WadFTPClient {
	private static final String TAG = "WAD";
	private FTPClient mFTPClient;
	
	private String host = "wakeappdriver.bugs3.com";
	private String username = "u659681709";
	private String password = "ilwadfa14";
	private int port = 21;
	
	public WadFTPClient(){
		this.mFTPClient = new FTPClient();
	}
	//Method to connect to FTP server:
		public boolean ftpConnect()
		{
		    try {
		        // connecting to the host
		        mFTPClient.connect(host, port);

		        // now check the reply code, if positive mean connection success
		        if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
		            // login using username & password
		            boolean status = mFTPClient.login(username, password);

		            /* Set File Transfer Mode
		             *
		             * To avoid corruption issue you must specified a correct
		             * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
		             * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE
		             * for transferring text, image, and compressed files.
		             */
		            mFTPClient.setFileType(FTP.ASCII_FILE_TYPE);
		            mFTPClient.enterLocalPassiveMode();
		            if (status == true){
		            	Log.d(TAG, "Connection to ftp server Succeeded");
		            }
		            return status;
		        }
		    } catch(Exception e) {
		        Log.d(TAG, "Error: could not connect to host " + host );
		    }

		    return false;
		}
		
		public boolean storeFile(String remote, InputStream local){
			boolean ans = false;
			
			try {
				ans =  mFTPClient.storeFile(remote, local);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "Error: could not store file at " + remote );
			}
			
			return ans;
		}
		
		
		//Method to disconnect from FTP server:

		public boolean ftpDisconnect()
		{
		    try {
		        mFTPClient.logout();
		        mFTPClient.disconnect();
		        return true;
		    } catch (Exception e) {
		        Log.d(TAG, "Error occurred while disconnecting from ftp server.");
		    }

		    return false;
		}
		
		//Method to get current working directory:

		public String ftpGetCurrentWorkingDirectory()
		{
		    try {
		        String workingDir = mFTPClient.printWorkingDirectory();
		        return workingDir;
		    } catch(Exception e) {
		        Log.d(TAG, "Error: could not get current working directory.");
		    }

		    return null;
		} 

		//Method to change working directory:

		public boolean ftpChangeDirectory(String directory_path)
		{
		    try {
		        mFTPClient.changeWorkingDirectory(directory_path);
		    } catch(Exception e) {
		        Log.d(TAG, "Error: could not change directory to " + directory_path);
		    }

		    return false;
		} 

		//Method to list all files in a directory:

		public void ftpPrintFilesList(String dir_path)
		{
		    try {
		        FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
		        int length = ftpFiles.length;

		        for (int i = 0; i < length; i++) {
		            String name = ftpFiles[i].getName();
		            boolean isFile = ftpFiles[i].isFile();

		            if (isFile) {
		                Log.i(TAG, "File : " + name);
		            }
		            else {
		                Log.i(TAG, "Directory : " + name);
		            }
		        }
		    } catch(Exception e) {
		        e.printStackTrace();
		    }
		} 

		//Method to create new directory:

		public boolean ftpMakeDirectory(String new_dir_path)
		{
		    try {
		        boolean status = mFTPClient.makeDirectory(new_dir_path);
		        return status;
		    } catch(Exception e) {
		        Log.d(TAG, "Error: could not create new directory named " + new_dir_path);
		    }

		 return false;
		} 

		//Method to delete/remove a directory:

		public boolean ftpRemoveDirectory(String dir_path)
		{
		    try {
		        boolean status = mFTPClient.removeDirectory(dir_path);
		        return status;
		    } catch(Exception e) {
		        Log.d(TAG, "Error: could not remove directory named " + dir_path);
		    }

		    return false;
		} 

		//Method to delete a file:

		public boolean ftpRemoveFile(String filePath)
		{
		    try {
		        boolean status = mFTPClient.deleteFile(filePath);
		        return status;
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    return false;
		} 

		//Method to rename a file:

		public boolean ftpRenameFile(String from, String to)
		{
		    try {
		        boolean status = mFTPClient.rename(from, to);
		        return status;
		    } catch (Exception e) {
		        Log.d(TAG, "Could not rename file: " + from + " to: " + to);
		    }

		    return false;
		} 

		//Method to download a file from FTP server:

		/**
		 * mFTPClient: FTP client connection object (see FTP connection example)
		 * srcFilePath: path to the source file in FTP server
		 * desFilePath: path to the destination file to be saved in sdcard
		 */
		public boolean ftpDownload(String srcFilePath, String desFilePath)
		{
		    boolean status = false;
		    try {
		        FileOutputStream desFileStream = new FileOutputStream(desFilePath);;
		        status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
		        desFileStream.close();

		        return status;
		    } catch (Exception e) {
		        Log.d(TAG, "download failed");
		    }

		    return status;
		} 

		//Method to upload a file to FTP server:

		/**
		 * mFTPClient: FTP client connection object (see FTP connection example)
		 * srcFilePath: source file path in sdcard
		 * desFileName: file name to be stored in FTP server
		 * desDirectory: directory path where the file should be upload to
		 */
		public boolean ftpUpload(String srcFilePath, String desFileName,
		                         String desDirectory, Context context)
		{
		    boolean status = false;
		    try {
		       // FileInputStream srcFileStream = new FileInputStream(srcFilePath);
		        
		        FileInputStream srcFileStream = context.openFileInput(srcFilePath);

		        // change working directory to the destination directory
		        //if (ftpChangeDirectory(desDirectory)) {
		            status = mFTPClient.storeFile(desFileName, srcFileStream);
		        //}

		        srcFileStream.close();
		        return status;
		    } 
		    catch (Exception e) {
		        Log.d(TAG, "upload failed: " + e);
		    }

		    return status;
		}
}
