package crawling;

import java.lang.Math;
import java.util.random.*;
import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VentureList {
	
	public static void main(String[] args) throws IOException, JSONException, KeyManagementException,
			NoSuchAlgorithmException, ParseException, InterruptedException {
		
		// 코드 실행 시간을 기준으로 시간을 받음
		LocalDateTime present = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
		String folder_time = present.format(formatter);
		
		// 분별을 위한 폴더 생성
		makeFolder("./result");
		makeFolder("./result/" + folder_time.substring(4,13));
		
		String all_or_select = args[0];
		
		String list_file;
		
		if(all_or_select.contains("all")) {
			scrapAll(1, folder_time.substring(4,13));
		}
		else if(all_or_select.contains("select")) {
			list_file = args[1];
			scrapSelect(0, list_file, folder_time.substring(4,13));
		}
		
	}
	
	public static void scrapSelect(int start, String list_file, String folder_time) throws IOException, JSONException, KeyManagementException,
	NoSuchAlgorithmException, ParseException, InterruptedException{
		
		//변수 지정
		String result; 
		ArrayList res_list = new ArrayList(); 
		StringBuilder data = new StringBuilder();
		int count = 0;
		String json_text = ""; 
		JSONObject json; 
		JSONArray total_list;
		JSONObject data_list = new JSONObject();
		String base_url = "https://www.smes.go.kr/venturein/pbntc/searchVntrCmpAction";
		String jsonInputString;
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());
	
		//사업자 등록 번호 받아오기
		List<String> busi_nums = readNum(list_file);
		
		//상대경로에 스크래핑한 값을 저장하도록 지정
		BufferedWriter bw = new BufferedWriter(new FileWriter("./result/" + folder_time +"/" +"venture_list.csv", true));
		if (base_url.indexOf("https://") >= 0) { // SSL 인증서 우회
			setSSL();
		}
		
		URL url = new URL(base_url);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
		for (int i = start ; i < busi_nums.size(); i++) {
			
			count += 1;
			if (count >= 100) {
				Thread.sleep(random.nextInt(1500) + 5500); // 항목 150개마다 (5.5 + 0 ~ 1.5)초 sleep
				count = 0;
			}
			
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			jsonInputString = "{\"pg\":\"" + "1" + "\"" + ",\"bizRNo\":\"" + busi_nums.get(i) +"\"}";
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				
				System.out.print(String.valueOf(i+1) + "번 : " + busi_nums.get(i) + " 처리중 .... ");
				
				json_text = readAll(br);
				json = new JSONObject(json_text);
				total_list = json.getJSONArray("DATA_LIST");//해당 list의 데이터 정보를 저장
				
				if(total_list.length() < 1) {
					System.out.print(" 해당 기업에 관한 정보가 없습니다.");
					continue;
				}
				
				data_list = total_list.getJSONObject(0);
				
				//CSV파일로 저장하기 위해 StringBuilder에 관련된 정보를 저장
				data.append(busi_nums.size() - i); data.append(","); //No
				if (data_list.get("cmp_nm").toString().contains(",")) // 기업명
					data.append("\"" + data_list.get("cmp_nm").toString() + "\"");
				else
					data.append(data_list.get("cmp_nm").toString());
				data.append(",");
				if (data_list.get("rprsv_nm").toString().contains(",")) // 대표자
					data.append("\"" + data_list.get("rprsv_nm").toString() + "\"");
				else
					data.append(data_list.get("rprsv_nm").toString());
				data.append(",");
				data.append(data_list.get("bizrno")); data.append(","); // 사업자번호
				data.append(data_list.get("hdofc_addr")); data.append(","); // 본사주소
				if (data_list.get("indsty_nm").toString().contains(",")) // 업종
					data.append("\"" + data_list.get("indsty_nm").toString() + "\"");
				else
					data.append(data_list.get("indsty_nm").toString());
				
				//데이터 입력
				bw.write(data.toString());
				bw.newLine();
				data.setLength(0);
			
				//venture_info의 정보를 저장하기 위해 해당 클래스의 메소드 이용
				NVentureInfo.infoScraping(String.valueOf(data_list.get("vnia_sn")), folder_time); //수정된 형식의 Nventure_info 작성
				//result = VentureInfo.infoScraping(String.valueOf(data_list.get("vnia_sn"))); //기존 형식의 venture_info 작성
					
			} catch (Exception e) { // 사이트 차단 발생시 잠시 멈춤
				System.out.println("\nERROR : " + i + "번째 사업자 번호 처리중 오류");
				Thread.sleep(15000);
				scrapSelect(i, list_file, folder_time);
				e.printStackTrace();
			} finally {
				try {
					if (bw != null) {
						bw.flush();
					}
					System.out.println("완료!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.print("스크래핑 종료");
		
	}
	
	public static void scrapAll(int start, String folder_time) throws IOException, JSONException, KeyManagementException,
	NoSuchAlgorithmException, ParseException, InterruptedException {
		//변수 지정
		String result;
		int list_number; 
		int count = 0; 
		ArrayList res_list = new ArrayList(); 
		StringBuilder data = new StringBuilder(); 
		String json_text = ""; 
		JSONObject json; 
		JSONArray total_list;
		JSONObject data_list = new JSONObject();
		String base_url = "https://www.smes.go.kr/venturein/pbntc/searchVntrCmpAction";
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());
		
		//상대경로에 스크래핑한 값을 저장하도록 지정
		BufferedWriter bw = new BufferedWriter(new FileWriter("./result/" + folder_time +"/"+"venture_list.csv", true));
		if (base_url.indexOf("https://") >= 0) { // SSL 인증서 우회
			setSSL();
		}

		//사이트와 연결을 설정하여 json형태로 초기값인 {"pg":""}을 보내어 스크래핑에 필요한 값들을 받아옴
		URL url = new URL(base_url);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		String jsonInputString = "{\"pg\":\"\"}";
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		}
		
		//받아온 값을 읽은 후에 json형태로 변환시키고 그중에 TOTAL_COUNT의 value를 받아와 기업 리스트 수를 구함
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			String json_text_temp = readAll(br);
			JSONObject json_temp = new JSONObject(json_text_temp);
			list_number = (int) (Math.ceil(Double.valueOf(json_temp.getString("TOTAL_COUNT")) / 15.0)); // 항목수 계산
			System.out.println("총 기업 수 : " + json_temp.getString("TOTAL_COUNT"));
		}

		for (int i = start; i <= list_number; i++) {// (int i = 1; i <= list_number ; i++) = 사이트에 존재하는 리스트 수만큼
			System.out.print(i + "번째 리스트 처리중 : ");
			
			//빈번한 접속으로 사이트 차단을 막기 위해 간헐적으로 스크래핑 멈춤
			count += 1;
			if (count >= 10) {
				Thread.sleep(random.nextInt(1500) + 5500); // 항목 150개마다 (5.5 + 0 ~ 1.5)초 sleep
				count = 0;
			}
			
			//리스트마다 json형태로 {"pg":"페이지수"}를 요청하여 해당하는 리스트의 정보를 받아옴
			//cmpNm 회사이름
			//rprsvNm 대표자명
			//bizRNo 사업자번호
			//pg 페이지번호
			//areaCd 지역번호 
			//indstyCd 업종
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			jsonInputString = "{\"pg\":\"" + i + "\"}";
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length); //i번째 리스트 json 데이터 요청
			}
			
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				json_text = readAll(br);
				json = new JSONObject(json_text);
				total_list = json.getJSONArray("DATA_LIST");//해당 list의 데이터 정보를 저장

				for (int j = 0; j < total_list.length(); j++) {//(int j = 0; j < total_list.length() ; j++) = 리스트에 존재하는 항목 수만큼
					data_list = total_list.getJSONObject(j);
					try {
						System.out.print((j + 1) + ", ");//현재 스크래핑중인 항목의 순번 출력
						
						//CSV파일로 저장하기 위해 StringBuilder에 관련된 정보를 저장
						data.append(data_list.get("seqnum")); data.append(","); //No
						if (data_list.get("cmp_nm").toString().contains(",")) // 기업명
							data.append("\"" + data_list.get("cmp_nm").toString() + "\"");
						else
							data.append(data_list.get("cmp_nm").toString());
						data.append(",");
						if (data_list.get("rprsv_nm").toString().contains(",")) // 대표자
							data.append("\"" + data_list.get("rprsv_nm").toString() + "\"");
						else
							data.append(data_list.get("rprsv_nm").toString());
						data.append(",");
						data.append(data_list.get("bizrno")); data.append(","); // 사업자번호
						data.append(data_list.get("hdofc_addr")); data.append(","); // 본사주소
						if (data_list.get("indsty_nm").toString().contains(",")) // 업종
							data.append("\"" + data_list.get("indsty_nm").toString() + "\"");
						else
							data.append(data_list.get("indsty_nm").toString());
						
						//데이터 입력
						bw.write(data.toString());
						bw.newLine();
						data.setLength(0);
					
						//venture_info의 정보를 저장하기 위해 해당 클래스의 메소드 이용
						NVentureInfo.infoScraping(String.valueOf(data_list.get("vnia_sn")), folder_time); //수정된 형식의 Nventure_info 작성
						//result = VentureInfo.infoScraping(String.valueOf(data_list.get("vnia_sn"))); //기존 형식의 venture_info 작성
						
					} catch (Exception e) { // 사이트 차단 발생시 잠시 멈춤
						System.out.print("\nERROR : " + i + "번째 처리중 오류");
						Thread.sleep(20000);
						NVentureInfo.infoScraping(String.valueOf(data_list.get("vnia_sn")), folder_time);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				scrapAll(i, folder_time);
				return;
			} finally {
				try {
					if (bw != null) {
						bw.flush();
					}
					System.out.println("");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		
		try {
			if(bw !=null) {
				bw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("스크래핑 종료");

	}

	public static List readNum(String list_file) {
		List<String> csvList = new ArrayList<String>();
        File csv = new File(list_file);
        BufferedReader br = null;
        String line = "";
        String lineArr;
        try {
            br = new BufferedReader(new FileReader(csv));
            while ((line = br.readLine()) != null) { // readLine()은 파일에서 개행된 한 줄의 데이터를 읽어온다.
                lineArr = line; // 파일의 한 줄을 ,로 나누어 배열에 저장 후 리스트로 변환한다.
                csvList.add(lineArr);
            }
        } catch (Exception e) {
        	return readNum(list_file);
        }
        
        return csvList;
	}
	
	// reader에 저장된 값을 모두 StringBuilder형태로 받아옴
	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
	public static void setSSL() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() { return null; }
			public void checkClientTrusted(X509Certificate[] certs, String authType) {}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {}
		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
	
	public static void makeFolder(String path) {
		
		File Folder = new File(path);
		
		if (!Folder.exists()) {
			try{
			    Folder.mkdir(); 
		        } 
		        catch(Exception e){
			    e.getStackTrace();
			}        
		}
	}
	
	
}
