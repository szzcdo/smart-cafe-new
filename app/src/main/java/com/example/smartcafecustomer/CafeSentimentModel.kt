package com.example.smartcafecustomer

import kotlin.math.exp
import kotlin.math.ln

data class SentimentPrediction(
    val label: String,
    val score: Double
)

class CafeSentimentModel {

    private val positiveSamples = listOf(
        "great coffee hot fresh delicious fast service",
        "lovely cake friendly staff quick order",
        "best latte warm brownie amazing taste",
        "smooth espresso clean cafe easy app"
    )

    private val negativeSamples = listOf(
        "cold coffee slow wait stale cake",
        "burnt espresso bitter taste rude staff",
        "wrong order delayed long queue",
        "dry brownie overpriced disappointing drink"
    )

    private val positiveWordCounts = mutableMapOf<String, Int>()
    private val negativeWordCounts = mutableMapOf<String, Int>()
    private val allWords = mutableSetOf<String>()
    private var totalPositiveWords = 0
    private var totalNegativeWords = 0

    init {
        for (sentence in positiveSamples) {
            for (word in getWords(sentence)) {
                allWords.add(word)
                positiveWordCounts[word] = (positiveWordCounts[word] ?: 0) + 1
                totalPositiveWords++
            }
        }

        for (sentence in negativeSamples) {
            for (word in getWords(sentence)) {
                allWords.add(word)
                negativeWordCounts[word] = (negativeWordCounts[word] ?: 0) + 1
                totalNegativeWords++
            }
        }
    }

    private fun getWords(text: String): List<String> {
        val rawWords = text.lowercase().split(Regex("[^a-z]+"))
        val cleanWords = mutableListOf<String>()
        for (word in rawWords) {
            if (word.length > 1) {
                cleanWords.add(word)
            }
        }
        return cleanWords
    }

    fun predict(text: String): SentimentPrediction? {
        val knownWords = mutableListOf<String>()
        for (word in getWords(text)) {
            if (allWords.contains(word)) {
                knownWords.add(word)
            }
        }

        if (knownWords.isEmpty()) {
            return null
        }

        var positiveScore = ln(0.5)
        var negativeScore = ln(0.5)
        val vocabSize = allWords.size

        for (word in knownWords) {
            val posCount = positiveWordCounts[word] ?: 0
            val negCount = negativeWordCounts[word] ?: 0

            positiveScore += ln((posCount + 1.0) / (totalPositiveWords + vocabSize))
            negativeScore += ln((negCount + 1.0) / (totalNegativeWords + vocabSize))
        }

        val maxScore = if (positiveScore > negativeScore) positiveScore else negativeScore
        val posWeight = exp(positiveScore - maxScore)
        val negWeight = exp(negativeScore - maxScore)
        val sumWeights = posWeight + negWeight

        return if (positiveScore >= negativeScore) {
            SentimentPrediction("Positive", posWeight / sumWeights)
        } else {
            SentimentPrediction("Negative", negWeight / sumWeights)
        }
    }
}