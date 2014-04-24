import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Util {
	private static final int MIN_TIMEOUT = 5000;
	private static final int MAX_TIMEOUT = 10000;

	private static final List<String> STOP_WORDS = loadStopWords();

	public static String loadText(File file) {
		try {
			FileInputStream input = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			input.read(bytes);
			input.close();
			return new String(bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String loadText(String fileName) {
		return loadText(new File(fileName));
	}

	public static void writeBytes2File(byte[] bytes, String path) {
		try {
			FileOutputStream output = new FileOutputStream(path);
			output.write(bytes);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String parseForPrefixWithDelimeter(String text, String prefix, String delimeter) {
		int prefixIndex = text.indexOf(prefix);
		if (prefixIndex == -1) {
			return "";
		}
		int delimeterIndex = text.indexOf(delimeter, prefixIndex + prefix.length());
		if (delimeterIndex == -1) {
			return "";
		}
		return text.substring(prefixIndex + prefix.length(), delimeterIndex);
	}

	public static boolean isTitleTrue(String title) {
		if (title.isEmpty()) {
			System.err.println("Empty title!!!");
			return false;
		}
		title = title.toLowerCase();
		for (String stopWord : STOP_WORDS) {
			if (title.contains(stopWord)) {
				return false;
			}
		}
		return true;
	}

	public static String cleanText(String text) {
		StringBuilder builder = new StringBuilder();
		boolean isTag = false;
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if (isTag) {
				if (c == '>') {
					isTag = false;
				}
			} else {
				if (c == '<') {
					isTag = true;
				} else {
					builder.append(c);
				}
			}
		}
		text = builder.toString();
		String[] lines = text.split("\n");
		builder = new StringBuilder();
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			builder.append(line).append('\n');
		}
		builder.deleteCharAt(builder.length() - 1);
		text = builder.toString();
		text = text.replace("&nbsp;", " ");
		return text;
	}

	public static void sleep() {
		int timeout = MIN_TIMEOUT + (int) (Math.random() * (MAX_TIMEOUT - MIN_TIMEOUT));
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static List<String> loadStopWords() {
		//String text = Util.loadText("config/stop_words.txt");
		//String[] wordsArray = text.split("\n");
		List<String> wordsList = new LinkedList<String>();
//		for (String word : wordsArray) {
//			word = word.trim();
//			if (word.isEmpty()) {
//				continue;
//			}
//			wordsList.add(word.toLowerCase());
//		}
		return wordsList;
	}

}
