package com.example.ritamesa.data.api

import com.example.ritamesa.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/logout")
    fun logout(): Call<Void>

    @GET("me/dashboard/teacher-summary")
    fun getTeacherDashboard(): Call<DashboardGuruResponse>

    @GET("me/dashboard/summary")
    fun getStudentDashboard(): Call<DashboardSiswaResponse>

    @POST("attendance/scan")
    fun scanQRCode(@Body request: ScanRequest): Call<ScanResponse>

    @GET("me/attendance/history")
    fun getTeachingHistory(
        @retrofit2.http.Query("month") month: Int? = null,
        @retrofit2.http.Query("year") year: Int? = null
    ): Call<List<TeachingAttendanceItem>>

    @GET("me/attendance")
    fun getStudentAttendanceHistory(
        @retrofit2.http.Query("month") month: Int? = null,
        @retrofit2.http.Query("year") year: Int? = null
    ): Call<List<StudentAttendanceItem>>

    @GET("me/students/follow-up")
    fun getStudentsFollowUp(
        @retrofit2.http.Query("search") search: String? = null
    ): Call<StudentFollowUpResponse>

    @GET("me/homeroom/dashboard")
    fun getHomeroomDashboard(): Call<HomeroomDashboardResponse>

    @GET("mobile/notifications")
    fun getNotifications(): Call<NotificationResponse>

    @GET("me/homeroom/attendance")
    fun getHomeroomAttendance(
        @retrofit2.http.Query("from") fromDate: String? = null,
        @retrofit2.http.Query("to") toDate: String? = null,
        @retrofit2.http.Query("status") status: String? = null
    ): Call<List<HomeroomAttendanceItem>>

    @GET("me/schedules")
    fun getStudentSchedules(
        @retrofit2.http.Query("date") date: String? = null
    ): Call<StudentScheduleResponse>

    @GET("me/class/attendance")
    fun getClassAttendance(
        @retrofit2.http.Query("from") fromDate: String? = null,
        @retrofit2.http.Query("to") toDate: String? = null
    ): Call<List<HomeroomAttendanceItem>>

    @GET("majors")
    fun getMajors(): Call<MajorResponse>

    // ===== STUDENT CRUD =====
    @GET("students")
    fun getStudents(
        @retrofit2.http.Query("search") search: String? = null,
        @retrofit2.http.Query("page") page: Int? = 1
    ): Call<StudentListResponse>

    @POST("students")
    fun createStudent(@Body request: CreateStudentRequest): Call<GeneralResponse> // Or StudentItem

    @retrofit2.http.PUT("students/{id}")
    fun updateStudent(
        @retrofit2.http.Path("id") id: Int,
        @Body request: CreateStudentRequest // Reusing create request or make specific
    ): Call<GeneralResponse>

    @retrofit2.http.DELETE("students/{id}")
    fun deleteStudent(@retrofit2.http.Path("id") id: Int): Call<GeneralResponse>

    @GET("classes")
    fun getClasses(): Call<List<ClassItem>>

    @retrofit2.http.Multipart
    @POST("classes/{id}/schedule-image")
    fun uploadClassScheduleImage(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): Call<GeneralResponse>
}
