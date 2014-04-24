import java.util.HashMap;
import java.util.Map;

public class PricesSorter {
	public static void main(String[] args) {
		String path = "/home/misha-sma/Dropbox/cars_prices.txt";
		String text = Util.loadText(path);
		String[] lines = text.split("\n");
		Map<String, Integer> carsMap = new HashMap<String, Integer>();
		for (String line : lines) {
			String[] words = line.split("\t");
			String carName = words[0];
			String priceStr = words[1];
			int price = Integer.parseInt(priceStr.substring(0, 3)) * 1000 + Integer.parseInt(priceStr.substring(4))
					* 100;
			carsMap.put(carName, price);
		}

		while (carsMap.size() > 0) {
			String minKey = null;
			int minPrice = Integer.MAX_VALUE;
			for (String carName : carsMap.keySet()) {
				int price = carsMap.get(carName);
				if (price < minPrice) {
					minPrice = price;
					minKey = carName;
				}
			}
			System.out.println(minKey + "\t" + minPrice);
			carsMap.remove(minKey);
		}
	}
}
