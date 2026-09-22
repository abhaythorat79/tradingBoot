package com.abhi.algotrading.marketdata;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvHistoricalDataServiceTest {

    private final CsvHistoricalDataService service =
            new CsvHistoricalDataService();

    @Test
    void shouldLoadHistoricalCandlesFromCsv()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "historical-data",
                        ".csv"
                );

        Files.writeString(
                file,
                """
                timestamp,symbol,open,high,low,close,volume
                2026-09-21 09:15:00,NIFTY,25000,25020,24990,25010,100000
                2026-09-21 09:16:00,NIFTY,25010,25030,25000,25025,120000
                2026-09-21 09:17:00,NIFTY,25025,25040,25015,25035,150000
                """
        );

        List<Candle> candles =
                service.load(file);

        assertEquals(3, candles.size());

        Candle first =
                candles.getFirst();

        assertEquals(
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        9,
                        15
                ),
                first.timestamp()
        );

        assertEquals("NIFTY", first.symbol());

        assertEquals(
                0,
                first.open().compareTo(
                        new BigDecimal("25000")
                )
        );

        assertEquals(
                0,
                first.high().compareTo(
                        new BigDecimal("25020")
                )
        );

        assertEquals(
                0,
                first.low().compareTo(
                        new BigDecimal("24990")
                )
        );

        assertEquals(
                0,
                first.close().compareTo(
                        new BigDecimal("25010")
                )
        );

        assertEquals(100000, first.volume());
        Files.deleteIfExists(file);
    }

    @Test
    void shouldSkipBlankLines()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "historical-data",
                        ".csv"
                );

        Files.writeString(
                file,
                """
                timestamp,symbol,open,high,low,close,volume
                2026-09-21 09:15:00,NIFTY,25000,25020,24990,25010,100000

                2026-09-21 09:16:00,NIFTY,25010,25030,25000,25025,120000
                """
        );

        List<Candle> candles =
                service.load(file);

        assertEquals(2, candles.size());

        Files.deleteIfExists(file);
    }

    @Test
    void shouldRejectInvalidColumnCount()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "historical-data",
                        ".csv"
                );

        Files.writeString(
                file,
                """
                timestamp,symbol,open,high,low,close,volume
                2026-09-21 09:15:00,NIFTY,25000,25020,24990
                """
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.load(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Expected 7 columns")
        );

        Files.deleteIfExists(file);
    }

    @Test
    void shouldRejectInvalidTimestamp()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "historical-data",
                        ".csv"
                );

        Files.writeString(
                file,
                """
                timestamp,symbol,open,high,low,close,volume
                invalid,NIFTY,25000,25020,24990,25010,100000
                """
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.load(file)
        );

        Files.deleteIfExists(file);
    }

    @Test
    void shouldRejectMissingFile() {

        Path file =
                Path.of(
                        "does-not-exist.csv"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.load(file)
        );
    }
}