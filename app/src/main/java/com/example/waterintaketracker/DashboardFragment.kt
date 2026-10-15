package com.example.waterintaketracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Screen 2: a summary of everything the user has logged. */
class DashboardFragment : Fragment() {

    companion object {
        const val DAILY_GOAL_OZ = 64.0
    }

    private lateinit var todayValue: TextView
    private lateinit var goalText: TextView
    private lateinit var goalProgress: ProgressBar
    private lateinit var weeklyChart: WeeklyChartView
    private lateinit var weekTotalText: TextView

    private lateinit var entriesStat: Pair<TextView, TextView>
    private lateinit var avgEntryStat: Pair<TextView, TextView>
    private lateinit var dailyAvgStat: Pair<TextView, TextView>
    private lateinit var bestDayStat: Pair<TextView, TextView>
    private lateinit var streakStat: Pair<TextView, TextView>
    private lateinit var allTimeStat: Pair<TextView, TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todayValue = view.findViewById(R.id.todayValue)
        goalText = view.findViewById(R.id.goalText)
        goalProgress = view.findViewById(R.id.goalProgress)
        weeklyChart = view.findViewById(R.id.weeklyChart)
        weekTotalText = view.findViewById(R.id.weekTotalText)

        entriesStat = bindStat(view, R.id.statEntries, R.string.stat_entries)
        avgEntryStat = bindStat(view, R.id.statAvgEntry, R.string.stat_avg_entry)
        dailyAvgStat = bindStat(view, R.id.statDailyAvg, R.string.stat_daily_avg)
        bestDayStat = bindStat(view, R.id.statBestDay, R.string.stat_best_day)
        streakStat = bindStat(view, R.id.statStreak, R.string.stat_streak)
        allTimeStat = bindStat(view, R.id.statAllTime, R.string.stat_all_time)

        weeklyChart.goal = DAILY_GOAL_OZ.toFloat()

        val dao = AppDatabase.getInstance(requireContext()).waterDao()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.getAllEntries().collect { render(it) }
            }
        }
    }

    /** Finds an included item_stat card, sets its label, returns (value, label). */
    private fun bindStat(root: View, cardId: Int, labelRes: Int): Pair<TextView, TextView> {
        val card = root.findViewById<View>(cardId)
        val value = card.findViewById<TextView>(R.id.statValue)
        val label = card.findViewById<TextView>(R.id.statLabel)
        label.setText(labelRes)
        return value to label
    }

    private fun render(entries: List<WaterEntry>) {
        // Total per calendar day, keyed by that day's midnight.
        val totalsByDay: Map<Long, Double> = entries
            .groupBy { DateUtils.startOfDay(it.timestamp) }
            .mapValues { (_, list) -> list.sumOf { it.amountOz } }

        // Today + goal
        val today = totalsByDay[DateUtils.startOfToday()] ?: 0.0
        todayValue.text = getString(R.string.oz_value, WaterAdapter.formatOz(today))
        val percent = ((today / DAILY_GOAL_OZ) * 100).toInt()
        goalProgress.progress = percent.coerceAtMost(100)
        goalText.text = if (today >= DAILY_GOAL_OZ) {
            getString(R.string.goal_reached, WaterAdapter.formatOz(DAILY_GOAL_OZ))
        } else {
            getString(
                R.string.goal_remaining,
                WaterAdapter.formatOz(DAILY_GOAL_OZ - today),
                WaterAdapter.formatOz(DAILY_GOAL_OZ)
            )
        }

        // Last 7 days chart (oldest on the left, today on the right)
        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault())
        val days = (6 downTo 0).map { DateUtils.startOfDayAgo(it) }
        val weekValues = days.map { (totalsByDay[it] ?: 0.0).toFloat() }
        val weekLabels = days.mapIndexed { i, day ->
            if (i == days.lastIndex) getString(R.string.today) else dayLabel.format(Date(day))
        }
        weeklyChart.setData(weekValues, weekLabels)
        weekTotalText.text = getString(
            R.string.week_total, WaterAdapter.formatOz(weekValues.sum().toDouble())
        )

        // Stats grid
        val allTime = entries.sumOf { it.amountOz }
        entriesStat.first.text = entries.size.toString()
        avgEntryStat.first.text = if (entries.isEmpty()) "—"
            else getString(R.string.oz_value, WaterAdapter.formatOz(allTime / entries.size))
        dailyAvgStat.first.text = if (totalsByDay.isEmpty()) "—"
            else getString(R.string.oz_value, WaterAdapter.formatOz(allTime / totalsByDay.size))
        bestDayStat.first.text = totalsByDay.values.maxOrNull()
            ?.let { getString(R.string.oz_value, WaterAdapter.formatOz(it)) } ?: "—"
        val streak = currentStreak(totalsByDay.keys)
        streakStat.first.text = resources.getQuantityString(R.plurals.days, streak, streak)
        allTimeStat.first.text = getString(R.string.oz_value, WaterAdapter.formatOz(allTime))
    }

    /** Consecutive days with at least one entry, counting back from today (or yesterday). */
    private fun currentStreak(daysWithEntries: Set<Long>): Int {
        var offset = if (DateUtils.startOfToday() in daysWithEntries) 0 else 1
        var streak = 0
        while (DateUtils.startOfDayAgo(offset) in daysWithEntries) {
            streak++
            offset++
        }
        return streak
    }
}
