@HiltViewModel
class ScreenerViewModel @Inject constructor(
    private val binanceRepository: BinanceRepository,
    private val strategyEngine: StrategyEngine,
    private val coinRepository: CoinRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScreenerUiState())
    val uiState: StateFlow<ScreenerUiState> = _uiState.asStateFlow()
    
    private val _signals = MutableStateFlow<List<CoinSignal>>(emptyList())
    val signals: StateFlow<List<CoinSignal>> = _signals.asStateFlow()
    
    private var analysisJob: Job? = null
    
    fun analyzeMarket() {
        analysisJob?.cancel()
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        analysisJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get all coins from repository
                val coins = coinRepository.getWatchlistCoins()
                
                // Analyze each coin
                val results = mutableListOf<CoinSignal>()
                
                coins.forEach { coin ->
                    try {
                        // Fetch candle data for all timeframes
                        val candles15m = binanceRepository.getCandleData(
                            symbol = coin.symbol,
                            interval = "15m",
                            limit = 100
                        )
                        
                        val candles1H = binanceRepository.getCandleData(
                            symbol = coin.symbol,
                            interval = "1h",
                            limit = 100
                        )
                        
                        val candles4H = binanceRepository.getCandleData(
                            symbol = coin.symbol,
                            interval = "4h",
                            limit = 100
                        )
                        
                        // Fetch volume data
                        val volumeData = binanceRepository.getVolumeData(
                            symbol = coin.symbol,
                            interval = "1h",
                            limit = 20
                        )
                        
                        // Run strategy analysis
                        val result = strategyEngine.analyzeCoin(
                            symbol = coin.symbol,
                            candles15m = candles15m,
                            candles1H = candles1H,
                            candles4H = candles4H,
                            volumeData = volumeData
                        )
                        
                        // Only include results with confidence > 70%
                        if (result.confidenceScore >= 70) {
                            results.add(CoinSignal(coin, result))
                        }
                        
                    } catch (e: Exception) {
                        Log.e("ScreenerVM", "Error analyzing ${coin.symbol}: ${e.message}")
                    }
                }
                
                // Sort by confidence score
                val sortedResults = results.sortedByDescending { it.result.confidenceScore }
                
                // Update UI state
                _signals.value = sortedResults
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis(),
                        analyzedCount = coins.size,
                        signalCount = sortedResults.size
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
    
    data class ScreenerUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val lastUpdated: Long = 0,
        val analyzedCount: Int = 0,
        val signalCount: Int = 0
    )
}

data class CoinSignal(
    val coin: Coin,
    val result: AnalysisResult
)