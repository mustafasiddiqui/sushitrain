package com.example.sushitrain.scanner;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrayScannerImplTest {

    String testPath = "src/test/resources";
    DateFormat hourDateFormat = new SimpleDateFormat("HH:mm");


    @Test
    void noTrays() throws IOException {
        File file = new File(testPath, "count_dataset_30.csv");
        TrayScannerImpl scanner = new TrayScannerImpl();
        assertEquals(0, scanner.scanTrays(file));
    }

    @Test
    void morningRunWithMissedScans() throws IOException {
        File file = new File(testPath, "count_dataset_370.csv");
        TrayScannerImpl scanner = new TrayScannerImpl();
        assertEquals(32, scanner.scanTrays(file));
    }

    @Test
    void allDayRun() throws IOException {
        File file = new File(testPath, "count_dataset_482.csv");
        TrayScannerImpl scanner = new TrayScannerImpl();
        assertEquals(2, scanner.scanTrays(file));
    }

    @Test
    void fullRun() throws IOException {
        File file = new File(testPath, "count_dataset_full.csv");
        TrayScannerImpl scanner = new TrayScannerImpl();
        assertEquals(0, scanner.scanTrays(file));
    }


    @Test
    void incrementEventStoreTests() {
        Map<Date, TrayEvent> eventStore = new HashMap<>();
        TrayScannerImpl scanner = new TrayScannerImpl();
        Date now = new Date();

        scanner.incrementEventStore(eventStore, now, 5, 2);
        assertEquals(new TrayEvent(now, 5, 2), eventStore.get(now));

        scanner.incrementEventStore(eventStore, now, 1, 0);
        assertEquals(new TrayEvent(now, 6, 2), eventStore.get(now));

        scanner.incrementEventStore(eventStore, now, 0, 1);
        assertEquals(new TrayEvent(now, 6, 3), eventStore.get(now));

        scanner.incrementEventStore(eventStore, now, 2, 2);
        assertEquals(new TrayEvent(now, 8, 5), eventStore.get(now));
    }

    @Test
    void adjustForMissingScansOutTest() throws ParseException {

        TrayScannerImpl scanner = new TrayScannerImpl();
        Map<Date, TrayEvent> eventStore = new HashMap<>();
        insertEntry(eventStore, "05:45", 1, 0);
        insertEntry(eventStore, "06:30", 1, 0);
        insertEntry(eventStore, "06:45", 3, 0);

        insertEntry(eventStore, "09:45", 4, 2);

        Date startTime = hourDateFormat.parse("06:45");
        Date endTime = hourDateFormat.parse("09:45");
        assertEquals(3, scanner.adjustForMissingScansOut(eventStore, startTime, endTime));

        assertEquals(0, scanner.adjustForMissingScansOut(eventStore, startTime, endTime));
    }

    private void insertEntry(Map<Date, TrayEvent> eventStore, String scanTime, int scansIn, int scansOut) throws ParseException {
        Date key = hourDateFormat.parse(scanTime);
        eventStore.put(key, new TrayEvent(key, scansIn, scansOut));
    }

}