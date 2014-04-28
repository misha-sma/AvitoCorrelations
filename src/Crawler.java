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
	private static final String BASE_URL = "http://www.avito.ru";
	private static final String BASE_URL_1 = BASE_URL + "/sankt-peterburg/avtomobili_s_probegom/";
	private static final String MARK = "renault";
	private static final String MODEL = "logan";
	private static final String BASE_URL_2 = BASE_URL_1 + MARK + "/" + MODEL;
	private static final Pattern TAB_PATTERN = Pattern.compile("<a href=\"/sankt-peterburg/avtomobili_s_probegom/"
			+ MARK + "/" + MODEL + "\\?p=[0-9]+\">([0-9]+)</a>");
	private static final Pattern URL_PATTERN = Pattern.compile("<a[ ]+href=\"(/sankt-peterburg/avtomobili_s_probegom/"
			+ MARK + "_" + MODEL + "_[0-9]+_[0-9]+)");
	private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	private static final Pattern PRICE_PATTERN = Pattern.compile("<strong itemprop=\"price\">([0-9]+&nbsp;[0-9]+)");
	private static final Pattern RUN_PATTERN = Pattern.compile("title=\"Пробег &mdash;([0-9 ]+)-([0-9 ]+)");
	private static final Pattern VOLUME_PATTERN = Pattern
			.compile(",[ ]+(1.\\d)[ ]+<a href=\"/sankt-peterburg/avtomobili_s_probegom");
	private static final Pattern COLOR_PATTERN = Pattern.compile(",[ ]+цвет[ ]+(*)[ ]+</div>[ ]+</div>");
	private static final Pattern BROKEN_PATTERN = Pattern.compile(",[ ]+битый[ ]+</div>[ ]+</div>");
	private static final Pattern DILER_PATTERN = Pattern
			.compile("<a href=\"/auto-traider\" title=\"Перейти на страницу автодилера &laquo;(*)&raquo;");
	private static final Pattern FULLTEXT_PATTERN = Pattern
			.compile("<div class=\"description description-text\"> <div id=\"desc_text\" itemprop=\"description\"><p>(*)<div class=\"item_sku\">");

	private static final CloseableHttpClient httpClient = HttpClients.createDefault();

	public static void main(String[] args) {
		int pagesCount = getPagesCount();
		System.out.println("pagesCount=" + pagesCount);
		try {
			for (int pageNumber = 1; pageNumber <= pagesCount; ++pageNumber) {
				System.out.println("PAGE NUMBER=" + pageNumber);
				Util.sleep();
				HttpGet httpGet = new HttpGet(BASE_URL_2 + "?p=" + pageNumber);
				prepareRequest(httpGet);
				CloseableHttpResponse response = httpClient.execute(httpGet);
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String html = EntityUtils.toString(entity);
				// Util.writeBytes2File(html.getBytes(),
				// "/home/misha-sma/AvitoCorrelations/page_" + pageNumber +
				// ".html");
				EntityUtils.consume(entity);
				response.close();
				List<String> urls = getAdUrls(html);
				System.out.println("urls count=" + urls.size());
				System.out.println("urls=" + urls);
				// System.exit(0);
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
				String html = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				response.close();
				String[] textWithImageUrl = parseText(html, url);
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

	private static String[] parseText(String html, String url) {
		Matcher brokenMatcher = BROKEN_PATTERN.matcher(html);
		if (brokenMatcher.find()) {
			return null;
		}

		Matcher yearMatcher = YEAR_PATTERN.matcher(url);
		int year = yearMatcher.find() ? Integer.parseInt(yearMatcher.group()) : 0;
		Matcher priceMatcher = PRICE_PATTERN.matcher(html);
		int price = priceMatcher.find() ? Integer.parseInt(priceMatcher.group(1).replace("&nbsp;", "")) : 0;
		Matcher runMatcher = RUN_PATTERN.matcher(html);
		int run = runMatcher.find() ? Integer.parseInt(runMatcher.group(1).replace(" ", "")) : 0;
		Matcher volumeMatcher = VOLUME_PATTERN.matcher(html);
		double volume = volumeMatcher.find() ? Integer.parseInt(volumeMatcher.group(1)) : 0;
		Matcher colorMatcher = COLOR_PATTERN.matcher(html);
		String color = colorMatcher.find() ? colorMatcher.group(1) : "";
		Matcher dilerMatcher = DILER_PATTERN.matcher(html);
		String diler = dilerMatcher.find() ? dilerMatcher.group(1) : "";
		Matcher fulltextMatcher = FULLTEXT_PATTERN.matcher(html);
		String fulltext = fulltextMatcher.find() ? fulltextMatcher.group(1).replace("<p>", "").replace("</p>", "")
				.replace("</div>", "") : "";
		return null;
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
		Matcher urlMatcher = URL_PATTERN.matcher(text);
		while (urlMatcher.find()) {
			String url = BASE_URL + urlMatcher.group(1);
			urls.add(url);
		}
		return urls;
	}

}
