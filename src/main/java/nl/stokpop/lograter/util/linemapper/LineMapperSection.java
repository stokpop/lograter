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
package nl.stokpop.lograter.util.linemapper;

import nl.stokpop.lograter.LogRaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LineMapperSection {
	
	private static final Logger log = LoggerFactory.getLogger(LineMapperSection.class);

	public static final List<LineMapperSection> SINGLE_MAPPER;

	static {
		List<LineMapperSection> single = new ArrayList<>(1);
		LineMapperSection section = new LineMapperSection("Single line mapper");
		section.addMapperRule(".*", "SINGLE-MAPPER");
		single.add(section);
		SINGLE_MAPPER = Collections.unmodifiableList(single);
	}

    private final List<LineMap> lineMaps = new ArrayList<>();

	private final String name;

	public LineMapperSection(String name) {
		this.name = name;
	}

    public void addMapperRule(String regexp, String name) {
        log.debug("Add mapper from {} to {}", regexp, name);
        LineMap accessLogMapper = new LineMap(regexp, name);
        lineMaps.add(accessLogMapper);
    }

        /**
		 * Update lineMaps will do an appropriate callback on the lineMaps that match (also called a hit).
		 * There are three call backs:
		 * - match found, that is called for each match found
		 * - multiple matches found that will tell the total number of matches if more than 1
		 * - no match found
		 *
		 * @see LineMapperCallback
		 */
		public void updateMappers(String line, boolean doCountMultipleMapperHits, LineMapperCallback callback) {
			if (line == null) throw new LogRaterException("updateMappers cannot map a 'null' line.");

			boolean matchFound = false;
			int hits = 0;
	
			Iterator<LineMap> it = lineMaps.iterator();
			while ((!matchFound || doCountMultipleMapperHits) && it.hasNext()) {
				LineMap mapper = it.next();
				matchFound = mapper.isMatch(line);
				if (matchFound) {
					hits++;
					callback.matchFound(mapper);
				}
			}
			if (hits == 0) {
				callback.noMatchFound(line);

			} else if (hits > 1) {
				callback.multiMatchFound(line, hits);
			}
		}

    public String getName() {
        return name;
    }

    public int size() {
        return lineMaps.size();
    }

    @Override
    public String toString() {
        return "LineMapperSection{" + "name='" + name + '\'' + '}';
    }
}
