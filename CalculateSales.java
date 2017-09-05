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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalculateSales {
	public static void main(String[] args){
		String line = null;
		HashMap<String,String> branchName = new HashMap<String,String>();
		HashMap<String,Long> branchValue = new HashMap<String,Long>();
		HashMap<String,String> commodityName = new HashMap<String,String>();
		HashMap<String,Long> commodityValue = new HashMap<String,Long>();
		String sep = System.getProperty("line.separator");
		String fs = System.getProperty( "file.separator" );
		BufferedReader br_b = null;
		BufferedReader br_c = null;
		BufferedReader br_e = null;
		FileWriter fw_b = null;
		FileWriter fw_c = null;
		Pattern p = Pattern.compile("^[0-9]*$");
		Pattern p2 = Pattern.compile("^[0-9a-zA-Z]*$");

		//支店定義ファイルの読み込み
		try{
			if(args.length!=1){
				System.out.println("予期せぬエラーが発生しました");		//コマンド引数複数orなし
				return;
			}
			File file = new File(args[0]+ fs+"branch.lst");
			if(file.exists()==false){
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			if(file.canRead()==false){
				System.out.println("予期せぬエラーが発生しました");		//読み込めないファイル
				return;
			}
			br_b = new BufferedReader(new FileReader(file));
			line = br_b.readLine();
			while (line != null) {
				if(line.indexOf(",")==-1){
					System.out.println("支店定義ファイルのフォーマットが不正です");		//,なし
					return;
				}
				String[] strings = line.split(",",0);
				if(strings.length>2){
					System.out.println("支店定義ファイルのフォーマットが不正です");		//,２個以上
					return;
				}
				branchName.put(line.substring(0,line.indexOf(",")),line.substring(line.indexOf(",")+1));
				Matcher m = p.matcher(line.substring(0,line.indexOf(",")));
				if(m.find()==false){
					System.out.println("支店定義ファイルのフォーマットが不正です");		//支店コードに数値以外
					return;
				}
				if(line.substring(0,line.indexOf(",")).length()!=3){
					System.out.println("支店定義ファイルのフォーマットが不正です");		//支店コードが３桁以外
					return;
				}
				branchValue.put(line.substring(0,line.indexOf(",")),(long)0);
				line = br_b.readLine();
			}
		}
		
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		finally {
			try {
				if(br_b != null){
					br_b.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		//商品定義ファイルの読み込み
		try{
			File file = new File(args[0]+fs+"commodity.lst");
			if(file.exists()==false){
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			if(file.canRead()==false){
				System.out.println("予期せぬエラーが発生しました");		//読み込めないファイル
				return;
			}
			br_c = new BufferedReader(new FileReader(file));
			line = br_c.readLine();
			while (line != null) {
				if(line.indexOf(",")==-1){
					System.out.println("商品定義ファイルのフォーマットが不正です");		//,なし
					return;
				}
				String[] strings = line.split(",",0);
				if(strings.length>2){
					System.out.println("商品定義ファイルのフォーマットが不正です");		//,２個以上
					return;
				}
				commodityName.put(line.substring(0,line.indexOf(",")),line.substring(line.indexOf(",")+1));
				Matcher m = p2.matcher(line.substring(0,line.indexOf(",")));
				if(m.find()==false){
					System.out.println("商品定義ファイルのフォーマットが不正です");		//商品コードに数値と英以外
					return;
				}
				if(line.substring(0,line.indexOf(",")).length()!=8){
					System.out.println("商品定義ファイルのフォーマットが不正です");		//商品コードが８桁以外
					return;
				}
				commodityValue.put(line.substring(0,line.indexOf(",")),(long)0);
				line = br_c.readLine();

			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		finally {
			try {
				if(br_c != null){
					br_c.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
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
			if(files[i].isDirectory()==false){
				earningsName[i] = files[i].getName();
			}
			else{
				earningsName[i] = "111";
			}
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
				String commodityKey = br_e.readLine();
				String value = br_e.readLine();
				String st =  br_e.readLine();
				if(branchKey == null){
					System.out.println(earningsName2.get(i)+"のフォーマットが不正です");		//1行目なし
					return;
				}
				if(commodityKey == null){
					System.out.println(earningsName2.get(i)+"のフォーマットが不正です");		//2行目なし
					return;
				}
				if(value == null){
					System.out.println(earningsName2.get(i)+"のフォーマットが不正です");		//3行目なし
					return;
				}
				if(st != null){
					System.out.println(earningsName2.get(i)+"のフォーマットが不正です");		//4行目あり
				}
				if(branchName.get(branchKey)==null){
					System.out.println(earningsName2.get(i)+"の支店コードが不正です");		//対応する支店なし
					return;
				}
				if(commodityName.get(commodityKey)==null){
					System.out.println(earningsName2.get(i)+"の商品コードが不正です");		//対応する商品なし
					return;
				}
				
				//売り上げ合計の計算
				Matcher m = p.matcher(value);
				if(m.find()==false){
					System.out.println("予期せぬエラーが発生しました");		//3行目が数値以外
					return;
				}
				long bValue = branchValue.get(branchKey)+Long.parseLong(value);
				long cValue =commodityValue.get(commodityKey)+Long.parseLong(value);
				branchValue.replace(branchKey,bValue);
				commodityValue.replace(commodityKey,cValue);
				
				//10桁の確認
				if(String.valueOf(bValue).length()>10 || String.valueOf(cValue).length()>10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				
				
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		finally {
			try {
				if(br_e != null){
					br_e.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		//支店別集計ファイルの生成
		try{
			File branchOut = new File(args[0]+fs+"branch.out");
			branchOut.createNewFile();

			//書き込み
			fw_b = new FileWriter(branchOut);
			ArrayList<String> list = new ArrayList<String>();
			branchValue.entrySet().stream().sorted(java.util.Collections.reverseOrder(java.util.Map.Entry.comparingByValue())).forEach(s -> list.add(s.getKey()+","+branchName.get(s.getKey())+","+branchValue.get(s.getKey())));
			for(int i=0; i<list.size(); i++){
				fw_b.write(list.get(i)+sep);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		finally {
			try {
				if(fw_b != null){
					fw_b.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}


		//商品別集計ファイルの生成
		try{
			File commodityOut = new File(args[0]+fs+"commodity.out");
			commodityOut.createNewFile();

			//書き込み
			fw_c = new FileWriter(commodityOut);
			ArrayList<String> list = new ArrayList<String>();
			commodityValue.entrySet().stream().sorted(java.util.Collections.reverseOrder(java.util.Map.Entry.comparingByValue())).forEach(s -> list.add(s.getKey()+","+commodityName.get(s.getKey())+","+commodityValue.get(s.getKey())));
			for(int i=0; i<list.size(); i++){
				fw_c.write(list.get(i)+sep);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		finally {
			try {
				if(fw_c != null){
					fw_c.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}


	}
}
