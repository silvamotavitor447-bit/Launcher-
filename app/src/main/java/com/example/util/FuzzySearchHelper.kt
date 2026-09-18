package com.example.util

import java.text.Normalizer
import kotlin.math.min

object FuzzySearchHelper {

  /**
   * Cleans string by lowercasing, trimming, and stripping accents/diacritics (e.g., 'Câmera' -> 'camera').
   */
  fun normalize(input: String): String {
    val normalized = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFD)
    return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
  }

  /**
   * Calculates a match score between the candidate text (e.g. app name) and the search query.
   * Higher score = better match. Returns 0 if no match meets minimum threshold.
   */
  fun scoreMatch(candidate: String, query: String): Int {
    val normCandidate = normalize(candidate)
    val normQuery = normalize(query)

    if (normQuery.isEmpty()) return 100
    if (normCandidate.isEmpty()) return 0

    // 1. Exact match
    if (normCandidate == normQuery) return 1000

    // 2. Starts with query (highest priority for app search)
    if (normCandidate.startsWith(normQuery)) {
      return 800 - (normCandidate.length - normQuery.length)
    }

    // 3. Any word inside candidate starts with query (e.g., 'Google Maps' query 'map')
    val words = normCandidate.split(" ", "-", "_", ".")
    for (word in words) {
      if (word.startsWith(normQuery)) {
        return 700 - (normCandidate.length - normQuery.length)
      }
    }

    // 4. Substring match
    val substringIndex = normCandidate.indexOf(normQuery)
    if (substringIndex != -1) {
      return 600 - substringIndex
    }

    // 5. Acronym / Initialism match (e.g. 'yt' for 'YouTube', 'ps' for 'Play Store')
    val acronym = words.mapNotNull { it.firstOrNull() }.joinToString("")
    if (acronym.contains(normQuery) || normQuery == acronym) {
      return 500
    }

    // 6. Subsequence match: characters in query appear in order inside candidate
    var queryIdx = 0
    var candidateIdx = 0
    var consecutiveBonus = 0
    while (queryIdx < normQuery.length && candidateIdx < normCandidate.length) {
      if (normQuery[queryIdx] == normCandidate[candidateIdx]) {
        if (queryIdx > 0 && candidateIdx > 0 && normCandidate[candidateIdx - 1] == normQuery[queryIdx - 1]) {
          consecutiveBonus += 15
        }
        queryIdx++
      }
      candidateIdx++
    }
    if (queryIdx == normQuery.length) {
      // All characters found in sequence!
      return 400 + consecutiveBonus - (normCandidate.length - normQuery.length)
    }

    // 7. Levenshtein edit distance for typo tolerance (e.g. 'whasapp', 'calcultor', 'spootify')
    // Only apply if query is at least 3 chars long
    if (normQuery.length >= 3) {
      // Check full string distance
      val fullDist = levenshteinDistance(normCandidate, normQuery)
      val maxAllowed = when {
        normQuery.length <= 4 -> 1
        normQuery.length <= 7 -> 2
        else -> 3
      }
      if (fullDist <= maxAllowed) {
        return 350 - (fullDist * 50)
      }

      // Check distance against each word in the candidate
      for (word in words) {
        if (word.isNotEmpty()) {
          val wordDist = levenshteinDistance(word, normQuery)
          if (wordDist <= maxAllowed) {
            return 300 - (wordDist * 50)
          }
          // Also check prefix distance of word
          val prefixLen = min(word.length, normQuery.length)
          val wordPrefix = word.substring(0, prefixLen)
          val queryPrefix = normQuery.substring(0, prefixLen)
          val prefixDist = levenshteinDistance(wordPrefix, queryPrefix)
          val maxPrefixAllowed = if (prefixLen <= 5) 1 else 2
          if (prefixDist <= maxPrefixAllowed) {
            return 280 - (prefixDist * 40)
          }
        }
      }
    }

    return 0
  }

  fun isMatch(candidate: String, query: String): Boolean {
    return scoreMatch(candidate, query) > 0
  }

  private fun levenshteinDistance(s1: String, s2: String): Int {
    val m = s1.length
    val n = s2.length
    val dp = Array(m + 1) { IntArray(n + 1) }

    for (i in 0..m) dp[i][0] = i
    for (j in 0..n) dp[0][j] = j

    for (i in 1..m) {
      for (j in 1..n) {
        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
        dp[i][j] = min(
          dp[i - 1][j] + 1, // deletion
          min(
            dp[i][j - 1] + 1, // insertion
            dp[i - 1][j - 1] + cost // substitution
          )
        )
      }
    }
    return dp[m][n]
  }
}
