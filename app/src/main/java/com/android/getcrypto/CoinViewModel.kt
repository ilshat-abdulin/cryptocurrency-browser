package com.android.getcrypto

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.getcrypto.api.ApiFactory
import com.android.getcrypto.database.AppDatabase
import com.android.getcrypto.pojo.CoinPriceInfo
import com.android.getcrypto.pojo.CoinPriceInfoRawData
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CoinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    val priceList = db.coinPriceInfoDao().getPriceList()

    fun loadData() {
        val disposable = ApiFactory.apiService.getTopCoinsInfo(limit = 100)
                .map { it.data?.map { it.coinInfo?.name }?.joinToString(",") }
                .flatMap { ApiFactory.apiService.getFullPriceList(fSyms = it) }
                .map { getPriceListFromRawData(it) }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    db.coinPriceInfoDao().insertPriceList(it)
                    Log.d("test", "Success: $it")
                }, {
                    Log.d("test", "Failure: ${it.message}")
                })
    }

    private fun getPriceListFromRawData(
            coinPriceInfoRawData: CoinPriceInfoRawData): List<CoinPriceInfo> {
        val result = ArrayList<CoinPriceInfo>()
        val jsonObject = coinPriceInfoRawData.CoinPriceInfoJsonObject
        if (jsonObject == null) {
            return result
        }
        val coinKeySet = jsonObject.keySet()
        for (coinKey in coinKeySet) {
            val currencyJson = jsonObject.getAsJsonObject(coinKey)
            val currencyKeySet = currencyJson.keySet()
            for (currencyKey in currencyKeySet) {
                val priceInfo = Gson().fromJson(currencyJson.getAsJsonObject(currencyKey), CoinPriceInfo::class.java)
                result.add(priceInfo)
            }
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}