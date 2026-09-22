package com.abhi.algotrading.marketdata;

import java.nio.file.Path;
import java.util.List;

public interface HistoricalDataService {

    List<Candle> load(Path filePath);
}