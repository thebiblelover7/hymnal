package org.sda.hymnal.data

// This was written very helpfully by Claude, as I basically do not
// understand how this works at all. The idea is that it calculates
// the similarity between two strings and then only returns those above
// a certain threshold.

object FuzzySearch {
    fun levenshteinDistance(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = Array(m + 1) { IntArray(n+1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (a[i-1] == b[j-1]) {
                    dp [i-1][j-1]
                } else {
                    1 + minOf(
                        dp[i-1][j], 	    // deletion
                        dp[i][j-1],		// insertion
                        dp[i-1][j-1]	    // substitution
                    )
                }
            }
        }

        return dp[m][n]
    }

    // Returns a score from 0.0 (no match) and 1.0 (perfect match)
    fun similarityScore(query: String, target: String): Double {
        if (query.isEmpty()) return 1.0
        if (target.isEmpty()) return 0.0

        val q = query.lowercase()
        val t = target.lowercase()

        if (t.contains(q)) return 1.0

        val targetWords = t.split(" ")
        val bestWordScore = targetWords.maxOf { word ->
            val maxLen = maxOf(q.length, word.length).toDouble()
            1.0 - (levenshteinDistance(q, word) / maxLen)
        }

        val fullLen = maxOf(q.length, t.length).toDouble()
        val fullScore = 1.0 - (levenshteinDistance(q, t) / fullLen)

        return maxOf(bestWordScore, fullScore)
    }

    fun <T> search(
        query: String,
        items: List<T>,
        threshold: Double = 0.6,
        titleSelector: (T) -> String,
        bodySelector: (T) -> String,
        titleWeight: Double = 0.7,
        bodyWeight: Double = 0.1,
    ): List<T> {
        if (query.isBlank()) return emptyList()

        return items
            .map { item ->
                val titleScore = similarityScore(query, titleSelector(item))
                val bodyScore = similarityScore(query, bodySelector(item))
                val weightedScore = (titleScore * titleWeight) + (bodyScore * bodyWeight)
                item to weightedScore
            }
            .filter { (_, score) -> score >= threshold }
            .sortedByDescending { (_, score) -> score }
            .map { (item, _) -> item}
    }
}