package com.interpark.smframework.network.HttpClient;

import com.interpark.smframework.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;

public class HttpCookie {
    public static class CookiesInfo {
        public String domain = "";
        public boolean tailmatch = false;
        public String path = "";
        public boolean secure = false;
        public String name = "";
        public String value = "";
        public String expires = "";
    }

    public HttpCookie() { }

    public void readFile() {
        String inString = FileUtils.getInstance().getStringFromFile(_cookieFileName);

        if (!inString.isEmpty()) {
            ArrayList<String> cookiesList = new ArrayList<>();
            cookiesList.clear();

            Scanner cookieRead = new Scanner(inString);
            cookieRead.useDelimiter("\n");

            while (cookieRead.hasNext()) {
                cookiesList.add(cookieRead.next());
            }


            if (cookiesList.isEmpty()) return;

            for (String cookie : cookiesList) {
                if (cookie.length()==0) continue;

                if (cookie.contains("#HttpOnly_")) {
                    cookie = cookie.substring(10);
                }

                if (cookie.substring(0, 1).equals("#")) {
                    continue;
                }

                CookiesInfo co = new CookiesInfo();
                ArrayList<String> elems = new ArrayList<>();

                Scanner infoRead = new Scanner(cookie);
                infoRead.useDelimiter("\t");

                while (infoRead.hasNext()) {
                    elems.add(infoRead.next());
                }

                co.domain = elems.get(0);

                if (co.domain.substring(0, 1).equals(".")) {
                    co.domain = co.domain.substring(0, 1);
                }

                co.tailmatch = elems.get(1).equals("TRUE");
                co.path = elems.get(2);
                co.secure = elems.get(3).equals("TRUE");
                co.expires = elems.get(4);
                co.name = elems.get(5);
                co.value = elems.get(6);
                _cookies.add(co);
            }
        }
    }

    public void writeFile() {
        File out = new File(_cookieFileName);

        String memo = "# Netscape HTTP Cookie File\n"+
                      "# http://curl.haxx.se/docs/http-cookies.html\n\n";

        StringBuffer sb = new StringBuffer(memo);


        for (CookiesInfo cookie : _cookies) {
            sb.append(cookie.domain);
            sb.append('\t');
            if (cookie.tailmatch) {
                sb.append("TRUE");
            } else {
                sb.append("FALSE");
            }
            sb.append('\t');
            sb.append(cookie.path);
            sb.append('\t');
            if (cookie.secure) {
                sb.append("TRUE");
            } else {
                sb.append("FALSE");
            }
            sb.append('\t');
            sb.append(cookie.expires);
            sb.append('\t');
            sb.append(cookie.name);
            sb.append('\t');
            sb.append(cookie.value);
            sb.append('\n');
        }

        if (out.canWrite()) {
            try {
                FileOutputStream stream = new FileOutputStream(out);
                stream.write(sb.toString().getBytes());
                stream.close();
            } catch (IOException e) {

            }
        }

//        sb.delete(0, sb.length());
        sb.setLength(0);

    }

    public void setCookieFileName(String fileName) {
        _cookieFileName = fileName;
    }

    public final ArrayList<CookiesInfo> getCookies() {
        return _cookies;
    }

    public final CookiesInfo getMatchCookie(final String url) {
        for (CookiesInfo cookie : _cookies) {
            if (url.contains(cookie.domain)) {
                return cookie;
            }
        }

        return null;
    }

    public void updateOrAddCookie(CookiesInfo cookie) {
        for (CookiesInfo _cookie : _cookies) {
            if (cookie.domain.equals(_cookie.domain)) {
                _cookie = cookie;
                return;
            }
        }

        _cookies.add(cookie);
    }

    private String _cookieFileName;
    private ArrayList<CookiesInfo> _cookies = null;
}
