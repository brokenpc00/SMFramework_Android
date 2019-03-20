package com.interpark.smframework.network;

import android.service.autofill.RegexValidator;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SNIMatcher;

public class Uri {

    public Uri() {

    }

    public Uri(final Uri o) {
        set(o);
    }

    public void move(Uri o) {
        set(o);
        o.clear();
    }

    public void set(final Uri o) {
        _isValid = o._isValid;
        _isSecure = o._isSecure;
        _scheme = o._scheme;
        _username = o._username;
        _password = o._password;
        _host = o._host;
        _hostName = o._hostName;
        _hasAuthority = o._hasAuthority;
        _port = o._port;
        _authority = o._authority;
        _pathEtc = o._pathEtc;
        _path = o._path;
        _query = o._query;
        _fragment = o._fragment;
        _queryParams = o._queryParams;
    }

    public boolean equals(final Uri o) {
        return (
                _isValid==o._isValid
                && _isSecure==o._isSecure
                && _scheme.equals(o._scheme)
                && _username.equals(o._username)
                && _password.equals(o._password)
                && _host.equals(o._host)
                && _hostName.equals(o._hostName)
                && _hasAuthority==o._hasAuthority
                && _port==o._port
                && _authority.equals(o._authority)
                && _pathEtc.equals(o._pathEtc)
                && _path.equals(o._path)
                && _query.equals(o._query)
                && _fragment.equals(o._fragment)
                && _queryParams.equals(o._queryParams)
                );
    }

    public boolean isValid() {return _isValid;}

    public boolean isSecure() {return _isSecure;}

    public String getScheme() {return _scheme;}

    public String getUserName() {return _username;}

    public String getPassword() {return _password;}

    public String getHost() {return _host;}

    public String getHostName() {return _hostName;}

    public short getPort() {return _port;}

    public String getPath() {return _path;}

    public String getPathEtc() {return _pathEtc;}

    public String getQuery() {return _query;}

    public String getFragment() {return _fragment;}

    public String getAuthority() {return _authority;}

    public String toString() {
        StringBuffer ss = new StringBuffer();

        if (_hasAuthority) {
            ss.append(_scheme).append("://");
            if (!_password.isEmpty()) {
                ss.append(_username).append(":").append(_password).append("@");
            } else if (!_username.isEmpty()) {
                ss.append(_username).append("@");
            }

            ss.append(_host);
            if (_port!=0) {
                ss.append(":").append(_port);
            }
        } else {
            ss.append(_scheme).append(":");
        }
        ss.append(_path);
        if (!_query.isEmpty()) {
            ss.append("?").append(_query);
        }
        if (!_fragment.isEmpty()) {
            ss.append("#").append(_fragment);
        }

        return ss.toString();
    }

    private static String queryParamRegexString1 = "(^|&)";
    private static String queryParamRegexString2 = "([^=&]*)=?";
    private static String queryParamRegexString3 = "([^=&]*)";
    private static String queryParamRegexString4 = "(?=(&|$))";

    private static Pattern queryParamRegex = Pattern.compile(queryParamRegexString1+queryParamRegexString2+queryParamRegexString3+queryParamRegexString4);

    public ArrayList<Pair<String, String>> getQueryParams() {
        if (!_query.isEmpty() && _queryParams.isEmpty()) {

            Matcher m = queryParamRegex.matcher(_query);
            while (m.find()) {
                if (_queryParams==null) {
                    _queryParams = new ArrayList<>();
                }
                _queryParams.add(new Pair<String, String>(m.group(2), m.group(3)));
            }
        }

        return _queryParams;
    }

    public void clear() {
        _isValid = false;
        _isSecure = false;
        _scheme = "";
        _username = "";
        _password = "";
        _host = "";
        _hostName = "";
        _hasAuthority = false;
        _port = 0;
        _authority = "";
        _pathEtc = "";
        _path = "";
        _query = "";
        _fragment = "";
        _queryParams.clear();
    }

    public static Uri parse(final String str) {
        Uri uri = new Uri();

        if (!uri.doParse(str)) {
            uri.clear();
        }

        return uri;
    }

    private static String schemRegex = "([a-zA-Z][a-zA-Z0-9+.-]*):";
    private static String authoRegex = "([^?#]*)";
    private static String queryRegex = "(?:\\?([^#]*))?";
    private static String fragRegex = "(?:#(.*))?";

    private static String pathRegex = "//([^/]*)(/.*)?";

    private static String authNamePwd = "(?:([^@:]*)(?::([^@]*))?@)?";
    private static String authHost = "(\\[[^\\]]*\\]|[^\\[:]*)";
    private static String authPort = "(?::(\\d*))?";

    private static Pattern uriP = Pattern.compile(schemRegex+authoRegex+queryRegex+fragRegex);
    private static Pattern authoPathP = Pattern.compile(pathRegex);
    private static Pattern authorityP = Pattern.compile(authNamePwd+authHost+authPort);

    private boolean doParse(final String str) {
        if (str.isEmpty()) {
            return false;
        }


        boolean hasScheme = true;
        String copied = str;

        if (copied.indexOf("://")<0) {
            hasScheme = false;
            copied = "abc://" + copied;
        }

        Matcher uriM = uriP.matcher(copied);

        if (!uriM.find()) {
            return false;
        }

        if (hasScheme) {
            _scheme = uriM.group(1);
            _scheme = _scheme.toLowerCase();
            if (_scheme.equals("https") || _scheme.equals("wss")) {
                _isSecure = true;
            }
        }

        String authorityAndPath = uriM.group(2);
        Matcher pathM = authoPathP.matcher(authorityAndPath);
        if (!pathM.find()) {
            _hasAuthority = false;
            _path = authorityAndPath;
        } else {
            String authority = pathM.group(1);
            Matcher authorityMatch = authorityP.matcher(authority);
            if (!authorityMatch.find()) {
                return false;
            }

            String port = authorityMatch.group(4);
            if (!port.isEmpty()) {
                _port = Short.valueOf(port);
            }

            _hasAuthority = true;
            _username = authorityMatch.group(1);
            _password = authorityMatch.group(2);
            _host = authorityMatch.group(3);
            _path = pathM.group(2);
        }

        _query = uriM.group(3);
        _fragment = uriM.group(4);
        _isValid = true;

        if (!getUserName().isEmpty() || !getPassword().isEmpty()) {
            _authority += getUserName();

            if (!getPassword().isEmpty()) {
                _authority += ":" + getPassword();
            }

            _authority += "@";
        }

        _authority += getHost();

        if (getPort()!=0) {
            _authority += ":" + String.valueOf(getPort());
        }

        _pathEtc = _path;

        if (!_query.isEmpty()) {
            _pathEtc += "?" + _query;
        }

        if (!_fragment.isEmpty()) {
            _pathEtc += "#" + _fragment;
        }

        if (!_host.isEmpty() && _host.substring(0, 1).equals("[")) {
            _hostName = _host.substring(1, _host.length() - 2);
        } else {
            _hostName = _host;
        }

        return true;
    }


    private boolean _isValid = false;
    private boolean _isSecure = false;
    private String _scheme = "";
    private String _username = "";
    private String _password = "";
    private String _host = "";
    private String _hostName = "";
    private boolean _hasAuthority = false;
    private short _port = 0;
    private String _authority = "";
    private String _pathEtc = "";
    private String _path = "";
    private String _query = "";
    private String _fragment = "";
    private ArrayList<Pair<String, String>> _queryParams = null;
}
