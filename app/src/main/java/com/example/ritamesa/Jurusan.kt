import com.google.gson.annotations.SerializedName

data class Jurusan(
    val id: Int,
    @SerializedName("name")
    val KonsentrasiKeahlian: String,
    @SerializedName("code")
    val Kodejurusan: String,
)