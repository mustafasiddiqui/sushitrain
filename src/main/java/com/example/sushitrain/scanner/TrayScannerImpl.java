package com.example.sushitrain.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TrayScannerImpl implements TrayScanner {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long MINUTE_MILLIS = 60 * 1000;

    @Override
    public int scanTrays(String pathString) throws FileNotFoundException {
        Path path = Paths.get(pathString);
        return scanTrays(path.toFile());
    }

    int scanTrays(File input) throws FileNotFoundException {
        Map<Date, TrayEvent> eventStore = new HashMap<>();
        Scanner scanner = new Scanner(input);
        int net, totalIn = 0, totalOut = 0;
        Date beginningDate = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Scanner lineScanner = new Scanner(line).useDelimiter(",");
            String day = lineScanner.next();
            String time = lineScanner.next();
            Date scanDate;

            try {
                scanDate = dateFormat.parse(day + " " + time);
                if (beginningDate == null) {
                    beginningDate = scanDate;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            int traysIn = lineScanner.nextInt();
            int traysOut = lineScanner.nextInt();

            if (scanDate != null) {
                incrementEventStore(eventStore, scanDate, traysIn, traysOut);
                if (traysIn > 0) {
                    totalIn += traysIn;
                }

                if (traysOut > 0) {
                    totalOut += traysOut;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(scanDate);
                boolean doErrorCorrections = cal.get(Calendar.HOUR_OF_DAY) >= 3;

                if (doErrorCorrections) {
                    int scanOutAdjustment = adjustForMissingScansOut(eventStore, scanDate);
                    if (scanOutAdjustment > 0) {
                        totalOut += scanOutAdjustment;
                    }
                }

                net = totalIn - totalOut;

                if (doErrorCorrections && net < 0) {
                    Date entryTime = new Date(scanDate.getTime() - 90 * MINUTE_MILLIS);
                    incrementEventStore(eventStore, entryTime, -net, 0);
                    totalIn += -net;
                }
            }
        }

        net = totalIn - totalOut;
        return net;
    }


    int adjustForMissingScansOut(Map<Date, TrayEvent> eventStore, Date endTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        if (cal.get(Calendar.HOUR_OF_DAY) >= 16) {
            cal.add(Calendar.MINUTE, -90);
        } else {
            cal.add(Calendar.MINUTE, -180);
        }
        return adjustForMissingScansOut(eventStore, cal.getTime(), endTime);
    }

    int adjustForMissingScansOut(Map<Date, TrayEvent> eventStore, Date startTime, Date endTime) {
        AtomicInteger scansIn = new AtomicInteger(),
                scansOut = new AtomicInteger();

        eventStore.entrySet()
                .stream()
                .filter(e -> (e.getKey().compareTo(startTime) <= 0))
                .flatMap(e -> Stream.of(e.getValue()))
                .forEach(event -> {
                    scansIn.addAndGet(event.scannedIn());
                });

        eventStore.entrySet()
                .stream()
                .filter(e -> (e.getKey().compareTo(endTime) <= 0))
                .flatMap(e -> Stream.of(e.getValue()))
                .forEach(event -> {
                    scansOut.addAndGet(event.scannedOut());
                });


        int difference = scansIn.get() - scansOut.get();
        if (difference > 0) {
            incrementEventStore(eventStore, endTime, 0, difference);
            return difference;
        }
        return 0;
    }

    void incrementEventStore(Map<Date, TrayEvent> eventStore, Date key, int scansInIncrement, int scansOutIncrement) {
        if (eventStore != null && key != null && (scansInIncrement > 0 || scansOutIncrement > 0)) {
            TrayEvent currentRecord = eventStore.get(key);
            int currentScansIn = currentRecord == null ? 0 : currentRecord.scannedIn();
            int currentScansOut = currentRecord == null ? 0 : currentRecord.scannedOut();
            eventStore.put(key, new TrayEvent(key, currentScansIn + scansInIncrement, currentScansOut + scansOutIncrement));
        }
    }
}
