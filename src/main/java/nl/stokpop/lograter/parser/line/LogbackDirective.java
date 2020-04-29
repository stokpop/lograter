/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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
package nl.stokpop.lograter.parser.line;

import nl.stokpop.lograter.LogRaterException;

/**
 * Represents a LogBack directive, such as %m or %msg.
 *
 * The directive is the X in %X{Session}
 * The variable is Session in %X{Session}
 */
public class LogbackDirective implements LogbackElement {
	
	private String directive;
	private String variable;

	private LogbackDirective(String directive) {
		this(directive, null);
	}

    private LogbackDirective(String directive, String variable) {
		this.directive = directive;
        this.variable = variable;
	}

    /**
     * Factory instance pattern to create new objects.
     * @param directive only the directive, e.g. X
     * @return a new LogbackDirective. The variable (e.g. userid) still needs to be set if needed.
     */
    public static LogbackDirective from(String directive) {
        return new LogbackDirective(directive, null);

    }

    /**
     * Factory instance pattern to create new objects.
     * @param directive only the directive, e.g. X
     * @param variable variable of the directive, such as userid in %X{userid}
     * @return a new LogbackDirective. The variable (e.g. userid) still needs to be set if needed.
     */
    public static LogbackDirective from(String directive, String variable) {
        if (directive == null) {
            throw new LogRaterException("Directive cannot be null.");
        }
        if (directive.length() == 0) {
            throw new LogRaterException("Directive cannot be empty. Is % followed by a directive name? Variable: " + variable);
        }
        return new LogbackDirective(directive, variable);
    }

	@Override
	public String toString() {
		return "LogbackDirective [" + getDirective() + ", variable=" + getVariable() + "]";
	}

    public String getDirective() {
        return directive;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

}
