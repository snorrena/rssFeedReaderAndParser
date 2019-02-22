package com.rsnorrena;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class App {
    /*
    No need to panic. This is just a test!
    No need to panic. This is just a test!
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        String rrsFeedUrl = "https://www.ndbc.noaa.gov/data/latest_obs/46146.rss";

        boolean ok = false;
        if (rrsFeedUrl.length() != 0) {
            try {


                //create a rss input feed using Rome
                URL feedUrl = new URL(rrsFeedUrl);

                SyndFeedInput input = new SyndFeedInput();

                LOGGER.info("Getting the SyndFeed");
                SyndFeed feed = input.build(new XmlReader(feedUrl));


                //list of string to hold the report data
                List<String> reportData;
                //get a list of entries from the rss feed
                List<SyndEntry> entries = feed.getEntries();

                LOGGER.debug("Debug log message");
                LOGGER.info("Info log message");
                LOGGER.error("Error log message");

                System.out.println("Entries");
                System.out.println("*******");
                System.out.println("Size of Entries array: " + entries.size());
                System.out.println(entries);
                System.out.println();

                //convert the feed data to an array isolating the description values split by a line break '<br />' out of the first entry

                /*
                all item values include a description separated by a ':' with the exception of the first item which is the date

                RSS feed data to be processed and stored locally:

        SyndEntryImpl.description.value=
        <strong>February 22, 2019 9:00 am PST</strong><br />
        <strong>Location:</strong> 49.34N 123.73W<br />
        <strong>Wind Direction:</strong> ESE (110&#176;)<br />
        <strong>Wind Speed:</strong> 25.3 knots<br />
        <strong>Wind Gust:</strong> 31.1 knots<br />
        <strong>Significant Wave Height:</strong> 4.9 ft<br />
        <strong>Dominant Wave Period:</strong> 5 sec<br />
        <strong>Atmospheric Pressure:</strong> 29.81 in (1009.5 mb)<br />
        <strong>Pressure Tendency:</strong> -0.07 in (-2.5 mb)<br />
        <strong>Air Temperature:</strong> 37.4&#176;F (3.0&#176;C)<br />
        <strong>Water Temperature:</strong> 43.3&#176;F (6.3&#176;C)<br />

                 */
                LOGGER.info("Get the entries from the feed extracting the description values and setting to an array");
                reportData = Arrays.asList(entries.get(0).getDescription().getValue().split("<br />"));

                System.out.println("Entries description value split by line break");
                System.out.println("*******");
                System.out.println(reportData);
                System.out.println();

                //local store of the report data in a hashmap as key value pairs
                HashMap<String, String> reportHashMap = new HashMap<>();

                //iterate through the report data array cleaning out the html code with jsoup format and save into the hashmap
                LOGGER.info("iterate through the report data array cleaning out the html code with jsoup format and save into the hashmap");
                for (int arrayIndex = 0; arrayIndex < reportData.size(); arrayIndex++) {
                    String cleanedDataItem = reportData.get(arrayIndex);// get an report item from the array
                    cleanedDataItem = Jsoup.clean(cleanedDataItem, Whitelist.none());//use jsoup to clean out the html code
                    String[] cleanedDataItemDescAndValues = cleanedDataItem.split(":");//split the item description from the data by the separating ':'

                    addReportDataToHashMap(arrayIndex, cleanedDataItemDescAndValues, reportHashMap, cleanedDataItem);
                }

                System.out.println("Date: " + reportHashMap.get("Date"));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(reportHashMap.get("Date"), formatter);

                System.out.println("LocalDateTime object converted from string: " + dateTime);


                System.out.println("Location: " + reportHashMap.get("Location"));
                System.out.println("Wind Direction Letters: " + reportHashMap.get("Wind Direction Letters"));
                System.out.println("Wind Direction Degrees: " + reportHashMap.get("Wind Direction Degrees"));
                System.out.println("Wind Speed: " + reportHashMap.get("Wind Speed"));
                System.out.println("Wind Gust: " + reportHashMap.get("Wind Gust"));
                System.out.println("Significant Wave Height: " + reportHashMap.get("Significant Wave Height"));
                System.out.println("Dominant Wave Period: " + reportHashMap.get("Dominant Wave Period"));
                System.out.println("Atmospheric Pressure: " + reportHashMap.get("Atmospheric Pressure"));
                System.out.println("Pressure Tendency: " + reportHashMap.get("Pressure Tendency"));
                System.out.println("Air Temperature: " + reportHashMap.get("Air Temperature"));
                System.out.println("Water Temperature: " + reportHashMap.get("Water Temperature"));

                ok = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        if (!ok) {
            LOGGER.error("The app was uable to coonect to the rss feed");
        }
    }

    private static void addReportDataToHashMap(int arrayIndex, String[] cleanedDataItemsDescAndValues, HashMap<String, String> reportHashMap, String cleanedDataItem) {

        if (2 == cleanedDataItemsDescAndValues.length) {

            switch (arrayIndex) {
                case 0://date
                    reportHashMap.put("Date", convertDateString(cleanedDataItem));//method to covert the data to a string that can be parsed to a LocalDateTime object
                    break;
                case 1://location
                    reportHashMap.put(cleanedDataItemsDescAndValues[0], cleanedDataItemsDescAndValues[1].trim());
                    break;
                case 2://wind direction
                    reportHashMap.put("Wind Direction Letters", stripOutAllButLetters(cleanedDataItemsDescAndValues[1]));
                    reportHashMap.put("Wind Direction Degrees", stripOutAllButNumbers(cleanedDataItemsDescAndValues[1]));
                    break;
                case 3://wind speed
                case 4://wind gust
                case 5://wave height
                case 6://wave period
                    reportHashMap.put(cleanedDataItemsDescAndValues[0], stripOutAllButNumbers(cleanedDataItemsDescAndValues[1]));
                    break;
                case 7:// Atmospheric pressure
                case 8://Pressure Tendency
                    reportHashMap.put(cleanedDataItemsDescAndValues[0], cleanedDataItemsDescAndValues[1].trim());
                    break;
                case 9:
                case 10:
                    String[] waterAirTemp = cleanedDataItemsDescAndValues[1].trim().split(" ");
                    reportHashMap.put(cleanedDataItemsDescAndValues[0], stripOutAllButNumbers(waterAirTemp[1]));
                    break;
            }

        }
    }

    private static String stripOutAllButLetters(String s) {
        return s.replaceAll("[^a-zA-Z]", "");
    }

    private static String stripOutAllButNumbers(String wdDegrees) {
        return wdDegrees.replaceAll("[^\\d.]", "");
    }

    private static String convertDateString(String reportDateString) {

        String[] tmp = reportDateString.split(" ");

        /*
        The report date string is split into its individual components
         original format: 'February 22, 2019 10:00 am PST<'
         index 0 = Month, index 1 = day of month, index 3 = year etc...
         */

        //the month is converted to a numeric format
        String month = tmp[0].trim();

        switch (month) {
            case "January":
                month = "01";
                break;
            case "February":
                month = "02";
                break;
            case "March":
                month = "03";
                break;
            case "April":
                month = "04";
                break;
            case "May":
                month = "05";
                break;
            case "June":
                month = "06";
                break;
            case "July":
                month = "07";
                break;
            case "August":
                month = "08";
                break;
            case "September":
                month = "09";
                break;
            case "October":
                month = "10";
                break;
            case "November":
                month = "11";
                break;
            case "December":
                month = "12";
                break;
        }

        //the comma and extra space is removed from the day of month
        String dayOfMonth = tmp[1].replace(",", "").trim();

        //the year string is trimmed of leading and trailing white space
        String year = tmp[2].trim();

        String tmpTime = tmp[3].trim();

        String[] hoursMinutes = tmpTime.split(":");

        String time;

        int tmpHour = Integer.parseInt(hoursMinutes[0]);


        //the time is 12 hour and must be converted to 24 for conversion to local date time object
        //if the date string contains pm add 12 hours to the hours
        if (tmp[4].contains("pm")) {

            tmpHour = tmpHour + 12;

            time = tmpHour + ":" + hoursMinutes[1];
        } else {

            time = tmp[3];
        }

        //add a leading zero if the hour is less than ten for sake of parse to LocalDateTime Object
        if (tmpHour < 10) {
            time = "0" + time;
        }

        return year + "-" + month + "-" + dayOfMonth + " " + time;
    }
}
