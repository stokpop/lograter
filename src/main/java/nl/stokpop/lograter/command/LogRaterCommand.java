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

/**
 * Super class for all LogRaterCommand objects.
 */
public abstract class LogRaterCommand {
	public abstract String getCommandName();

    @Override
    public int hashCode() {
        return getCommandName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LogRaterCommand) {
            return getCommandName().equals(((LogRaterCommand) obj).getCommandName());
        }
        else {
            return false;
        }
    }
}
