import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Crawler {
	private static final String BASE_URL = "http://www.avito.ru/sankt-peterburg/avtomobili_s_probegom/";
	private static final String MARK = "renault";
	private static final String MODEL = "logan";
	private static final String BASE_URL_2 = BASE_URL + MARK + "/" + MODEL;
	private static final Pattern TAB_PATTERN = Pattern
			.compile("<a href=\"/sankt-peterburg/avtomobili_s_probegom/renault/logan\\?p=[0-9]+\">([0-9]+)</a>");

	private static final String FIRST_PART_URL = "http://www.all-sbor.net/board/cat_29.html?page=";
	private static final String SECOND_PART_URL = "&pagestyle=collapsed&stronpage=50&part=2";

	private static final CloseableHttpClient httpClient = HttpClients.createDefault();

	public static void main(String[] args) {
		int pagesCount = getPagesCount();
		System.out.println("pagesCount=" + pagesCount);
		System.exit(0);
		try {
			for (int pageNumber = 1; pageNumber <= pagesCount; ++pageNumber) {
				System.out.println("PAGE NUMBER=" + pageNumber);
				Util.sleep();
				HttpGet httpGet = new HttpGet(FIRST_PART_URL + pageNumber + SECOND_PART_URL);
				prepareRequest(httpGet);
				CloseableHttpResponse response = httpClient.execute(httpGet);
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String html = EntityUtils.toString(entity, "cp1251");
				EntityUtils.consume(entity);
				response.close();
				List<String> urls = getAdUrls(html);
				System.out.println("urls count=" + urls.size());
				System.out.println("urls=" + urls);
				dowloadUrls(urls);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int getPagesCount() {
		HttpGet httpGet = new HttpGet(BASE_URL_2);
		prepareRequest(httpGet);
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			String html = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			response.close();
			return parsePagesCount(html);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static void prepareRequest(HttpGet httpGet) {
		httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpGet.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
	}

	private static void dowloadUrls(List<String> urls) {
		try {
			for (String url : urls) {
				Util.sleep();
				HttpGet httpGet = new HttpGet(url);
				prepareRequest(httpGet);
				CloseableHttpResponse response = httpClient.execute(httpGet);
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String html = EntityUtils.toString(entity, "cp1251");
				EntityUtils.consume(entity);
				response.close();
				String[] textWithImageUrl = parseText(html);
				String text = textWithImageUrl[0];
				String imageUrl = textWithImageUrl[1];
				System.out.println(text);
				System.out.println(imageUrl);
				System.out.println("--------------------------------------------------------------------");
				System.out.println();
				int idCar = CarDao.addCar(text, url);
				if (!imageUrl.isEmpty()) {
					downloadImage(imageUrl, idCar);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void downloadImage(String url, int idCar) {
		Util.sleep();
		HttpGet httpGet = new HttpGet(url);
		prepareRequest(httpGet);
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			byte[] bytes = EntityUtils.toByteArray(entity);
			Util.writeBytes2File(bytes, "images/" + idCar + ".jpg");
			EntityUtils.consume(entity);
			response.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String[] parseText(String html) {
		String dirtyText = Util.parseForPrefixWithDelimeter(html, "<td class=\"rub\">",
				"<table cellspacing=\"0\" cellpadding=\"0\" class=\"table100\"");
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";
		if (dirtyText.isEmpty()) {
			System.err.println("Empty ad!!!");
			return result;
		}
		String text = Util.cleanText(dirtyText);
		String imageUrl = Util.parseForPrefixWithDelimeter(dirtyText, "<img src=\"", "\" style=");
		result[0] = text;
		result[1] = imageUrl.isEmpty() ? "" : BASE_URL + imageUrl;
		return result;
	}

	private static int parsePagesCount(String text) {
		Matcher tabMatcher = TAB_PATTERN.matcher(text);
		int tabsCount = 0;
		while (tabMatcher.find()) {
			tabsCount = Integer.parseInt(tabMatcher.group(1));
		}
		return tabsCount;
	}

	private static List<String> getAdUrls(String text) {
		List<String> urls = new LinkedList<String>();
		String[] dirtyUrls = text.split("<a href=\"/board/details_");
		for (int i = 1; i < dirtyUrls.length; ++i) {
			int index = dirtyUrls[i].indexOf("\">");
			if (index == -1) {
				continue;
			}
			String title = Util.parseForPrefixWithDelimeter(dirtyUrls[i], "\">", "</a>");
			if (!Util.isTitleTrue(title)) {
				continue;
			}
			String url = BASE_URL + "/board/details_" + dirtyUrls[i].substring(0, index);
			urls.add(url);
		}
		return urls;
	}

}
