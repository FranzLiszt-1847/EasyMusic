package com.franz.easymusicplayer.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HistorySearch")
data class HistorySearchBean(
    @PrimaryKey
    @ColumnInfo(name = "key") val key: String
    )
