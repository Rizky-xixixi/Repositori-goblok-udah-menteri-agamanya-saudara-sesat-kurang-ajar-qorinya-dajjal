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

    @POST("me/devices")
    fun registerDevice(@Body request: DeviceRequest): Call<Device>

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

    @GET("me/statistics/monthly")
    fun getMonthlyStatistics(
        @retrofit2.http.Query("month") month: Int,
        @retrofit2.http.Query("year") year: Int
    ): Call<TeacherStatisticsResponse>

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

    @POST("majors")
    fun createMajor(@Body request: CreateMajorRequest): Call<GeneralResponse>

    @retrofit2.http.PUT("majors/{id}")
    fun updateMajor(
        @retrofit2.http.Path("id") id: Int,
        @Body request: CreateMajorRequest
    ): Call<GeneralResponse>

    @retrofit2.http.DELETE("majors/{id}")
    fun deleteMajor(@retrofit2.http.Path("id") id: Int): Call<GeneralResponse>

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

    @GET("classes/{id}")
    fun getClassDetail(@retrofit2.http.Path("id") id: Int): Call<ClassDetailResponse>

    @GET("waka/dashboard/summary")
    fun getWakaDashboard(): Call<WakaDashboardResponse>

    @POST("classes")
    fun createClass(@Body request: CreateClassRequest): Call<GeneralResponse>

    @retrofit2.http.PUT("classes/{id}")
    fun updateClass(
        @retrofit2.http.Path("id") id: Int,
        @Body request: CreateClassRequest
    ): Call<GeneralResponse>

    @retrofit2.http.DELETE("classes/{id}")
    fun deleteClass(@retrofit2.http.Path("id") id: Int): Call<GeneralResponse>

    @GET("admin/summary")
    fun getAdminDashboard(): Call<com.example.ritamesa.data.model.AdminDashboardResponse>

    @GET("admin/attendance/history")
    fun getSchoolAttendanceHistory(
        @retrofit2.http.Query("date") date: String? = null,
        @retrofit2.http.Query("status") status: String? = null,
        @retrofit2.http.Query("role") role: String? = null,
        @retrofit2.http.Query("page") page: Int? = 1
    ): Call<SchoolAttendanceResponse>

    @GET("teachers")
    fun getTeachers(): Call<com.example.ritamesa.data.model.TeacherListResponse>

    @POST("teachers")
    fun createTeacher(@Body request: CreateTeacherRequest): Call<GeneralResponse> // Or TeacherItem/Response

    @retrofit2.http.PUT("teachers/{id}")
    fun updateTeacher(
        @retrofit2.http.Path("id") id: Int,
        @Body request: UpdateTeacherRequest
    ): Call<GeneralResponse>

    @retrofit2.http.DELETE("teachers/{id}")
    fun deleteTeacher(@retrofit2.http.Path("id") id: Int): Call<GeneralResponse>

    @POST("attendance/bulk-manual")
    fun submitBulkAttendance(@Body request: BulkAttendanceRequest): Call<GeneralResponse>


    @GET("students")
    fun getStudents(): Call<com.example.ritamesa.data.model.StudentListResponse>

    @retrofit2.http.Multipart
    @POST("classes/{id}/schedule-image")
    fun uploadClassScheduleImage(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): Call<GeneralResponse>
    @retrofit2.http.POST("auth/profile")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<GeneralResponse>

    @GET("classes/{class}/attendance")
    fun getClassAttendanceByDate(
        @retrofit2.http.Path("class") classId: Int,
        @retrofit2.http.Query("date") date: String
    ): Call<ClassAttendanceByDateResponse>

    @GET("absence-requests")
    fun getAbsenceRequests(): Call<AbsenceRequestResponse>

    @POST("absence-requests/{id}/approve")
    fun approveAbsence(@retrofit2.http.Path("id") id: Int): Call<GeneralResponse>

    @POST("absence-requests/{id}/reject")
    fun rejectAbsence(@retrofit2.http.Path("id") id: Int): Call<GeneralResponse>

    @GET("students/absences")
    fun getStudentAttendanceAdmin(
        @retrofit2.http.Query("student_id") studentId: Int,
        @retrofit2.http.Query("from") from: String? = null,
        @retrofit2.http.Query("to") to: String? = null
    ): Call<List<StudentAttendanceAdminResponse>>

    @GET("teachers/{id}/attendance")
    fun getTeacherAttendanceAdmin(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Query("month") month: Int? = null,
        @retrofit2.http.Query("year") year: Int? = null
    ): Call<List<TeachingAttendanceItem>>

    @POST("absence-requests")
    fun submitAbsenceRequest(@Body request: AbsenceRequestRequest): Call<GeneralResponse>

    @GET("waka/attendance/summary")
    fun getWakaAttendanceSummary(
        @retrofit2.http.Query("from") from: String? = null,
        @retrofit2.http.Query("to") to: String? = null,
        @retrofit2.http.Query("type") type: String? = "student"
    ): Call<WakaAttendanceSummaryResponse>
}
