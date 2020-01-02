package com.neepu.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MergeVideoMp3 {

	private String ffmpegEXE;
	
	public MergeVideoMp3(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}

	//ffmpeg -i bgm.mp3 -i input.mp4 -t 6 -filter_complex amix=inputs=2 output.mp4

	/**
	 * 注意:inputs=输入流数量, duration=决定流的结束,
	 * dropout_transition= 输入流结束时,容量重整时间,
	 * (longest最长输入时间,shortest最短,first第一个输入持续的时间))
	 *
	 * ffmpeg -i 1.mp3 -i lalala.mp4 -t 15 -filter_complex amix=inputs=2:duration=first:dropout_transition=2 output.mp4
	 *
	 * @throws Exception
	 */
	public void convertor(String videoInputPath, String mp3InputPath,
			double seconds, String videoOutputPath) throws Exception {
//		ffmpeg.exe -i 1.mp4 -i bgm.mp3 -t 7 -y 2.mp4
		System.out.print(mp3InputPath);
		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);

		command.add("-i");
		command.add(mp3InputPath);
		
		command.add("-i");
		command.add(videoInputPath);
		

		
		command.add("-t");
		command.add(String.valueOf(seconds));
		
		//command.add("-y");
		command.add("-filter_complex");
		command.add("amix=inputs=2:duration=first:dropout_transition=2");

		command.add(videoOutputPath);

		
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();
		
		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);

		
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
		MergeVideoMp3 ffmpeg = new MergeVideoMp3("G:\\ffmpeg.exe");
		try {
			ffmpeg.convertor("C:\\Users\\wangsq\\Desktop\\小程序实战代码文件专用分享文件夹\\lalala.avi",
					"C:\\Users\\wangsq\\Desktop\\小程序实战代码文件专用分享文件夹\\我知道.mp3", 15,
					"C:\\Users\\wangsq\\Desktop\\小程序实战代码文件专用分享文件夹\\这是通过java生产的视频1.mp4");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
