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
package nl.stokpop.lograter.sar;

import nl.stokpop.lograter.LogRaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ParserState {

    INITIAL {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            if (matchesHeader(line)) {
                logTransition(line, INITIAL, HEADER);
                return HEADER;
            } else {
                logTransition(line, INITIAL, INBETWEEN);
                return INITIAL;
            }
        }
    },
    INBETWEEN {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            if (matchesCpuHeader(line)) {
                logTransition(line, INBETWEEN, CPU_HEADER);
                return CPU_HEADER;
            } else if (matchesSwpHeader(line)) {
                logTransition(line, INBETWEEN, SWP_HEADER);
                return SWP_HEADER;
            } else if (matchesEmptyLine(line)) {
                logTransition(line, INBETWEEN, INBETWEEN);
                return INBETWEEN;
            } else {
                logTransition(line, INBETWEEN, OTHER);
                return OTHER;
            }
        }
    },
    HEADER {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            if (matchesEmptyLine(line)) {
                logTransition(line, HEADER, INBETWEEN);
                return INBETWEEN;
            } else {
                throw new LogRaterException("Unexpected line, empty line expected: " + line);
            }
        }
    },
    CPU_HEADER {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            logTransition(line, CPU_HEADER, CPU_LINE);
            return CPU_LINE;
        }
    },
    CPU_LINE {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            if (matchesEmptyLine(line)) {
                logTransition(line, CPU_LINE, INBETWEEN);
                return INBETWEEN;
            } else {
                logTransition(line, CPU_LINE, CPU_LINE);
                return CPU_LINE;
            }
        }
    },
    OTHER {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            if (matchesEmptyLine(line)) {
                logTransition(line, OTHER, INBETWEEN);
                return INBETWEEN;
            } else {
                logTransition(line, OTHER, OTHER);
                return OTHER;
            }
        }
    },
    END {
        @Override
        public ParserState nextState(ParseContext context) {
            return null;
        }
    },
    SWP_LINE {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            if (matchesEmptyLine(line)) {
                logTransition(line, SWP_LINE, INBETWEEN);
                return INBETWEEN;
            }
            return SWP_LINE;
        }
    },
    SWP_HEADER {
        @Override
        public ParserState nextState(ParseContext context) {
            String line = context.getCurrentLine();
            logTransition(line, SWP_HEADER, SWP_LINE);
            return SWP_LINE;
        }
    };

    private static final Logger log = LoggerFactory.getLogger(ParserState.class);

    private static boolean matchesEmptyLine(String line) {
        return line.trim().length() == 0;
    }

    private static boolean matchesSwpHeader(String line) {
        return line.contains("pswpin");
    }

    private static boolean matchesCpuHeader(String line) {
        return line.contains("CPU") && line.contains("%idle");
    }

    private static boolean matchesHeader(String line) {
        return line.startsWith("Linux");
    }

    private static void logTransition(String line, ParserState currentState, ParserState nextState) {
        log.debug("Transition from [{}] to [{}] on line [{}].", currentState, nextState, line);
    }

    public abstract ParserState nextState(ParseContext context);

}
