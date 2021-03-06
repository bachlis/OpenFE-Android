package de.datlag.openfe.recycler.data

import android.os.Parcelable
import de.datlag.openfe.commons.getRootOfStorage
import de.datlag.openfe.data.Usage
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class LocationItem(
    val name: String,
    val usage: Usage,
    val rootFile: File = File(usage.file.getRootOfStorage())
) : Parcelable