package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SurahListResponse(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "message") val message: String = "",
    @Json(name = "data") val data: List<SurahItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SurahItemDto(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String,
    @Json(name = "audioFull") val audioFull: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SurahDetailResponse(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "message") val message: String = "",
    @Json(name = "data") val data: SurahDetailDto
)

@JsonClass(generateAdapter = true)
data class SurahDetailDto(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String,
    @Json(name = "audioFull") val audioFull: Map<String, String>? = null,
    @Json(name = "ayat") val ayat: List<AyatDto> = emptyList(),
    @Json(name = "suratSelanjutnya") val suratSelanjutnya: Any? = null,
    @Json(name = "suratSebelumnya") val suratSebelumnya: Any? = null
)

@JsonClass(generateAdapter = true)
data class AyatDto(
    @Json(name = "nomorAyat") val nomorAyat: Int,
    @Json(name = "teksArab") val teksArab: String,
    @Json(name = "teksLatin") val teksLatin: String,
    @Json(name = "teksIndonesia") val teksIndonesia: String,
    @Json(name = "audio") val audio: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class TafsirResponse(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "message") val message: String = "",
    @Json(name = "data") val data: TafsirDetailDto
)

@JsonClass(generateAdapter = true)
data class TafsirDetailDto(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String,
    @Json(name = "tafsir") val tafsir: List<TafsirItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TafsirItemDto(
    @Json(name = "ayat") val ayat: Int,
    @Json(name = "teks") val teks: String
)
