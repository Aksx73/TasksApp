package com.absut.tasksapp.data

import androidx.room.TypeConverter

class PairTypeConverter {
	@TypeConverter
	fun fromPair(pair: Pair<Int, Int>): String {
		return "${pair.first},${pair.second}"
	}

	@TypeConverter
	fun toPair(value: String): Pair<Int, Int> {
		val parts = value.split(",")
		return Pair(parts[0].toInt(), parts[1].toInt())
	}
}