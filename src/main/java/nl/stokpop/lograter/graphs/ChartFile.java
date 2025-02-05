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
package nl.stokpop.lograter.graphs;

import java.io.File;

public class ChartFile {
    private String name;
    private String title;
    private File file;
    private ChartType type;

    public enum ChartType {
        HTML, PNG
    }

    public ChartFile(String name, String title, File file, ChartType type) {
        this.name = name;
        this.title = title;
        this.file = file;
        this.type = type;
    }

    public ChartFile(String name, String title, File file) {
        this(name, title, file, ChartType.PNG);
    }

    public ChartFile(String name, File file) {
        this(name, name, file);
    }

    public String getTitle() {
        return title;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public ChartType getType() {
        return type;
    }

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}


    @Override
    public String toString() {
        return "ChartFile{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", file=" + file +
                ", type=" + type +
                '}';
    }
}
