package com.neepu.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//mp4 转avi
public class FFMpegTest {

	private String ffmpegEXE;
	
	public FFMpegTest(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}
	
	public void convertor(String videoInputPath, String videoOutputPath) throws Exception {
//		ffmpeg -i input.mp4 -y output.avi
		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);
		
		command.add("-i");
		command.add(videoInputPath);
		command.add("-y");
		command.add(videoOutputPath);
		
		for (String c : command) {
			System.out.print(c + " ");
		}
		
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();
		
		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);
		
		String line = "";
		while ( (line = br.readLine()) != null ) {
		}
		
		if (br != null) {
			br.close();
		}
		if (inputStreamReader != null) {
			inputStreamReader.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}
		
	}

	public static void main(String[] args) {
		FFMpegTest ffmpeg = new FFMpegTest("G:\\ffmpeg.exe");
		try {
			ffmpeg.convertor("C:\\Users\\wangsq\\Desktop\\小程序实战代码文件专用分享文件夹\\lalala.mp4",
					"C:\\Users\\wangsq\\Desktop\\小程序实战代码文件专用分享文件夹\\lalala.avi");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
