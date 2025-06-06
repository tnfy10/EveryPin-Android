package everypin.app.network.model.post


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WriterDto(
    @SerialName("createdDate")
    val createdDate: String?,
    @SerialName("photoUrl")
    val photoUrl: String?,
    @SerialName("profileDisplayId")
    val profileDisplayId: String,
    @SerialName("profileName")
    val profileName: String,
    @SerialName("selfIntroduction")
    val selfIntroduction: String?,
    @SerialName("updatedDate")
    val updatedDate: String?
)