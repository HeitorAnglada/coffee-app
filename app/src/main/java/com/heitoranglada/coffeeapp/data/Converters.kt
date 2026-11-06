package com.heitoranglada.coffeeapp.data

import androidx.room.TypeConverter
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.PourCodec

class Converters {
    @TypeConverter
    fun methodToString(method: BrewMethod): String = method.name

    @TypeConverter
    fun stringToMethod(value: String): BrewMethod =
        BrewMethod.entries.firstOrNull { it.name == value } ?: BrewMethod.V60

    @TypeConverter
    fun poursToString(pours: List<Pour>): String = PourCodec.encode(pours)

    @TypeConverter
    fun stringToPours(value: String): List<Pour> = PourCodec.decode(value)
}
