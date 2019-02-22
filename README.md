# rssFeedReaderAndParser
app that takes an rss feed as input parses the information for data and stores in memory and outputs to console

The program uses the Maven imported libraries of ROM and Jsoup

Rom is used to download the rss feed for the current weather and water conditions from the Halibut Bank Buoy in the Strait of Georgia near
Vancouver B.C. Canada.

The data is cleaned of html tags using jsoup, formatted and added to in memory storage in a Hashmap.

The Hashmap data is then output to the console

todo: save data in a gson object and write to the local file systmem
