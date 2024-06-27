/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.clickpath;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NotThreadSafe
public class ClickPathAnalyserEngine implements ClickPathAnalyser {

    private Logger log = LoggerFactory.getLogger(ClickPathAnalyserEngine.class);

    private final String logOutSnippet;

	private Map<String, ClickPath> openSessions = new HashMap<>();

	private long sessionTimeOutMillis = 30 * 60 * 1000;

    private ClickPathCollector collector;

    public ClickPathAnalyserEngine(ClickPathCollector collector, String logOutSnippet) {
        this.logOutSnippet = logOutSnippet;
        this.collector = collector;
    }

    @Override
    public ClickPathCollector getCollector() {
        return collector;
    }

	@Override
	public void addLineEntry(String sessionId, String url, long timestamp) {
	    // create new session if not exists
	   if (!openSessions.containsKey(sessionId)) {       
	        addNewSession(sessionId, url, timestamp);
	   }
	   else {
	        ClickPath clickpath = openSessions.get(sessionId);
	        
	        // check if latest click was not more than session timeout ago
	        if (timestamp - clickpath.getEndTimestamp() > sessionTimeOutMillis) {
	            // if so, start a new session
	            closeAndStartNewSession(sessionId, url, timestamp);
	        }
	        // if logout is clicked
	        else if (logOutSnippet != null && url.contains(logOutSnippet)) {
	            // add this click and close session
	            addAndCloseSession(sessionId, url, timestamp);
	        }
	        // session is still alive: add click to session
	        else {
	            addToSession(sessionId, url, timestamp);
	        }
	   }
	}
	   
	private void addNewSession(String sessionId, String url, long timestamp) {
	    // create new session and add first click
	    ClickPath clickPath = new ClickPath(timestamp, url, sessionId);
	    openSessions.put(sessionId, clickPath);
	}
	
	private void closeSession(String sessionId) {
	    ClickPath oldSession = openSessions.remove(sessionId);
	    finalizeSession(oldSession);
	}
	
	/* (non-Javadoc)
	 * @see nl.stokpop.lograter.clickpath.ClickPathAnalyser#closeAllRemainingSessions()
	 */
	@Override
	public void closeAllRemainingSessions() {
	    log.info("Closing all remaining open sessions");
	    
	    Set<String> keysToRemove = new HashSet<>(openSessions.keySet());
	    
	    for (String sessionId : keysToRemove) {
	        finalizeSession(openSessions.remove(sessionId));
	    }
	    
	}

	private void addToSession(String sessionId, String url, long timestamp) {
	    ClickPath path = openSessions.get(sessionId);
	    path.addToPath(url, timestamp);
	}

	private void closeAndStartNewSession(String klid, String url, long timestamp) { 
		// begin new session and add new click
	    closeSession(klid);
	    addNewSession(klid, url, timestamp);
	}
	
	private void addAndCloseSession(String klid, String url, long timestamp) { 
	    addToSession(klid, url, timestamp);
	    closeSession(klid);
	}
	
	private void  finalizeOldSessions(long timestamp) {
	    Set<String> sessionsToBeRemoved = new HashSet<>();
	    
	    for (Map.Entry<String, ClickPath> key : openSessions.entrySet()) {
	    	long lastTimestamp = key.getValue().getEndTimestamp();
	        if (timestamp - lastTimestamp > sessionTimeOutMillis) {
	            sessionsToBeRemoved.add(key.getKey());
	        }
	    }
	    
	    for (String key : sessionsToBeRemoved) {    
	        log.debug("Timed out session: {}", key);
	        finalizeSession(openSessions.remove(key));
	    }
	}

	private void finalizeSession(ClickPath clickPath) {
        collector.addClickPath(clickPath);
	}

}
