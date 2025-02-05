/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.run;

import nl.stokpop.lograter.util.DatabaseBootstrapTest;
import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class ApplicationLogRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testApplicationLog() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                //"-debug",
                "application",
                "-lp",
                "%d;[%t] [%p] [%marker] [cid=%X{cid}] [Service=%X{service}] [Portlet=%X{portletName} %X{portletId}] %c - %m%n %xEx{short}",
                "src/test/resources/application-log/default/application.log" };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

        assertTrue("LogRater application log run outputs a duration.", result.contains("Duration"));
    }

	@Test
	public void testSimpleLogPatternApplicationLog() throws Exception {
		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				//"-debug",
				"application",
				"-lp",
				"%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [reqId=%X{reqId}] [sessionId=%X{sessionId}] %logger{36} - %msg%n",
				"src/test/resources/application-log/application-log-simple-logpattern.log" };

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

		// check if all loggers are reported
		assertTrue("check if logger is present", result.contains("n.v.b.t.s.vla.VlaHelper"));
		assertTrue("check if logger is present", result.contains("n.v.v.t.t.d.s.a.VlaServiceImpl"));
		assertTrue("check if logger is present", result.contains("n.v.v.t.g.b.r.f.VlaReloadingStrategy"));
		assertTrue("check if logger is present", result.contains("n.v.http.HttpClientFactory"));
		assertTrue("check if logger is present", result.contains("org.quartz.core.JobRunShell"));
		assertTrue("check if logger is present", result.contains("org.quartz.core.ErrorLogger"));
	}
	
	@Test
	public void testApplicationLogDb() throws Exception {

        DatabaseBootstrapTest.injectTestDatabasePathIntoSysVars();
        
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				//"-debug",
				"-storage",
				"database",
				"-clear-db",
				"application",
				"-lp",
				"%d;[%t] [%p] [%marker] [cid=%X{cid}] [Service=%X{service}] [App=%X{appName} %X{appId}] %c - %m%n %xEx{short}",
				"src/test/resources/application-log/default/application.log" };

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

		assertTrue("LogRater application log run outputs a duration.", result.contains("Duration"));
	}

	@Test
	public void testApplicationLogWithFullLogbackVariables() throws Exception {
		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				//"-debug",
				"application",
				"-lp",
				"%date;%thread;%level;%X;%class;%msg;%xEx{full}%n",
				"src/test/resources/application-log/default/application-full-variables.log" };

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

		assertTrue("LogRater application log run outputs a duration.", result.contains("Duration"));
	}

	@Test
	public void testApplicationLogWithNonLogLines() throws Exception {

		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				//"-debug",
				"application",
				"-lp",
				"%d;%t;%p;%marker;%X{s-id};%X{ALevel};%X{AType};%X{AId};%X{cust};%X{service};%X{qname};%X{qtype};%X{XFID};%c;%m;%X{x-id}%n%xEx{full}",
				"src/test/resources/application-log/non-logline/application-non-logline.log" };

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		//System.out.println(result);
		assertTrue("LogRater application log run outputs a duration.", result.contains("Duration"));
	}
}
