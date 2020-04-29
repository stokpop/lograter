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
package nl.stokpop.lograter.util;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.store.TimeMeasurement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExternalSort {

	public static String createSerializedFilename(String name, int fileIndex) {
		return FileUtils.replaceNonFileChars(name) + ".timemeasurements." + fileIndex + ".ser";
	}

	public static String createTotalSerializedFilename(String name) {
		return FileUtils.replaceNonFileChars(name) + ".timemeasurements.total.ser";
	}

	public static void externalMerge(File tempDir, String name, int slices, int size) throws IOException {
		// Now open each file and merge them, then write back to disk
		TimeMeasurement[] topNums = new TimeMeasurement[slices];
		DataInputStream[] ois = new DataInputStream[slices];

		for (int i = 0; i < slices; i++) {

			File serializedFile = new File(tempDir, createSerializedFilename(name, i));
			FileInputStream fis = new FileInputStream(serializedFile);
			BufferedInputStream bis = new BufferedInputStream(fis, 256 * 1024);

			ois[i] = new DataInputStream(bis);

			try {
				// unshared to avoid "memory leak": otherwise all objects will be held for future reference
				TimeMeasurement timeMeasurement = readTimeMeasurement(ois[i]);
				topNums[i] = timeMeasurement;
			} catch (EOFException eofe) {
				// seems no non-exception catcher for eof exists...
				topNums[i] = TimeMeasurement.END_OF_TIME;
			}

		}

		File totalSerializedFile = new File(tempDir, createTotalSerializedFilename(name));
		FileOutputStream fos = new FileOutputStream(totalSerializedFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 1024);
		DataOutputStream dos = new DataOutputStream(bos);

		try {

			for (int i = 0; i < size; i++) {
				TimeMeasurement min = topNums[0];
				int minFile = 0;

				for (int j = 0; j < slices; j++) {
					if (min.getTimestamp() > topNums[j].getTimestamp()) {
						min = topNums[j];
						minFile = j;
					}
				}
				writeTimeMeasurement(dos, min);

				try {
					TimeMeasurement timeMeasurement = readTimeMeasurement(ois[minFile]);

					topNums[minFile] = timeMeasurement;
				} catch (EOFException eofe) {
					// seems no non-exception catcher for eof exists...
					topNums[minFile] = TimeMeasurement.END_OF_TIME;
				}
			}
		} finally {
			dos.close();
			bos.close();
			fos.close();
			for (int i = 0; i < slices; i++)
				ois[i].close();
		}
		for (int i = 0; i < slices; i++) {
			File serializedFile = new File(tempDir, createSerializedFilename(name, i));
			boolean deleteOK = serializedFile.delete();
			if (!deleteOK) throw new LogRaterException("Cannot delete file " + serializedFile.getAbsolutePath());
		}
	}

	public static void writeTimeMeasurement(DataOutputStream dos, TimeMeasurement tm) throws IOException {
		dos.writeLong(tm.getTimestamp());
		// long is much more efficient to read/write that an int, see code of DataOutputStream
		dos.writeLong(tm.getDurationInMillis());
	}

	public static TimeMeasurement readTimeMeasurement(DataInputStream dis) throws IOException {
		long timestamp = dis.readLong();
		// long is much more efficient to read/write that an int, see code of DataOutputStream
		int duration = (int) dis.readLong();
		return new TimeMeasurement(timestamp, duration);
	}

	public static File createTempDir(File rootTmpDir, String storeName, String counterName) {
		File tempDir = new File(rootTmpDir, "serialized-time-stores");
		File tempDirStore = new File(tempDir, FileUtils.replaceNonFileChars(storeName));

		if (!tempDirStore.exists()) {
			boolean mkdirsOK = tempDirStore.mkdirs();
			if (!mkdirsOK) throw new LogRaterException("Cannot create dirs " + tempDirStore.getAbsolutePath());
		}

		File tempDirCounter = new File(tempDirStore, FileUtils.replaceNonFileChars(counterName));
		if (tempDirCounter.exists()) {
			FileUtils.deleteDir(tempDirCounter);
		}
		boolean mkdirOK = tempDirCounter.mkdir();
		if (!mkdirOK) throw new LogRaterException("Cannot create dirs: " + tempDirCounter.getAbsolutePath());

		return tempDirCounter;
	}
}