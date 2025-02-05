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

import java.util.Collections;
import java.util.List;

public abstract class AbstractCommandAccessLog extends AbstractCommandMapperAndClickPath {

	@Parameter(names = { "-rpu", "--remove-params-from-url" },
            description = "Remove the parameters before parsing the access log urls (split at question mark)")
	public boolean removeParametersFromUrl = false;
	@Parameter(names = { "-imm", "--ignore-multi-and-no-matches" },
            description = "Ignore multi match and no match warnings. Do not display a list of the no matches.")
	public boolean ignoreMultiAndNoMatches = false;
	@Parameter(names = { "-multi-hit", "--count-multiple-hits-in-mapper" },
            description = "Will count all hits in the mapper file, otherwise only the first hit will be counted.")
	public boolean doCountMultipleMapperHits = false;
    @Parameter(names = { "-count-no-mapper-as-one" },
            description = "Will count all no-mappers as one line, default count all no-mappers separately.")
    public boolean countNoMappersAsOne = false;
	@Parameter(names = { "-group-by-http-status" },
            description = "Group by http status code by adding it to the counter name and adding a http status column in text report.")
	public boolean doGroupByHttpStatus = false;
	@Parameter(names = { "-group-by-http-method" },
            description = "Group by http method (POST, GET, ...) by adding it to the counter name and adding a http method column in text report.")
    public boolean doGroupByHttpMethod = false;
    @Parameter(names = { "-group-by-fields" },
            description = "Group by the given comma separated fields as specified in the used logformat. Url will be the mapped url.")
    public List<String> groupByFields = Collections.emptyList();
	@Parameter(names = { "-ref", "--referers" },
            description = "Include referers in iis and access logs.")
	public boolean showReferers = false;
	@Parameter(names = { "-ua", "--useragents" },
            description = "Include agents in iis and access logs.")
	public boolean showUserAgents = false;
	@Parameter(names = { "-urls" },
            description = "Include basic urls in iis and access logs.")
	public boolean showBasicUrls = false;
	@Parameter(names = { "-nompr", "--nomappers" },
            description = "Exclude mappers in access logs.")
	public boolean excludeMappers = false;

	public AbstractCommandAccessLog() {
		super();
	}

	@Override
	public String toString() {
		return "AbstractCommandAccessLog{" +
			"removeParametersFromUrl=" + removeParametersFromUrl +
			", ignoreMultiAndNoMatches=" + ignoreMultiAndNoMatches +
			", doCountMultipleMapperHits=" + doCountMultipleMapperHits +
			", countNoMappersAsOne=" + countNoMappersAsOne +
			", doGroupByHttpStatus=" + doGroupByHttpStatus +
			", doGroupByHttpMethod=" + doGroupByHttpMethod +
			", doGroupByFields=" + groupByFields +
			", showReferers=" + showReferers +
			", showUserAgents=" + showUserAgents +
			", showBasicUrls=" + showBasicUrls +
			", excludeMappers=" + excludeMappers +
			"} " + super.toString();
	}
}