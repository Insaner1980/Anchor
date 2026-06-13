package com.finnvek.anchor.data.local

import androidx.room.TypeConverter
import com.finnvek.anchor.domain.model.ResourceType
import com.finnvek.anchor.domain.model.TagCategory
import com.finnvek.anchor.domain.model.TagRole

class AnchorTypeConverters {

    @TypeConverter
    fun resourceTypeToString(value: ResourceType): String = value.name

    @TypeConverter
    fun stringToResourceType(value: String): ResourceType = ResourceType.valueOf(value)

    @TypeConverter
    fun tagCategoryToString(value: TagCategory): String = value.name

    @TypeConverter
    fun stringToTagCategory(value: String): TagCategory = TagCategory.valueOf(value)

    @TypeConverter
    fun tagRoleToString(value: TagRole): String = value.name

    @TypeConverter
    fun stringToTagRole(value: String): TagRole = TagRole.valueOf(value)
}
