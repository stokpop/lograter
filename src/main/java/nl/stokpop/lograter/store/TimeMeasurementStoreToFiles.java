/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.store;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.ExternalSort;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is NOT thread safe due to sorting and sorting check.
 */
@NotThreadSafe
public class TimeMeasurementStoreToFiles extends AbstractTimeMeasurementStore {

	private static final Logger log = LoggerFactory.getLogger(TimeMeasurementStoreToFiles.class);

	// check if store names are unique, this is static because the names need to be
	// unique for all TimeMeasurementStoreToFiles objects
	@GuardedBy("itself")
	private static final List<String> storeNames = new ArrayList<>();

	private List<TimeMeasurement> bufferedTimeMeasurements;

	private boolean isOrdered = true;
	private final String storeName;
	private final String counterName;
	private int size = 0;

	private final int buffersize;
	private int fileIndex = 0;
	private boolean isDirty;
	private boolean isLocked;
	private boolean externalSortDone = false;
	private File storeDir;


	public TimeMeasurementStoreToFiles(File rootStorageDir, String storeName, String counterName, int bufferSize) {

		String combinedName = String.join("-", storeName, counterName);
		synchronized (storeNames) {
			if (storeNames.contains(combinedName)) {
				throw new LogRaterException(String.format("Store/counter name already in use, it must be unique. Store name [%s] Counter name [%s].", storeName, combinedName));
			}
			storeNames.add(combinedName);
		}

		this.bufferedTimeMeasurements = createNewTimeMeasurementList();
		this.storeName = storeName;
		this.counterName = counterName;
		this.buffersize = bufferSize;

		this.storeDir = ExternalSort.createTempDir(rootStorageDir, this.storeName, this.counterName);

	}

	@Override
	public void add(long timestamp, int durationInMillis) {
		add(new TimeMeasurement(timestamp, durationInMillis));
	}

	@Override
	public void add(TimeMeasurement timeMeasurement) {
		size++;
		if (isLocked) {
			throw new RuntimeException("This time measurement store has been locked due to reading, cannot add more data after read action.");
		}
		isDirty = true;

		long timestamp = timeMeasurement.getTimestamp();

		// check if stays ordered if already ordered
		if (isOrdered && !bufferedTimeMeasurements.isEmpty()) {
			long lastTimestamp = bufferedTimeMeasurements.get(bufferedTimeMeasurements.size() - 1).getTimestamp();
			isOrdered = lastTimestamp <= timestamp;
		}

		bufferedTimeMeasurements.add(timeMeasurement);

		updateFirstAndLastTimestamps(timestamp);

		if (bufferedTimeMeasurements.size() == buffersize) {
			flushToFile();
		}
	}

	private void flushToFile() {
		int currentSize = bufferedTimeMeasurements.size();
		if (currentSize == 0) {
			log.debug("Nothing to flush for {}", counterName);
			return;
		}
		if (currentSize != buffersize) {
			log.debug("Flushing {} time measurements buffer to new file while the buffer has not reached full buffersize {}",
					currentSize, buffersize);
		}
		log.debug("Flushing {} {} measurements to file.", currentSize, counterName);

		File file = new File(storeDir, ExternalSort.createSerializedFilename(counterName, fileIndex++));
		bufferedTimeMeasurements.sort(TimeMeasurement.ORDER_TIMESTAMP);
		writeToDisk(file, bufferedTimeMeasurements);
		bufferedTimeMeasurements = createNewTimeMeasurementList();
		isDirty = false;
	}

	private List<TimeMeasurement> createNewTimeMeasurementList() {
		// avoid array list growth
		return new ArrayList<>(buffersize * 2);
	}

	private void writeToDisk(File file, List<TimeMeasurement> timeMeasurements) {

		String filePath = file.getAbsolutePath();
		try {
			if (file.exists()) {
				if (file.isDirectory()) {
					throw new LogRaterException("File is a directory: " + filePath);
				}
				if (!file.canWrite()) {
					throw new LogRaterException("File is not writeable: " + filePath);
				}
			}
			else {
				boolean newFileOK = file.createNewFile();
				if (!newFileOK) throw new LogRaterException("File create failed: " + filePath);
			}

			try (
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				DataOutputStream oos = new DataOutputStream(bufferedOutputStream)
			) {
				if (!isOrdered) {
					timeMeasurements.sort(TimeMeasurement.ORDER_TIMESTAMP);
				}
				for (TimeMeasurement tm : timeMeasurements) {
					ExternalSort.writeTimeMeasurement(oos, tm);
				}
			}
		} catch (IOException e) {
			throw new LogRaterException(String.format("Cannot serialize file: %s", filePath), e);
		}
	}

	@Override
	public TimeMeasurementStore getTimeSlice(TimePeriod timePeriod) {
		lockAndWrite();
		return new TimeMeasurementStoreView(timePeriod, this);
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
    public String toString() {
        return "TimeMeasurementStoreInMemory{" +
                "timeMeasurements size=" + size +
                '}';
    }

	@Override
	public TimeMeasurementIterator iterator() {

		// this is a read action that causes the time measurement store to sort and lock
		lockAndWrite();

		final File totalSerializedFile = new File(storeDir, ExternalSort.createTotalSerializedFilename(counterName));

		return new TimeMeasurementIterator() {

			private FileInputStream fis;
			private BufferedInputStream bis;
			private DataInputStream ois;

			TimeMeasurement previous = null;
			boolean isHasNextCalled = false;

			{
				try {
					fis = new FileInputStream(totalSerializedFile);
					bis = new BufferedInputStream(fis, 256 * 1024);
					ois = new DataInputStream(bis);
				} catch (IOException e) {
					throw new RuntimeException("Error reading file: " + totalSerializedFile, e);
				}
			}

			@Override
			public boolean hasNext() {
				isHasNextCalled = true;
				try {
					previous = ExternalSort.readTimeMeasurement(ois);
					return true;
				} catch (EOFException e) {
					previous = null;
					return false;
				} catch (IOException e) {
					throw new RuntimeException("No next TimeMeasurement found due to error.", e);
				}
			}

			@Override
			public TimeMeasurement next() {
				if (isHasNextCalled) {
					return previous;
				}
				else {
					try {
						previous = ExternalSort.readTimeMeasurement(ois);
						isHasNextCalled = false;
					} catch (IOException e) {
						throw new RuntimeException("No next TimeMeasurement found due to error.", e);
					}
					return previous;
				}
			}

			@Override
			public void remove() {
				throw new RuntimeException("Remove is not implemented for file backed TimeMeasurementStore.");
			}

			@Override
			public void close() throws Exception {
				if (ois != null) ois.close();
				if (bis != null) bis.close();
				if (fis != null) fis.close();
			}
		};

	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	private void lockAndWrite() {
		isLocked = true;
		// push out remaining data
		if (isDirty) flushToFile();
		if (!externalSortDone) externalSort();
	}

	/**
	 * Read all files and write one big sorted file to be used as underlying array on disk for iterator.
	 * Based on http://exceptional-code.blogspot.nl/2011/07/external-sorting-for-sorting-large.html
	 */
	private void externalSort() {
		try {
			log.info("Start external sort for {} with size {}", counterName, size);
			ExternalSort.externalMerge(storeDir, counterName, fileIndex, size);
			externalSortDone = true;
		} catch (IOException e) {
			throw new RuntimeException("External merge failed.", e);
		}
	}
}
