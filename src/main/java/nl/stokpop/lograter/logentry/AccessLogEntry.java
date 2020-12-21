/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.counter.HttpMethod;
import nl.stokpop.lograter.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  AccessLogEntry extends LogEntry {

	private static final Logger log = LoggerFactory.getLogger(AccessLogEntry.class);

    /**
     * null is also allowed for the same behaviour: no splitting is done, all is root url
     */
    public static final UrlSplitter URL_SPLITTER_NOOP = url -> new RootUrlAndParameters(url, "");

    /**
     * a splitter for question mark splitting
     */
    public static final UrlSplitter URL_SPLITTER_DEFAULT = url -> {
        String urlPostFixRoot;
        String urlPostFixParams;
        int questionMarkIndex = url.indexOf('?');

        if (questionMarkIndex != -1) {
            urlPostFixRoot =  url.substring(0, questionMarkIndex);
            urlPostFixParams = url.substring(questionMarkIndex - 1);
        }
        else {
            urlPostFixRoot = url;
            urlPostFixParams = "";
        }
        return new RootUrlAndParameters(urlPostFixRoot, urlPostFixParams);
    };

    private String url;
	private int httpStatus;
	private HttpMethod httpMethod;
	private int durationInMillis;
	private String referrer;
	private String userAgent;
	private String sessionId;
    private String serverName;
    private String clientIP;

	private int bytes;
    private String version;
    private String remoteHost;
    private String remoteUser;
    private long durationInMicros;
    private String remoteLogname;

    protected static void parseRequest(String request, AccessLogEntry e) {
        parseRequest(request, e, null);
    }

    protected static void parseRequest(String request, AccessLogEntry e, UrlSplitter urlSplitter) {
		String[] values = request.split(" ");

		if (values.length == 3) {
			// this is expected
			String httpMethodValue = values[0];
			HttpMethod httpMethod = parseHttpMethod(request, httpMethodValue);
			e.setHttpMethod(httpMethod);

			String urlValue = values[1];
			String url = urlSplitter != null ? urlSplitter.split(urlValue).getRootUrl() : urlValue;
			e.setUrl(url);

			String versionValue = values[2];
			e.setVersion(versionValue);
		}
		else {
			log.debug("This %r parameter does not contain the expected three values [{}]", request);
			e.setHttpMethod(HttpMethod.UNKNOWN);
			String trimmedValue = request.trim();
			String url = urlSplitter != null ? urlSplitter.split(trimmedValue).getRootUrl() : trimmedValue;
			e.setUrl(url);
			e.setVersion("Unknown");
		}
	}

	private static HttpMethod parseHttpMethod(String value, String httpMethodValue) {
		HttpMethod httpMethod;// empty or unknown values are possible in apache http log?
		if (httpMethodValue.trim().length() == 0 || "-".equals(httpMethodValue)) {
			httpMethod = HttpMethod.NONE;
		}
		else {
			try {
				httpMethod = HttpMethod.valueOf(httpMethodValue);
			} catch (IllegalArgumentException exception) {
				log.debug("Unknown value found for http method in [{}]", value);
				httpMethod = HttpMethod.UNKNOWN;
			}
		}
		return httpMethod;
	}

    final void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}
	
	public final String getSessionId() {
		return sessionId;
	}

	public final String getUrl() {
		return url;
	}

	public final int getHttpStatus() {
		return httpStatus;
	}

	public final HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public final int getDurationInMillis() {
		return durationInMillis;
	}

	public final String getReferrer() {
		return referrer;
	}

	public final String getUserAgent() {
		return userAgent;
	}

	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public final void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public final void setDurationInMillis(int durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	public final void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public final void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isHttpError() {
		return HttpUtil.isHttpError(httpStatus);
	}

    @Override
    public String toString() {
        return "AccessLogEntry{" +
                "durationInMillis=" + durationInMillis +
                ", httpMethod=" + httpMethod +
                ", httpStatus=" + httpStatus +
                ", referrer='" + referrer + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", url='" + url + '\'' +
                ", userAgent='" + userAgent + '\'' +
                "} " + super.toString();
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public int getBytes() {
        return bytes;
    }

    public final String getRemoteUser() {
		return remoteUser;
	}

    public String getVersion() {
        return version;
    }

    final void setBytes(int bytes) {
		this.bytes = bytes;
	}

    public final long getDurationInMicros() {
		return durationInMicros;
	}

    public String getRemoteHost() {
        return remoteHost;
    }

    final void setVersion(String version) {
		this.version = version;
	}

    final void setDurationInMicros(long durationInMicros) {
		this.durationInMicros = durationInMicros;
	}

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public final String getRemoteLogname() {
		return remoteLogname;
	}

    final void setRemoteLogname(String remoteLogname) {
		this.remoteLogname = remoteLogname;
	}

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

}