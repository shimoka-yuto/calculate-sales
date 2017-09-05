package jp.alhinc.shimoka_yuto.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CalculateSales {
	public static void main(String[] args){
		String line = null;
		HashMap<String,String> branchName = new HashMap<String,String>();
		HashMap<String,Integer> branchValue = new HashMap<String,Integer>();
		HashMap<String,String> commodityName = new HashMap<String,String>();
		HashMap<String,Integer> commodityValue = new HashMap<String,Integer>();
		String sep = System.getProperty("line.separator");
		String fs = System.getProperty( "file.separator" );
		BufferedReader br_b = null;
		BufferedReader br_c = null;
		BufferedReader br_e = null;

		//支店定義ファイルの読み込み
		try{
			File file = new File(args[0]+ fs+"branch.lst");
			if(file.exists()==false){
				System.out.println("支店定義ファイルが存在しません。");
				return;
			}
			br_b = new BufferedReader(new FileReader(file));
			line = br_b.readLine();
			while (line != null) {
				if(line.indexOf(",")==-1){
					System.out.println("支店定義ファイルのフォーマットが不正です。");
					return;
				}
				else{
					branchName.put(line.substring(0,line.indexOf(",")),line.substring(line.indexOf(",")+1));
					branchValue.put(line.substring(0,line.indexOf(",")),0);
					line = br_b.readLine();
				}
			}
		}
		
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
			return;
		}
		finally {
			try {
				br_b.close();
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました。");
				return;
			}
		}

		//商品定義ファイルの読み込み
		try{
			File file = new File(args[0]+fs+"commodity.lst");
			if(file.exists()==false){
				System.out.println("商品定義ファイルが存在しません。");
				return;
			}
			br_c = new BufferedReader(new FileReader(file));
			line = br_c.readLine();
			while (line != null) {
				if(line.indexOf(",")==-1){
					System.out.println("商品定義ファイルのフォーマットが不正です。");
					return;
				}
				else{
					commodityName.put(line.substring(0,line.indexOf(",")),line.substring(line.indexOf(",")+1));
					commodityValue.put(line.substring(0,line.indexOf(",")),0);
					line = br_c.readLine();
				}
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
			return;
		}
		finally {
			try {
				br_c.close();
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました。");
				return;
			}
		}

		//売り上げファイルの抽出
		FilenameFilter filter = new FilenameFilter() {		//ファイルネームフィルター
			public boolean accept(File file, String str){
				if (str.endsWith("rcd")){
					return true;
				}
				else{
					return false;
				}
			}
		};
		File[] files = new File(args[0]).listFiles(filter);
		
		//売り上げファイルの名前の取得
		String[] earningsName = new String[files.length];
		for(int i=0; i<files.length; i++){
			earningsName[i] = files[i].getName();
		}
		
		//8桁の確認
		ArrayList<String> earningsName2  = new ArrayList<String>();
		for(int i=0; i<earningsName.length; i++){
			if(earningsName[i].length()==12){
				earningsName2.add(earningsName[i]);
			}
		}
		
		//連番の確認
		int[] num = new int[earningsName2.size()];
		for(int i=0; i<num.length; i++){
			num[i] = Integer.parseInt(earningsName2.get(i).substring(0,8));
		}
		Arrays.sort(num);
		if(num[num.length-1]-num[0]+1!=num.length){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		//売り上げマップ
		try{
			File[] file = new File[num.length];
			for(int i=0; i<num.length; i++){
				file[i] = new File(args[0]+fs+earningsName2.get(i));
				br_e = new BufferedReader(new FileReader(file[i]));
				String branchKey = br_e.readLine();
				if(branchName.get(branchKey)==null){
					System.out.println("<"+branchKey+">の支店コードが不正です。");
					return;
				}
				String commodityKey = br_e.readLine();
				if(commodityName.get(commodityKey)==null){
					System.out.println("<"+commodityKey+">の商品コードが不正です。");
					return;
				}
				
				//売り上げ合計の計算
				String value = br_e.readLine();
				int bValue = branchValue.get(branchKey)+Integer.parseInt(value);
				int cValue =commodityValue.get(commodityKey)+Integer.parseInt(value);
				branchValue.replace(branchKey,bValue);
				commodityValue.replace(commodityKey,cValue);
				
				//10桁の確認
				if(String.valueOf(bValue).length()>10 || String.valueOf(cValue).length()>10){
					System.out.println("合計金額が１０桁を超えました。");
					return;
				}
				
				//４行目の確認
				String st =  br_e.readLine();
				if(st != null){
					System.out.println("<"+earningsName2.get(i)+">のフォーマットが不正です。");
				}
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
			return;
		}
		finally {
			try {
				br_e.close();
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました。");
				return;
			}
		}

		//支店別集計ファイルの生成
		try{
			File branchOut = new File(args[0]+fs+"branch.out");
			branchOut.createNewFile();

			//書き込み
			FileWriter filewriter = new FileWriter(branchOut);
			ArrayList<String> list = new ArrayList<String>();
			branchValue.entrySet().stream().sorted(java.util.Collections.reverseOrder(java.util.Map.Entry.comparingByValue())).forEach(s -> list.add(s.getKey()+","+branchName.get(s.getKey())+","+branchValue.get(s.getKey())));
			for(int i=0; i<list.size(); i++){
				filewriter.write(list.get(i)+sep);
			}
			filewriter.close();
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
			return;
		}


		//商品別集計ファイルの生成
		try{
			File commodityOut = new File(args[0]+fs+"commodity.out");
			commodityOut.createNewFile();

			//書き込み
			FileWriter filewriter = new FileWriter(commodityOut);
			ArrayList<String> list = new ArrayList<String>();
			commodityValue.entrySet().stream().sorted(java.util.Collections.reverseOrder(java.util.Map.Entry.comparingByValue())).forEach(s -> list.add(s.getKey()+","+commodityName.get(s.getKey())+","+commodityValue.get(s.getKey())));
			for(int i=0; i<list.size(); i++){
				filewriter.write(list.get(i)+sep);
			}
			filewriter.close();
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
			return;
		}


	}
}
