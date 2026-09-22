package com.abhi.algotrading.marketdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvHistoricalDataService
        implements HistoricalDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<Candle> load(Path filePath) {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException(
                    "Historical data file does not exist: "
                            + filePath
            );
        }

        try {

            List<String> lines =
                    Files.readAllLines(filePath);

            if (lines.isEmpty()) {
                throw new IllegalArgumentException(
                        "Historical data file is empty"
                );
            }

            List<Candle> candles =
                    new ArrayList<>();

            /*
             * Expected header:
             *
             * timestamp,symbol,open,high,low,close,volume
             */

            for (int i = 1; i < lines.size(); i++) {

                String line =
                        lines.get(i).trim();

                if (line.isEmpty()) {
                    continue;
                }

                candles.add(
                        parseLine(line, i + 1)
                );
            }

            return candles;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to read historical data file: "
                            + filePath,
                    e
            );
        }
    }

    private Candle parseLine(
            String line,
            int lineNumber) {

        String[] values =
                line.split(",", -1);

        if (values.length != 7) {

            throw new IllegalArgumentException(
                    "Invalid CSV format at line "
                            + lineNumber
                            + ". Expected 7 columns but found "
                            + values.length
            );
        }

        try {

            LocalDateTime timestamp =
                    LocalDateTime.parse(
                            values[0].trim(),
                            DATE_TIME_FORMATTER
                    );

            String symbol =
                    values[1].trim();

            if (symbol.isBlank()) {
                throw new IllegalArgumentException(
                        "Symbol cannot be empty"
                );
            }

            BigDecimal open =
                    new BigDecimal(
                            values[2].trim()
                    );

            BigDecimal high =
                    new BigDecimal(
                            values[3].trim()
                    );

            BigDecimal low =
                    new BigDecimal(
                            values[4].trim()
                    );

            BigDecimal close =
                    new BigDecimal(
                            values[5].trim()
                    );

            long volume =
                    Long.parseLong(
                            values[6].trim()
                    );

            return new Candle(
                    timestamp,
                    symbol,
                    open,
                    high,
                    low,
                    close,
                    volume
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid candle data at line "
                            + lineNumber
                            + ": "
                            + line,
                    e
            );
        }
    }
}