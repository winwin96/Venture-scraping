package crawling;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import org.json.JSONException;
import org.json.JSONObject;

public class NVentureInfo {
	//변수 설정
	static StringBuilder data = new StringBuilder();
	static String year = "";
	static String busi_num = "";
	static ArrayList list = new ArrayList();
	
	//벤처기업 상세정보에서 스크래핑한 값들을 리스트로 저장하였을 때 유효한 정보가 어떤 인덱스에 저장되어 있는지 나타내기 위한 배열
	//예를 들어, LIST_INDEX[1] = 22는 list.get(22)를 통해 첫번째 항목인 유동자산에 대한 정보를 얻을 수 있음
	static int[] LIST_INDEX = { 0, 22, 27, 32, 37, 42, 47, 52, 57, 62, 67, 72, 77, 82, 87, 92, 97, 102, 107, 112, 117, 
			122, 127, 132, 137, 142, 147, 152, 157, 162, 167, 172, 177, 182, 187, 192, 197, 202, 207, 212, 217, 222,
			227, 232, 237, 242, 247, 252, 257, 262, 267, 272, 277, 282, 287, 292, 297, 302, 322, 327, 332, 337, 342,
			347, 352, 357, 362, 367, 372, 377, 382, 387, 392, 397, 402, 407, 412, 417, 422, 427, 432, 437, 442, 447,
			452, 475, 480, 485, 490, 495, 500, 505, 510, 515, 520, 525, 530, 535, 540, 545, 550, 555, 560, 565, 570,
			575, 580, 585, 590, 595, 600, 605, 610, 615, 620, 625, 630, 635, 640, 645, 650, 655, 660, 665, 670, 675,
			680, 685, 690, 695, 700, 705, 710, 715, 720, 725, 730, 735, 740, 745, 750, 755, 760, 765, 770, 775, 780,
			785, 790, 795, 800, 805, 810, 815, 820, 825, 830, 835, 840, 845, 850, 855, 860 };
	// 계정코드를 순서별로 저장한 배열
		static String[] ACCT_CD = { "M11000", "M11100", "M11117", "M11008", "M11120", "M11124", "M11126", "M11130",
				"M11133", "M11140", "M11144", "M11150", "M11159", "M11180", "M11182", "M11190", "M11193", "M11220",
				"M11223", "M11500", "M11510", "M11513", "M11520", "M11521", "M11523", "M11530", "M11534", "M11540",
				"M11542", "M11550", "M11551", "M11552", "M11556", "M11581", "M11586", "M11570", "M12000", "M12100",
				"M12124", "M12129", "M12130", "M12143", "M12150", "M12151", "M12152", "M12251", "M12300", "M12310",
				"M12370", "M12320", "M12330", "M12340", "M12350", "M12360", "M12381", "M12388", "M12500", "M12510",
				"M12520", "M12525", "M12563", "M12570", "M12700", "M12160", "M12191", "M12195", "M12180", "M12183",
				"M12198", "M14900", "M15000", "M15010", "M15013", "M15020", "M15027", "M15030", "M15035", "M15040",
				"M15044", "M15050", "M15056", "M15060", "M15084", "M15110", "M15116", "M15130", "M15134", "M15150",
				"M16000", "M16200", "M16230", "M16300", "M16330", "M16350", "M16356", "M16400", "M16410", "M16460",
				"M16470", "M16500", "M16530", "M16550", "M16551", "M16552", "M16570", "M16900", "M17000", "M17120",
				"M19599", "M19800", "M19000", "M19425", "M19900", "M21000", "M21110", "M21120", "M21140", "M21141",
				"M21151", "M21170", "M21220", "M21230", "M22000", "M22110", "M22111", "M22112", "M22116", "M22114",
				"M22117", "M22120", "M22124", "M22122", "M22230", "M23000", "M24000", "M24100", "M24110", "M24112", 
				"M24116", "M24120", "M24124", "M24130", "M24300", "M24310", "M24320", "M24330", "M24340", "M24350", 
				"M24360", "M24370", "M24380", "M24390", "M24420", "M24430", "M24440", "M24450", "M24470", "M24481", 
				"M24510", "M24700", "M24710", "M24730", "M24770", "M24790", "M24810", "M24820", "M24880", "M24886", 
				"M24910", "M25000", "M26100", "M26110", "M26120", "M26140", "M26144", "M26180", "M26190", "M26230", 
				"M26240", "M26250", "M26277", "M26316", "M26320", "M26500", "M26510", "M26530", "M26535", "M26540", 
				"M26544", "M26561", "M26620", "M26630", "M26660", "M26670", "M26680", "M26730", "M26703", "M26702", 
				"M29000", "M29600", "M29900" };
		// 계정금액을 산출하기 위한 재무매핑산식을 적어놓은 배열
		static String[] CALC = { "1", "2", "3", "3", "4", "4", "5", "8", "8", "6+7", "6+7", "10+11", "10+11", "9", "9",
				"12", "12", "13+14", "13+14", "15", "16", "16", "17+23", "23", "17", "18", "18", "24", "24", "19+20+22",
				"19", "20", "22", "21", "21", "25+26", "27", "28", "29", "29", "30", "30", "31", "33", "32", "34", "35",
				"36", "44", "37", "38", "39", "40", "42", "43", "41+45", "46", "47", "48", "48", "49", "50", "51", "52",
				"54", "53", "55", "55", "56", "57", "58", "60", "60", "59", "59", "62", "62", "61", "61", "63", "63", "64",
				"64", "65", "65", "66", "66", "67", "68", "69", "69", "71", "71", "72", "72", "76+77+78", "76", "78",
				"77", "73+74+75+79", "73", "74+75", "74", "75", "79", "80", "81", "81", "82", "83", "83-81", "83-81", "84",
				"85", "86", "87", "88+89", "88", "89", "91", "90", "92", "93", "94", "95", "96", "98", "95+96", "97", "100",
				"100", "100", "101", "102", "103", "104+105+106+107", "104+105", "104", "105", "106", "106", "107",
				"108+113+110+111+112+116+109+117+119+114+131+123+125+126+127+132+115+133+120", "108+113", "110", "111+112",
				"116", "109", "117", "119", "114", "131", "123", "125", "126", "127+132", "115", "133", "120",
				"121+122+130+124+128+129+118+134", "121", "122", "130", "124", "128", "129", "118", "118", "134", "135",
				"136", "137", "138", "141", "141", "139", "140", "145", "142", "143", "146", "144",
				"136-137-138-139-140-141-142-143-144-145-146", "148", "149", "152", "159", "154", "154", "157", "150",
				"151", "153", "155", "156", "161", "158", "160", "135+136-148", "135+136-148", "162" };

	private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

	//https 호출시 SSL 신뢰받지 않는 인증서 예외 처리 
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

	public static void main(String args[])
			throws KeyManagementException, NoSuchAlgorithmException, ParseException, IOException, InterruptedException {
		//NVentureInfo.infoScraping("1026082");
	}

	public static void infoScraping(String vnia_sn, String folder_time)
			throws ParseException, KeyManagementException, NoSuchAlgorithmException, IOException, InterruptedException {
		year = "";
		busi_num = "";
		list.clear();
		String result = "";
		int seq;
		int year_count = 0;

		try {
			String url = "https://www.smes.go.kr/venturein/pbntc/searchVntrCmpDtls?vniaSn=" + vnia_sn;
			if (url.indexOf("https://") >= 0) {
				NVentureInfo.setSSL();
			}
			
			//연결 설정
			Connection con = Jsoup.connect(url).header("Content-Type", "application/json;charset=UTF-8")
					.userAgent(USER_AGENT).method(Connection.Method.GET).ignoreContentType(true);
			Document doc = con.get();

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter("./result/" + folder_time +"/" +"Nventure_info.csv", true));
				
				//사업자번호
				busi_num = doc.selectXpath("/html/body/div/div[2]/div[3]/div/div[1]/div[1]/table/tbody/tr[4]/td[1]")
						.text().replace("-", "");
				
				//재무정보-대차대조표에 해당하는 값 저장
				Elements elements = doc.selectXpath("/html/body/div/div[2]/div[3]/div/div[1]/div[2]/div/div[1]/div[2]")
						.select("*");
				for (Element element : elements) {
					list.add(element.ownText());
				}
				
				//재무정보-손익계산서에 해당하는 값 저장
				elements = doc.selectXpath("/html/body/div/div[2]/div[3]/div/div[1]/div[2]/div/div[2]/div[2]")
						.select("*");
				for (Element element : elements) {
					list.add(element.ownText());
				}

				//데이터 년도수 조회
				if (list.size() > 4) {
					if (list.get(18) != "")
						year_count = 3;
					else if (list.get(17) != "")
						year_count = 2;
					else if (list.get(16) != "")
						year_count = 1;
				} else {
					year_count = 0;
				}
				
				//데이터 년도수에 따라 스크래핑 진행
				for (int j = 1; j <= year_count; j++) {
					seq = 1;
					year = list.get(15 + j) + "1231";

					for (int i = 0; i < ACCT_CD.length; i++) {
						bw.write(putData(year, ACCT_CD[i], i < 112 ? "1" : "2", CALC[i], j));
						bw.newLine();
					}
				}

			} catch (Exception e) {
				//오류가 발생했으므로 해당 사이트의 vnia_sn값을 리턴한다
				Thread.sleep(1000);
				NVentureInfo.infoScraping(vnia_sn, folder_time);
				return;
			} finally {
				try {
					if (bw != null) {
						bw.flush();
						bw.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			//오류가 발생할 시 사이트 차단을 풀기 위해 멈추고 함수를 다시 호출한다
			//오류가 발생했으므로 해당 사이트의 vnia_sn값을 리턴한다
			Thread.sleep(15000);
			NVentureInfo.infoScraping(vnia_sn, folder_time);
			return;
		}
		return;
	}

	//스크래핑 정보를 CSV방식으로 저장하기 위한 메소드
	public static String putData(String year, String code, String number, String f_calc, int j) throws IOException {
		data.setLength(0);
		String temp = "";
		String[] num_list; 
		Long sum = (long) 0;
		
		data.append(busi_num).append(",").append("FLS").append(",").append("Y").append(",").append(year).append(",")
				.append(code).append(",").append(number).append(",");
		//매핑산식에 산식이 있을 경우
		if (f_calc.contains("+") || f_calc.contains("-")) {
			f_calc = f_calc.replace("-", "-_");
			num_list = f_calc.split("\\+|-");
			for(int i=0 ; i<num_list.length ; i++) {
				if(num_list[i].contains("_")) {
					sum = sum - Long.valueOf(((String) list.get(LIST_INDEX[Integer.valueOf(num_list[i].replace("_", ""))] + j - 1)).replace(",", ""));
				}
				else {
					sum = sum + Long.valueOf(((String) list.get(LIST_INDEX[Integer.valueOf(num_list[i])] + j - 1 )).replace(",", ""));
				}
			}
			temp = String.valueOf(sum).replace(",", "");
		}		
		//매핑산식에 계산식이 없을 경우 
		else {
			temp = ((String) list.get(LIST_INDEX[Integer.valueOf(f_calc)] + j - 1)).replace(",", "");
		}
		data.append(temp);
		return data.toString();
	}
}
