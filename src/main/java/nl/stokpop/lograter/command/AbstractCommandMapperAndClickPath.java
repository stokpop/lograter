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
package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;

public abstract class AbstractCommandMapperAndClickPath extends AbstractCommandBasic {

    @Parameter(names = {"-clickpath" },
        description = "Determine and report click paths (BETA). Set sessionfield for the session id to use.")
    public boolean determineClickpaths = false;

    @Parameter(names = { "--clickpath-report-step-duration" },
        description = "Report the average duration between clickpath steps in millis.")
    public boolean clickpathReportStepDurations = false;

    @Parameter(names = { "--clickpath-end-of-session-snippet" },
        description = "Url's that contain this snippet are used as end of session marker (default: logout)")
    public String clickpathEndOfSessionSnippet = "logout";

    @Parameter(names = {"-session-duration" },
        description = "Determine the average session duration. Set sessionfield for the session id to use.")
    public boolean determineSessionDuration = false;

    @Parameter(names = {"-sessionfield" },
        description = "Name of the session field to use for clickpath and session duration analysis, from logpattern.")
    public String sessionField = null;

    @Parameter(names = { "-mf", "--mapper-file" },
        description = "Mapper file to use. Also used in clickpath analysis.")
    public String mapperFile;

    @Parameter(names = { "-single-mapper" },
        description = "Use single mapper for all counters. Mapper file is ignored.")
    public boolean useSingleMapper = false;

    @Parameter(names = { "-sessionfield-regexp" },
        description = "Regexp to use to get the sessionId from the sessionField. Use a capture group () to specify the sessionId capture.")
    public String sessionFieldRegexp;

    @Parameter(names = { "--clickpath-short-code-length" },
        description = "Length of parts between slashes in clickpath urls, to shorten the path.")
    public int clickPathShortCodeLength = 3;

    @Parameter(names = { "-regexp", "--include-mapper-regexp-column" },
        description = "Include the mapper regexp column in text report.")
    public boolean includeMapperRegexpColumn = false;

    @Override
    public String toString() {
        return "AbstractCommandMapperAndClickPath{" +
            "determineClickpaths=" + determineClickpaths +
            ", clickpathReportStepDurations=" + clickpathReportStepDurations +
            ", clickpathEndOfSessionSnippet='" + clickpathEndOfSessionSnippet + '\'' +
            ", determineSessionDuration=" + determineSessionDuration +
            ", sessionField='" + sessionField + '\'' +
            ", mapperFile='" + mapperFile + '\'' +
            ", useSingleMapper=" + useSingleMapper +
            ", sessionFieldRegexp='" + sessionFieldRegexp + '\'' +
            ", clickPathShortCodeLength=" + clickPathShortCodeLength +
            ", includeMapperRegexpColumn=" + includeMapperRegexpColumn +
            "} " + super.toString();
    }
}
