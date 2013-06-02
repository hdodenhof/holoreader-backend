package de.hdodenhof.holoreader.backend.parser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class OPML {
    public static Map<String, String> parse(InputStream inputStream) {
        Map<String, String> results = new HashMap<String, String>();
        boolean isOpml = false;
        boolean isBody = false;
        String url = null;
        String name = null;

        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = parserFactory.newPullParser();
            pullParser.setInput(inputStream, null);

            int eventType = pullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String currentTag = pullParser.getName();

                    if (currentTag.equalsIgnoreCase("opml")) {
                        isOpml = true;
                    } else if (isOpml && currentTag.equalsIgnoreCase("body")) {
                        isBody = true;
                    } else if (isBody && currentTag.equalsIgnoreCase("outline")) {
                        String type = pullParser.getAttributeValue(null, "type");
                        if (type != null && type.equalsIgnoreCase("rss")) {
                            url = pullParser.getAttributeValue(null, "xmlUrl");
                            name = pullParser.getAttributeValue(null, "text");
                            if (name == null) {
                                name = pullParser.getAttributeValue(null, "title");
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String currentTag = pullParser.getName();

                    if (currentTag.equalsIgnoreCase("body")) {
                        isBody = false;
                    } else if (currentTag.equalsIgnoreCase("opml")) {
                        isOpml = false;
                    }
                }

                if (url == null) {
                    // TODO
                } else if (name == null) {
                    name = url;
                }

                results.put(url, name);
                eventType = pullParser.next();

                url = null;
                name = null;
            }

            inputStream.close();
        } catch (Exception e) {
            // TODO
        }

        return results;
    }
}
