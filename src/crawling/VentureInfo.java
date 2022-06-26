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

public class VentureInfo {

	private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

	public static void setSSL() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
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
		//VentureInfo.infoScraping("390622");
	}

	public static String infoScraping(String vnia_sn)
			throws ParseException, KeyManagementException, NoSuchAlgorithmException, IOException, InterruptedException {
		String result = "";
		int seq;
		int year_count = 0;
		StringBuilder data = new StringBuilder();
		ArrayList list = new ArrayList();

		try {
			String url = "https://www.smes.go.kr/venturein/pbntc/searchVntrCmpDtls?vniaSn=" + vnia_sn;
			if (url.indexOf("https://") >= 0) {
				VentureInfo.setSSL();
			}
			Connection con = Jsoup.connect(url).header("Content-Type", "application/json;charset=UTF-8")
					.userAgent(USER_AGENT).method(Connection.Method.GET).ignoreContentType(true);
			Document doc = con.get();

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter("venture_info.csv", true));

				String busi_num = doc
						.selectXpath("/html/body/div/div[2]/div[3]/div/div[1]/div[1]/table/tbody/tr[4]/td[1]").text()
						.replace("-", "");
				
				Elements elements = doc.selectXpath("/html/body/div/div[2]/div[3]/div/div[1]/div[2]/div/div[1]/div[2]")
						.select("*");
				for (Element element : elements) {
					list.add(element.ownText());
				}
				
				elements = doc.selectXpath("/html/body/div/div[2]/div[3]/div/div[1]/div[2]/div/div[2]/div[2]")
						.select("*");
				for (Element element : elements) {
					list.add(element.ownText());
				}
				
				// 데이터 년도수 조회
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

				// NVentureInfo와 다르게 리스트에 저장된 값을 이용하여 변환시켜줄 필요가 없으므로, 데이터가 존재하는 규칙성에 따라 데이터를 CSV방식으로 저장할수 있도록 함
				for (int j = 1; j <= year_count; j++) {
					seq = 1;
					for (int i = 21; i < 305; i += 5) { // 대차대조표 왼쪽 사항
						data.append(String.valueOf(seq++));
						data.append(","); // sequence
						data.append(busi_num);
						data.append(","); // 사업자번호
						data.append(list.get(15 + j) + "1231");
						data.append(","); // 날짜
						if (list.get(i).toString().contains(",")) // 계정명
							data.append("\"" + list.get(i).toString() + "\"");
						else
							data.append(list.get(i));
						data.append(",");
						if (list.get(i + j).toString().contains(",")) // 계정값
							data.append("\"" + list.get(i + j).toString() + "\"");
						else
							data.append(list.get(i + j));
						bw.write(data.toString());
						bw.newLine();
						data.setLength(0);
					}
					for (int i = 321; i <= 454; i += 5) { // 대차대조표 오른쪽 사항
						data.append(String.valueOf(seq++));
						data.append(","); // sequence
						data.append(busi_num);
						data.append(","); // 사업자번호
						data.append(list.get(15 + j) + "1231");
						data.append(","); // 날짜
						if (list.get(i).toString().contains(",")) // 계정명
							data.append("\"" + list.get(i).toString() + "\"");
						else
							data.append(list.get(i));
						data.append(",");
						if (list.get(i + j).toString().contains(",")) // 계정값
							data.append("\"" + list.get(i + j).toString() + "\"");
						else
							data.append(list.get(i + j));
						bw.write(data.toString());
						bw.newLine();
						data.setLength(0);
					}
					for (int i = 474; i <= 862; i += 5) { // 손익계산서 사항
						data.append(String.valueOf(seq++));
						data.append(","); // sequence
						data.append(busi_num);
						data.append(","); // 사업자번호
						data.append(list.get(15 + j) + "1231");
						data.append(","); // 날짜
						if (list.get(i).toString().contains(",")) // 계정명
							data.append("\"" + list.get(i).toString() + "\"");
						else
							data.append(list.get(i));
						data.append(",");
						if (list.get(i + j).toString().contains(",")) // 계정값
							data.append("\"" + list.get(i + j).toString() + "\"");
						else
							data.append(list.get(i + j));
						bw.write(data.toString());
						bw.newLine();
						data.setLength(0); 
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
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
			System.out.println("\nvniaSn=" + vnia_sn + " / venture_info 오류 발생");
			e.printStackTrace();
			Thread.sleep(15000);
			VentureInfo.infoScraping(vnia_sn);
			result = vnia_sn;
			return result;
		}
		return result;
	}
}
