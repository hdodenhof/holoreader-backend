package de.hdodenhof.holoreader.backend.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import de.hdodenhof.holoreader.backend.exception.InvalidFeedException;

public class FeedValidator {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FeedValidator.class.getName());

    public static final String RESULT_NAME = "name";
    public static final String RESULT_URL = "url";

    public static Map<String, String> parseFeed(String urlString) throws InvalidFeedException {
        String resultUrl = urlString;
        String name = null;
        String error = null;

        try {
            urlString = urlString.trim();
            logger.info("[" + urlString + "] Start");

            URL url = prepareUrl(urlString);

            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-agent", "Holo Reader/1.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            String contentType = connection.getContentType();
            if (contentType.contains("xml")) {
                logger.info("[" + urlString + "] XML content type detected");
                InputStream inputStream = connection.getInputStream();
                try {
                    name = validateFeedAndExtractName(inputStream);
                    logger.info("[" + urlString + "] Feed is valid!");
                } catch (InvalidFeedException e) {
                    error = e.getMessage();
                    logger.warning("[" + urlString + "] Feed is invalid!");
                } finally {
                    inputStream.close();
                }
            } else {
                logger.info("[" + urlString + "] Invalid content type, trying to discover feed");
                String alternateUrl = discoverFeed(url);
                if (alternateUrl == null) {
                    logger.warning("[" + urlString + "] No feed discovered, aborting!");
                    throw new InvalidFeedException();
                } else {
                    logger.info("[" + urlString + "] Discovered " + alternateUrl);
                    URLConnection secondConnection = new URL(alternateUrl).openConnection();
                    secondConnection.setRequestProperty("User-agent", "Holo Reader/1.0");
                    secondConnection.setConnectTimeout(2000);
                    secondConnection.setReadTimeout(2000);
                    secondConnection.connect();

                    InputStream inputStream = secondConnection.getInputStream();
                    try {
                        name = validateFeedAndExtractName(inputStream);
                        logger.info("[" + urlString + "] Discovered feed is valid.");
                        resultUrl = alternateUrl;
                    } catch (InvalidFeedException e) {
                        error = e.getMessage();
                        logger.warning("[" + urlString + "] Discovered feed is invalid!");
                    } finally {
                        inputStream.close();
                    }
                }
            }

            if (name == null) {
                throw new InvalidFeedException(error);
            }
        } catch (IOException e) {
            logger.severe("[" + urlString + "] Exception: " + e.getMessage());
            throw new InvalidFeedException("IOEXCEPTION");
        } catch (InvalidFeedException e) {
            // no further logging
            throw new InvalidFeedException();
        } catch (Exception e) {
            logger.severe("[" + urlString + "] Exception: " + e.getMessage());
            throw new InvalidFeedException();
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put(RESULT_NAME, name);
        result.put(RESULT_URL, resultUrl);

        logger.info("[" + urlString + "] Success: " + resultUrl + " - " + name);
        return result;
    }

    private static String validateFeedAndExtractName(InputStream inputStream) throws InvalidFeedException {
        String name = "";
        boolean foundName = false;
        boolean isFeed = false;
        boolean isArticle = false;
        boolean hasContent = false;
        boolean hasSummary = false;

        try {

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

                    if (currentTag.equalsIgnoreCase("rss") || currentTag.equalsIgnoreCase("feed") || currentTag.equalsIgnoreCase("rdf")) {
                        isFeed = true;
                    } else if (currentTag.equalsIgnoreCase("title") && isFeed && foundName == false) {
                        name = pullParser.nextText();
                        foundName = true;
                    } else if ((currentTag.equalsIgnoreCase("item") || currentTag.equalsIgnoreCase("entry")) && isFeed) {
                        isArticle = true;
                    } else if (((currentTag.equalsIgnoreCase("encoded") && currentPrefix.equalsIgnoreCase("content")) || (currentTag
                            .equalsIgnoreCase("content") && currentPrefix.equalsIgnoreCase(""))) && isArticle == true) {
                        hasContent = true;
                    } else if ((currentTag.equalsIgnoreCase("summary") || currentTag.equalsIgnoreCase("description")) && isArticle == true
                            && currentPrefix.equalsIgnoreCase("")) {
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
        } catch (IOException e) {
            throw new InvalidFeedException("IOEXCEPTION");
        } catch (XmlPullParserException e) {
            throw new InvalidFeedException("XMLPULLPARSEREXCEPTION");
        }

        if (isFeed && (hasContent || hasSummary)) {
            return name;
        } else if (isFeed) {
            throw new InvalidFeedException("NO_CONTENT");
        } else {
            throw new InvalidFeedException("NO_FEED");
        }
    }

    private static String discoverFeed(URL url) {
        try {
            Document document = Jsoup.connect(url.toString()).userAgent("Holo Reader/1.0").timeout(2000).get();
            String rssUrl = document.select("link[rel=alternate][type=application/rss+xml]").attr("href");
            if (rssUrl == null || rssUrl == "") {
                rssUrl = document.select("link[rel=alternate][type=application/atom+xml]").attr("href");
            }

            if (rssUrl == null || rssUrl == "") {
                return null;
            } else {
                Pattern pattern = WEB_URL;
                Matcher matcher = pattern.matcher(rssUrl);

                if (matcher.matches()) {
                    return rssUrl;
                } else {
                    logger.info("[" + url + "] Discovered URL (" + rssUrl + ") invalid");
                    return null;
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static URL prepareUrl(String url) throws MalformedURLException, InvalidFeedException {
        String requestUrl = url;
        URL parsedUrl = null;

        if (url.length() < 7 || (!url.substring(0, 7).equalsIgnoreCase("http://") && !url.substring(0, 8).equalsIgnoreCase("https://"))) {
            url = "http://" + url;
        }

        Pattern pattern = WEB_URL;
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            parsedUrl = new URL(url);
            logger.info("[" + requestUrl + "] Parsed URL: " + parsedUrl);
        } else {
            logger.warning("[" + requestUrl + "] URL invalid, aborting.");
            throw new InvalidFeedException("URL invalid");
        }

        return parsedUrl;
    }

    /**
     * Taken from AOSP
     */
    /* @formatter:off */
    
    /**
     *  Regular expression to match all IANA top-level domains for WEB_URL.
     *  List accurate as of 2011/07/18.  List taken from:
     *  http://data.iana.org/TLD/tlds-alpha-by-domain.txt
     *  This pattern is auto-generated by frameworks/ex/common/tools/make-iana-tld-pattern.py
     */
    public static final String TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL =
        "(?:"
        + "(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])"
        + "|(?:biz|b[abdefghijmnorstvwyz])"
        + "|(?:cat|com|coop|c[acdfghiklmnoruvxyz])"
        + "|d[ejkmoz]"
        + "|(?:edu|e[cegrstu])"
        + "|f[ijkmor]"
        + "|(?:gov|g[abdefghilmnpqrstuwy])"
        + "|h[kmnrtu]"
        + "|(?:info|int|i[delmnoqrst])"
        + "|(?:jobs|j[emop])"
        + "|k[eghimnprwyz]"
        + "|l[abcikrstuvy]"
        + "|(?:mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])"
        + "|(?:name|net|n[acefgilopruz])"
        + "|(?:org|om)"
        + "|(?:pro|p[aefghklmnrstwy])"
        + "|qa"
        + "|r[eosuw]"
        + "|s[abcdeghijklmnortuvyz]"
        + "|(?:tel|travel|t[cdfghjklmnoprtvwz])"
        + "|u[agksyz]"
        + "|v[aceginu]"
        + "|w[fs]"
        + "|(?:\u03b4\u03bf\u03ba\u03b9\u03bc\u03ae|\u0438\u0441\u043f\u044b\u0442\u0430\u043d\u0438\u0435|\u0440\u0444|\u0441\u0440\u0431|\u05d8\u05e2\u05e1\u05d8|\u0622\u0632\u0645\u0627\u06cc\u0634\u06cc|\u0625\u062e\u062a\u0628\u0627\u0631|\u0627\u0644\u0627\u0631\u062f\u0646|\u0627\u0644\u062c\u0632\u0627\u0626\u0631|\u0627\u0644\u0633\u0639\u0648\u062f\u064a\u0629|\u0627\u0644\u0645\u063a\u0631\u0628|\u0627\u0645\u0627\u0631\u0627\u062a|\u0628\u06be\u0627\u0631\u062a|\u062a\u0648\u0646\u0633|\u0633\u0648\u0631\u064a\u0629|\u0641\u0644\u0633\u0637\u064a\u0646|\u0642\u0637\u0631|\u0645\u0635\u0631|\u092a\u0930\u0940\u0915\u094d\u0937\u093e|\u092d\u093e\u0930\u0924|\u09ad\u09be\u09b0\u09a4|\u0a2d\u0a3e\u0a30\u0a24|\u0aad\u0abe\u0ab0\u0aa4|\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe|\u0b87\u0bb2\u0b99\u0bcd\u0b95\u0bc8|\u0b9a\u0bbf\u0b99\u0bcd\u0b95\u0baa\u0bcd\u0baa\u0bc2\u0bb0\u0bcd|\u0baa\u0bb0\u0bbf\u0b9f\u0bcd\u0b9a\u0bc8|\u0c2d\u0c3e\u0c30\u0c24\u0c4d|\u0dbd\u0d82\u0d9a\u0dcf|\u0e44\u0e17\u0e22|\u30c6\u30b9\u30c8|\u4e2d\u56fd|\u4e2d\u570b|\u53f0\u6e7e|\u53f0\u7063|\u65b0\u52a0\u5761|\u6d4b\u8bd5|\u6e2c\u8a66|\u9999\u6e2f|\ud14c\uc2a4\ud2b8|\ud55c\uad6d|xn\\-\\-0zwm56d|xn\\-\\-11b5bs3a9aj6g|xn\\-\\-3e0b707e|xn\\-\\-45brj9c|xn\\-\\-80akhbyknj4f|xn\\-\\-90a3ac|xn\\-\\-9t4b11yi5a|xn\\-\\-clchc0ea0b2g2a9gcd|xn\\-\\-deba0ad|xn\\-\\-fiqs8s|xn\\-\\-fiqz9s|xn\\-\\-fpcrj9c3d|xn\\-\\-fzc2c9e2c|xn\\-\\-g6w251d|xn\\-\\-gecrj9c|xn\\-\\-h2brj9c|xn\\-\\-hgbk6aj7f53bba|xn\\-\\-hlcj6aya9esc7a|xn\\-\\-j6w193g|xn\\-\\-jxalpdlp|xn\\-\\-kgbechtv|xn\\-\\-kprw13d|xn\\-\\-kpry57d|xn\\-\\-lgbbat1ad8j|xn\\-\\-mgbaam7a8h|xn\\-\\-mgbayh7gpa|xn\\-\\-mgbbh1a71e|xn\\-\\-mgbc0a9azcg|xn\\-\\-mgberp4a5d4ar|xn\\-\\-o3cw4h|xn\\-\\-ogbpf8fl|xn\\-\\-p1ai|xn\\-\\-pgbs0dh|xn\\-\\-s9brj9c|xn\\-\\-wgbh1c|xn\\-\\-wgbl6a|xn\\-\\-xkc2al3hye2a|xn\\-\\-xkc2dl3a5ee0h|xn\\-\\-yfro4i67o|xn\\-\\-ygbi2ammx|xn\\-\\-zckzah|xxx)"
        + "|y[et]"
        + "|z[amw]))";

    /**
     * Good characters for Internationalized Resource Identifiers (IRI).
     * This comprises most common used Unicode characters allowed in IRI
     * as detailed in RFC 3987.
     * Specifically, those two byte Unicode characters are not included.
     */
    public static final String GOOD_IRI_CHAR =
        "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    /**
     *  Regular expression pattern to match most part of RFC 3987
     *  Internationalized URLs, aka IRIs.  Commonly used Unicode characters are
     *  added.
     */
    public static final Pattern WEB_URL = Pattern.compile(
        "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
        + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
        + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
        + "((?:(?:[" + GOOD_IRI_CHAR + "][" + GOOD_IRI_CHAR + "\\-]{0,64}\\.)+"   // named host
        + TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL
        + "|(?:(?:25[0-5]|2[0-4]" // or ip address
        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
        + "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
        + "|[1-9][0-9]|[0-9])))"
        + "(?:\\:\\d{1,5})?)" // plus option port number
        + "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
        + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
        + "(?:\\b|$)"); // and finally, a word boundary or end of
                        // input.  This is to stop foo.sure from
                        // matching as foo.su
    /* @formatter:on */
}
