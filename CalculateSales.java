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
	static String fs = System.getProperty( "file.separator" );
	static String sep = System.getProperty("line.separator");
	static HashMap<String,String> branchName = new HashMap<String,String>();
	static HashMap<String,Long> branchValue = new HashMap<String,Long>();
	static HashMap<String,String> commodityName = new HashMap<String,String>();
	static HashMap<String,Long> commodityValue = new HashMap<String,Long>();
	
	
	public static void main(String[] args){	
		BufferedReader br = null;
		boolean boo = true;
		
		//コマンド引数の確認
		if(args.length!=1){
			System.out.println("予期せぬエラーが発生しました");		//コマンド引数複数orなし
			return;
		}
			
		//支店定義ファイルの読み込み
		boo = fileRead(args[0], "branch.lst", "支店", "^[0-9]*$", 3, branchName, branchValue);
		if(boo == false){
			return;
		}

		//商品定義ファイルの読み込み
		boo = fileRead(args[0], "commodity.lst","商品", "^[0-9a-zA-Z]*$", 8, commodityName, commodityValue);
		if(boo == false){
			return;
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
		}
		
		//8桁の確認
		ArrayList<String> earningsName2  = new ArrayList<String>();
		for(int i=0; i<earningsName.length; i++){
			if(earningsName[i]!=null && earningsName[i].length()==12){
				earningsName2.add(earningsName[i]);
			}
		}
		
		//連番の確認
		int[] num = new int[earningsName2.size()];
		for(int i=0; i<num.length; i++){
			num[i] = Integer.parseInt(earningsName2.get(i).substring(0,8));
		}
		Arrays.sort(num);
		if(num.length!=0 && num[num.length-1]-num[0]+1!=num.length){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		//売り上げファイルの読み込み
		try{
			File[] file = new File[num.length];
			for(int i=0; i<num.length; i++){
				file[i] = new File(args[0]+fs+earningsName2.get(i));
				br = new BufferedReader(new FileReader(file[i]));
				String branchKey = br.readLine();
				String commodityKey = br.readLine();
				String value = br.readLine();
				String st =  br.readLine();
				
				//ファイルフォーマットの確認
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
				if(value.matches("^[0-9]*$")==false){
					System.out.println("予期せぬエラーが発生しました");		//3行目が数値以外
					return;
				}
				
				//売り上げ合計の計算
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
				if(br != null){
					br.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		//支店別集計ファイルの生成
		boo = fileWrite(args[0], "branch.out", branchName, branchValue);
		if(boo == false){
			return;
		}

		//商品別集計ファイルの生成
		boo = fileWrite(args[0], "commodity.out", commodityName, commodityValue);
		if(boo == false){
			return;
		}
	}
	
	
	//ファイル読み込みのメソッド
	public static boolean fileRead(String arg, String str, String name, String patturn, int codeLength, HashMap<String, String> nameMap, HashMap<String, Long> valueMap){
		BufferedReader br = null;
		
		try{
			File file = new File(arg+ fs + str);
			
			//ファイル存在の確認
			if(file.exists()==false){
				System.out.println(name+"定義ファイルが存在しません");		//ファイルが存在しない
				return false;
			}
			
			//ファイルロックの確認
			if(file.canRead()==false){
				System.out.println("予期せぬエラーが発生しました");		//読み込めないファイル
				return false;
			}
			
			//ファイル読み込み
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				
				//ファイルフォーマットの確認
				if(line.indexOf(",")==-1){
					System.out.println(name+"定義ファイルのフォーマットが不正です");		//,なし
					return false;
				}
				String[] strings = line.split(",",0);
				if(strings.length>2){
					System.out.println(name+"定義ファイルのフォーマットが不正です");		//,2個以上
					return false;
				}
				if(strings[0].matches(patturn)==false){
					System.out.println(name+"定義ファイルのフォーマットが不正です");		//コードに数値(とアルファベット)以外
					return false;
				}
				if(strings[0].length()!=codeLength){
					System.out.println(name+"定義ファイルのフォーマットが不正です");		//コードの桁数が違う
					return false;
				}
				
				//マップに追加
				nameMap.put(strings[0],strings[1]);
				valueMap.put(strings[0],(long)0);
				
				line = br.readLine();
			}
		}	
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		finally {
			try {
				if(br != null){
					br.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		
		return true;
	}
	
	
	//ファイル生成・書き込みのメソッド
	public static boolean fileWrite(String arg, String str, HashMap<String, String> nameMap, HashMap<String, Long> valueMap){
		FileWriter fw = null;

		try{
			//ファイルの生成
			File file = new File(arg+fs+str);
			file.createNewFile();

			//書き込み
			fw = new FileWriter(file);
			ArrayList<String> list = new ArrayList<String>();
			valueMap.entrySet().stream().sorted(java.util.Collections.reverseOrder(java.util.Map.Entry.comparingByValue())).forEach(s -> list.add(s.getKey()+","+nameMap.get(s.getKey())+","+valueMap.get(s.getKey())));
			for(int i=0; i<list.size(); i++){
				fw.write(list.get(i)+sep);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		finally {
			try {
				if(fw != null){
					fw.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		
		return true;

	}
}
