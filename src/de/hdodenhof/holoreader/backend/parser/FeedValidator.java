package de.hdodenhof.holoreader.backend.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import de.hdodenhof.holoreader.backend.exception.InvalidFeedException;

public class FeedValidator {

    public static String validateFeedAndGetTitle(String urlString) throws InvalidFeedException {

        String name = "";
        boolean isFeed = false;
        boolean isArticle = false;
        boolean hasContent = false;
        boolean hasSummary = false;
        boolean foundName = false;

        try {
            URL url = prepareUrl(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-agent", "Holo Reader/1.0");
            connection.connect();
            String contentType = connection.getContentType();
            if (!contentType.contains("xml")) {
                throw new InvalidFeedException("NOFEED");
            }
            InputStream inputStream = connection.getInputStream();

            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            XmlPullParser pullParser = parserFactory.newPullParser();
            pullParser.setInput(inputStream, null);

            int eventType = pullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String currentTag = pullParser.getName();
                    String currentPrefix = pullParser.getPrefix();

                    if (currentPrefix == null) {
                        currentPrefix = "";
                    }

                    if (currentTag.equalsIgnoreCase("rss") || currentTag.equalsIgnoreCase("feed")
                            || currentTag.equalsIgnoreCase("rdf")) {
                        isFeed = true;
                    } else if (currentTag.equalsIgnoreCase("title") && isFeed && foundName == false) {
                        name = pullParser.nextText();
                        foundName = true;
                    } else if ((currentTag.equalsIgnoreCase("item") || currentTag.equalsIgnoreCase("entry")) && isFeed) {
                        isArticle = true;
                    } else if (((currentTag.equalsIgnoreCase("encoded") && currentPrefix.equalsIgnoreCase("content")) || (currentTag
                            .equalsIgnoreCase("content") && currentPrefix.equalsIgnoreCase(""))) && isArticle == true) {
                        hasContent = true;
                    } else if ((currentTag.equalsIgnoreCase("summary") || currentTag.equalsIgnoreCase("description"))
                            && isArticle == true && currentPrefix.equalsIgnoreCase("")) {
                        hasSummary = true;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String currentTag = pullParser.getName();

                    if ((currentTag.equalsIgnoreCase("item") || currentTag.equalsIgnoreCase("entry")) && isFeed) {
                        break;
                    }
                }
                eventType = pullParser.next();
            }
            inputStream.close();

            if (isFeed && (hasContent || hasSummary)) {

            } else if (isFeed) {
                throw new InvalidFeedException("NO_CONTENT");
            } else {
                throw new InvalidFeedException("NO_FEED");
            }
        } catch (IOException e) {
            throw new InvalidFeedException("IOEXCEPTION");
        } catch (XmlPullParserException e) {
            throw new InvalidFeedException("XMLPULLPARSEREXCEPTION");
        }
        return name;
    }

    private static URL prepareUrl(String url) throws MalformedURLException, InvalidFeedException {
        URL parsedUrl = null;

        if (url.length() < 7 || !url.substring(0, 7).equalsIgnoreCase("http://")) {
            url = "http://" + url;
        }

        Pattern pattern = PATTERN_WEB;
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            parsedUrl = new URL(url);
        } else {
            throw new InvalidFeedException("");
        }

        return parsedUrl;
    }

    /* @formatter:off */
    /**
     * based on AOSP
     */
    private static final Pattern PATTERN_WEB = Pattern.compile(new StringBuilder()
            .append("((?:(http|Http):")
            .append("\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)")
            .append("\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_")
            .append("\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?")
            .append("((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+")
            // named host
            .append("(?:")
            // plus top level domain
            .append("(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])")
            .append("|(?:biz|b[abdefghijmnorstvwyz])")
            .append("|(?:cat|com|coop|c[acdfghiklmnoruvxyz])")
            .append("|d[ejkmoz]")
            .append("|(?:edu|e[cegrstu])")
            .append("|f[ijkmor]")
            .append("|(?:gov|g[abdefghilmnpqrstuwy])")
            .append("|h[kmnrtu]")
            .append("|(?:info|int|i[delmnoqrst])")
            .append("|(?:jobs|j[emop])")
            .append("|k[eghimnrwyz]")
            .append("|l[abcikrstuvy]")
            .append("|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])")
            .append("|(?:name|net|n[acefgilopruz])")
            .append("|(?:org|om)")
            .append("|(?:pro|p[aefghklmnrstwy])")
            .append("|qa")
            .append("|r[eouw]")
            .append("|s[abcdeghijklmnortuvyz]")
            .append("|(?:tel|travel|t[cdfghjklmnoprtvwz])")
            .append("|u[agkmsyz]")
            .append("|v[aceginu]")
            .append("|w[fs]")
            .append("|y[etu]")
            .append("|z[amw]))")
            .append("|(?:(?:25[0-5]|2[0-4]")
            // or ip address
            .append("[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]")
            .append("|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]")
            .append("[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}")
            .append("|[1-9][0-9]|[0-9])))")
            .append("(?:\\:\\d{1,5})?)") // plus option port number
            .append("(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~") // plus option query params
            .append("\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?")
            .append("(?:\\b|$)").toString());
}
