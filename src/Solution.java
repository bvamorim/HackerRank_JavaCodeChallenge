import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Solution {

    final static Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().registerTypeAdapter(ServerResponse.class, new ServerResponse.Deserializer()).create();
    final static String defaultFormat = "d-MMMMM-yyyy";	
	
    static class StockInfo {
        private Date date;
        private float open;
        private float close;
        private float high;
        private float low;

        public StockInfo(Date date, float open, float close, float high, float low) {
            this.date = date;
            this.open = open;
            this.close = close;
            this.high = high;
            this.low = low;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public float getOpen() {
            return open;
        }

        public void setOpen(float open) {
            this.open = open;
        }

        public float getClose() {
            return close;
        }

        public void setClose(float close) {
            this.close = close;
        }

        public float getHigh() {
            return high;
        }

        public void setHigh(float high) {
            this.high = high;
        }        
        
        public float getLow() {
            return low;
        }

        public void setLow(float low) {
            this.low = low;
        }          

    }

    static class ServerResponse {

        private int page;
        private int perPage;
        private int total;
        private int totalPages;
        private List<StockInfo> data;

        public ServerResponse(int page, int perPage, int total, int totalPages, List<StockInfo> data) {

            this.page = page;
            this.perPage = perPage;
            this.total = total;
            this.totalPages = totalPages;
            this.data = data;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public List<StockInfo> getData() {
            return data;
        }

        public void setData(List<StockInfo> data) {
            this.data = data;
        }

        static final class Deserializer implements JsonDeserializer<ServerResponse> {

            /**
             * {@inheritDoc}
             */
            @Override
            public ServerResponse deserialize(final JsonElement json, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException {
                final JsonObject jObject = json.getAsJsonObject();
                final int page = jObject.get("page").getAsInt();
                final int perPage = jObject.get("per_page").getAsInt();
                final int total = jObject.get("total").getAsInt();
                final int totalPages = jObject.get("total_pages").getAsInt();
                final JsonArray jsonArray = jObject.get("data").getAsJsonArray();
                final List<StockInfo> stockList = new ArrayList<>();

                for (int i = 0; i < jsonArray.size(); i++) {
                    final JsonObject stockJson = jsonArray.get(i).getAsJsonObject();
                    final String dateStr = stockJson.get("date").getAsString();
                    final float open = stockJson.get("open").getAsFloat();
                    final float close = stockJson.get("close").getAsFloat();
                    final float high = stockJson.get("high").getAsFloat();
                    final float low = stockJson.get("low").getAsFloat();

                    Date date = stringToDate(dateStr);			

                    final StockInfo StockInfo = new StockInfo(date, open, close, high, low);
                    stockList.add(StockInfo);
                }

                return new ServerResponse(page, perPage, total, totalPages, stockList);
            }

        }
    }
    
    static Date stringToDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat(defaultFormat, Locale.ENGLISH);  
        Date dateReturn = null;
		try {
			dateReturn = format.parse(date);
		} catch (ParseException e) {
	        e.printStackTrace();
		}             
		return dateReturn;
    	
    }
    
    static String dateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(defaultFormat, Locale.ENGLISH);  
        String dateReturn = null;
		dateReturn = format.format(date);             
		return dateReturn;
    }    
    
    static String getWeekDay(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE", Locale.ENGLISH);  
        String dateReturn = null;
		dateReturn = format.format(date);             
		return dateReturn;
    }    

    /**
     * This returns list of stocks queried by the date and the page number.
     *
     * @param searchDate
     * @param weekDay
     * @param pageNumber
     * @return
     * @throws IOException
     */
    static List<String> getData(Date searchDate, String weekDay) throws IOException {
        String serverReply = makeServerCall(searchDate, weekDay);

        ServerResponse response = gson.fromJson(serverReply, ServerResponse.class);
        List<String> results = new ArrayList<>();
        for (StockInfo stock : response.getData()) {
        	if((weekDay.equals(getWeekDay(stock.date))) &&
        	   (searchDate.equals(stock.date))) {
                results.add(dateToString(stock.date) + " " + stock.open + " " + stock.close);
        	}
        }
        return results;
    }

    /*
     * The heart of this class, makes the server call and retrieves json string as server reply.
     */
    static String makeServerCall(Date searchDate, String weekDay) throws IOException{

        URL url;
        url= new URL("https://jsonmock.hackerrank.com/api/stocks/search?date=" + dateToString(searchDate));
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String temp;
        StringBuffer output = new StringBuffer();

        while ((temp = br.readLine()) !=null ) {
            output.append(temp);
        }

        return output.toString();
    }
        
    public static String[] openAndClosePricesList(String firstDate, String lastDate, String weekDay) throws IOException {
        List<String> results = new ArrayList<>();
        
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(stringToDate(firstDate));
        
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(stringToDate(lastDate));
        
	    while (calendar.before(endCalendar)) {
	        Date searchDate = calendar.getTime();
	        results.addAll(getData(searchDate, weekDay));	        
	        calendar.add(Calendar.DATE, 1);
	    }

        return results.toArray(new String[results.size()]);
    }  
    
    static void openAndClosePrices(String firstDate, String lastDate, String weekDay) {
        try {
            String[] stockList = openAndClosePricesList(firstDate, lastDate, weekDay);
            for(String str:stockList) {
                System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }        

    public static void main(String[] args) {

    	String firstDate = "1-January-2000";
    	String lastDate = "22-February-2000";
    	String weekDay = "Monday";
    	
    	openAndClosePrices(firstDate, lastDate, weekDay);


    }
}

