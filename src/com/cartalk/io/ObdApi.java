package com.cartalk.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.util.Log;

public class ObdApi{
	private InputStream mIn = null;
	private OutputStream mOut = null;
	private boolean reset = false;
	private float speed = 0;
	private float rpm = 0;
	private float maf = 0;
	private float fuelLevel =0;
	private float equivRatio = 0;
	private float position =0;
	private float presure = 0;
	private float tempereture = 0;
	private int codeCount = 0;
	public ObdApi(InputStream in, OutputStream out){
		mIn = in;
		mOut = out;
	}
	private boolean sendCommand(String cmd){
		if(mOut==null){
			return false;
		}
		try {
			mOut.write(cmd.getBytes());
			mOut.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String readCommand(){
		if(mIn==null){
			return "";
		}
		byte b = 0;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives
        try {
			while ((char) (b = (byte) mIn.read()) != '>')
			  if ((char) b != ' ')
			    res.append((char) b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
        if(res.length()==0){
        	return "";
        }
        String rawdata=res.toString().trim();
        if(rawdata.lastIndexOf(13)<0){
        	return rawdata;
        }
        return rawdata.substring(rawdata.lastIndexOf(13)+1).trim();
	}
	
	private String readRawData(){
		if(mIn==null){
			return "";
		}
		byte b = 0;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives
        try {
			while ((char) (b = (byte) mIn.read()) != '>')
			  if ((char) b != ' ')
			    res.append((char) b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
        if(res.length()==0){
        	return "";
        }
        return res.toString();
	}
	
	private ArrayList<Integer> writeAndRead(String cmd,int wait){
		if(!sendCommand(cmd)){
			return null;
		}
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = readCommand();
		return StringToArray(result);
	}
	private ArrayList<Integer> StringToArray(String str){
		if(str=="" || str=="NODATA"){
			return null;
		}
		ArrayList<Integer> buffer = new ArrayList<Integer>();
	    int begin = 0;
	    int end = 2;
	    while (end <= str.length()) {
	        try{
	            buffer.add(Integer.decode("0x" + str.substring(begin, end)));
	        }catch(Exception e){
	        	break;
	        }
	        begin = end;
	        end += 2;
	    }
		return buffer;
	}
	private String writeAndReadString(String cmd,int wait){
		if(!sendCommand(cmd)){
			return null;
		}
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = readCommand();
		if(result=="" || result=="NODATA"){
			return null;
		}
		return result;
	}
	
	public boolean Receive(){
		String result = readCommand();
		if(result=="" || result=="NODATA"){
			return false;
		}
		return true;
	}
	private String extractResult(String rawdata){
        if(rawdata.lastIndexOf(13)<0){
        	return rawdata;
        }
        return rawdata.substring(rawdata.lastIndexOf(13)+1).trim();	
	}
	private boolean ParseResult(String rawData){
		String result = extractResult(rawData);
		if(rawData.contains("ATZ")){
			if(result.contains("ELM")){
				reset = true;
			}
		}
		else if(rawData.contains("010D")){
			ArrayList<Integer> buffer = StringToArray(result);
			if(buffer==null || buffer.size()<3){
				speed = 0;
				return false;
			}
			speed = buffer.get(2);
		}
		else if(rawData.contains("010C")){
			ArrayList<Integer> buffer = StringToArray(result);
			if(buffer==null || buffer.size()<4){
				rpm = 0;
				return false;
			}
			rpm = (buffer.get(2) * 256 + buffer.get(3)) / 4;
		}
		else if(rawData.contains("0110")){
			ArrayList<Integer> buffer = StringToArray(result);
			if(buffer==null || buffer.size()<0){
				maf = 0;
				return false;
			}
			maf = (buffer.get(2) * 256 + buffer.get(3)) / 100.0f;
		}
		else if(rawData.contains("012F")){
			ArrayList<Integer> buffer = StringToArray(result);
			if(buffer==null || buffer.size()<3){
				fuelLevel = 0;
				return false;
			}
			fuelLevel = 100.0f * buffer.get(2) / 255.0f;
		}
		else if(rawData.contains("0144")){
			ArrayList<Integer> buffer = StringToArray(result);
			if(buffer==null || buffer.size()<4){
				fuelLevel = 0;
				return false;
			}
			int a = buffer.get(2);
		    int b = buffer.get(3);
		    equivRatio = (a * 256 + b) / 32768;
		}
        else if(rawData.contains("0111")){
		    ArrayList<Integer> buffer = StringToArray(result);
		    if(buffer==null || buffer.size()<3){
			    position = 0;
			    return false;
		    }
		    position = (buffer.get(2) * 100.0f) / 255.0f;
        }
        else if(rawData.contains("010b")){	
		    ArrayList<Integer> buffer = StringToArray(result);
		    if(buffer==null || buffer.size()<3){
			    presure = 0;
			    return false;
		    }
		    presure = buffer.get(2);
        }
        else if(rawData.contains("0105")){
		    ArrayList<Integer> buffer = StringToArray(result);
		    if(buffer==null || buffer.size()<3 ){
			    tempereture=0;
			    return false;
		    }
		    tempereture = buffer.get(2) - 40;
        }
        else if(rawData.contains("0101")){	
		    ArrayList<Integer> buffer = StringToArray(result);
		    if(buffer==null || buffer.size()<3){
			    codeCount = 0;
			    return false;
		    }
		    final int mil = buffer.get(2);
	        boolean milOn = (mil & 0x80) == 128;
	        codeCount = mil & 0x7F;
        }
		return true;	
	}

	public boolean ResetObd(){
		String cmd = "ATZ\r\n";
		String buffer = writeAndReadString(cmd,2000);
/*		while(!buffer.contains("CONNECTING")){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = readCommand();
		}
		buffer = readCommand();

		buffer = writeAndReadString(cmd,2000);
		if(buffer==null || buffer.contains("?")){
			return false;
		}*/
		while(!buffer.contains("ELM")){
			Log.e("Reset OBD",buffer);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = readCommand();
		}
		return true;
	}
	public boolean EchoOff(){
		String cmd = "ATE0\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	public boolean EchoOn(){
		String cmd = "ATE1\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	public boolean LineFeedOff(){
		String cmd = "ATL0\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	public boolean SpaceOff(){
		String cmd = "ATS0\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	public boolean AdaptiveTiming(int mode){
		String cmd = "ATAT"+mode+"\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	public boolean SelectProtocol(int protocol){
		String cmd = "ATSP"+protocol+"\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	
	public boolean GetProtocol(){
		String cmd = "ATDPN\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	public boolean Headers(int onoff){
		String cmd = "ATH"+onoff+"\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("OK")){
			return false;
		}
		return true;
	}
	
	public boolean Supported(){
		String cmd = "0100\r";
		String buffer = writeAndReadString(cmd,100);
		if(buffer==null || !buffer.contains("41")){
			return false;
		}
		return true;
	}
	public float AmbientAirTemperature(){
		String cmd = "01461\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float temperature = buffer.get(2) - 40;
		return temperature;
	}
	
	public float Speed(){
		String cmd = "010D1\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float speed = buffer.get(2);
		return speed;
	}
	
	public float RPM(){
		String cmd = "010C1\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float rpm = (buffer.get(2) * 256 + buffer.get(3)) / 4;
		return rpm;	
	}
	
	public float MassAirFlow(){
		String cmd = "01101\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float maf = (buffer.get(2) * 256 + buffer.get(3)) / 100.0f;
		return maf;
	}
	
	public float FuelLevel(){
		String cmd = "012F1\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float fuelLevel = 100.0f * buffer.get(2) / 255.0f;
		return fuelLevel;
	}
	
	public float EquivRatio(){
		String cmd = "01441\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		int a = buffer.get(2);
	    int b = buffer.get(3);
	    float ratio = (a * 256 + b) / 32768;
		return ratio;
	}
	
	public float ThrottlePosition(){
		String cmd = "01111\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float position = (buffer.get(2) * 100.0f) / 255.0f;
		return position;
	}
	
	public float IntakeManifoldPressure(){
		String cmd = "010b1\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		float presure = buffer.get(2);
		return presure;
	}
	
	public float EngineCoolantTemperature(){
		String cmd = "01051\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0 ){
			return 0;
		}
		float tempereture = buffer.get(2) - 40;
		return tempereture;
	}
	
	public int DtcNumber(){
		String cmd = "01011\r";
		ArrayList<Integer> buffer = writeAndRead(cmd,100);
		if(buffer==null || buffer.size()==0){
			return 0;
		}
		final int mil = buffer.get(2);
	    boolean milOn = (mil & 0x80) == 128;
	    int codeCount = mil & 0x7F;
		return codeCount;
	}
}