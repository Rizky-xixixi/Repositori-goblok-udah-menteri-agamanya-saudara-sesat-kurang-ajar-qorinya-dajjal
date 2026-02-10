package com.example.ritamesa.data.api

import com.example.ritamesa.data.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // ===== AUTH ENDPOINTS =====
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/logout")
    fun logout(): Call<Void>

    @POST("devices")
    fun registerDevice(@Body request: DeviceRequest): Call<Device>

    @GET("me")
    fun getCurrentUser(): Call<User>

    // ===== SCHEDULES ENDPOINTS =====
    @GET("schedules")
    fun getSchedules(
        @Query("class_id") classId: Int? = null,
        @Query("date") date: String? = null
    ): Call<ScheduleListResponse>

    @GET("schedules/{schedule}")
    fun getScheduleDetail(@Path("schedule") scheduleId: Int): Call<Schedule>

    @POST("classes/{class}/schedules/bulk")
    fun bulkUpsertSchedules(
        @Path("class") classId: Int,
        @Body request: ScheduleBulk
    ): Call<GeneralResponse>

    // ===== QR CODE ENDPOINTS =====
    @GET("qrcodes/active")
    fun getActiveQRCodes(): Call<QRCodeListResponse>

    @POST("qrcodes/generate")
    fun generateQRCode(@Body request: QrGenerate): Call<QrGenerateResponse>

    @POST("qrcodes/{token}/revoke")
    fun revokeQRCode(@Path("token") token: String): Call<GeneralResponse>

    // ===== ATTENDANCE ENDPOINTS =====
    @POST("attendance/scan")
    fun scanQRCode(@Body request: ScanRequest): Call<ScanResponse>

    @GET("attendance/recap")
    fun getAttendanceRecap(@Query("month") month: String): Call<AttendanceRecapResponse>

    @GET("attendance/export")
    fun exportAttendance(
        @Query("class_id") classId: Int? = null,
        @Query("schedule_id") scheduleId: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Call<AttendanceExportResponse>

    @GET("attendance/schedules/{schedule}/summary")
    fun getScheduleSummary(@Path("schedule") scheduleId: Int): Call<ScheduleSummaryResponse>

    @GET("attendance/classes/{class}/summary")
    fun getClassSummary(@Path("class") classId: Int): Call<ClassSummaryResponse>

    @POST("attendance/{attendance}/excuse")
    fun updateAttendanceExcuse(
        @Path("attendance") attendanceId: Int,
        @Body request: ExcuseRequest
    ): Call<GeneralResponse>

    // ===== ABSENCE REQUESTS ENDPOINTS =====
    @GET("absence-requests")
    fun getAbsenceRequests(): Call<AbsenceRequestResponse>

    @POST("absence-requests")
    fun createAbsenceRequest(@Body request: AbsenceRequestCreate): Call<AbsenceRequest>

    @POST("absence-requests")
    fun submitAbsenceRequest(@Body request: AbsenceRequestRequest): Call<GeneralResponse>

    @POST("absence-requests/{absenceRequest}/approve")
    fun approveAbsenceRequest(
        @Path("absenceRequest") absenceRequestId: Int,
        @Body request: AbsenceRequestApproval? = null
    ): Call<GeneralResponse>

    @POST("absence-requests/{absenceRequest}/reject")
    fun rejectAbsenceRequest(
        @Path("absenceRequest") absenceRequestId: Int,
        @Body request: AbsenceRequestApproval? = null
    ): Call<GeneralResponse>

    // ===== MASTER DATA ENDPOINTS =====
    @GET("majors")
    fun getMajors(): Call<MajorListResponse>

    @POST("majors")
    fun createMajor(@Body request: MajorCreate): Call<GeneralResponse>

    @GET("classes")
    fun getClasses(): Call<ClassListResponse>

    @POST("classes")
    fun createClass(@Body request: ClassCreate): Call<GeneralResponse>

    @GET("subjects")
    fun getSubjects(@Query("per_page") perPage: Int? = -1): Call<SubjectListResponse>

    // ===== WHATSAPP ENDPOINTS =====
    @POST("wa/send-text")
    fun sendWhatsAppText(@Body request: WaText): Call<GeneralResponse>

    @POST("wa/send-media")
    fun sendWhatsAppMedia(@Body request: WaMedia): Call<GeneralResponse>

    // ===== LEGACY ENDPOINTS - KEPT FOR BACKWARD COMPATIBILITY =====
    @GET("me/dashboard/teacher-summary")
    fun getTeacherDashboard(): Call<DashboardGuruResponse>

    @GET("me/dashboard/summary")
    fun getStudentDashboard(): Call<DashboardSiswaResponse>

    @GET("me/attendance/history")
    fun getTeachingHistory(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Call<List<TeachingAttendanceItem>>

    @GET("me/attendance")
    fun getStudentAttendanceHistory(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Call<List<StudentAttendanceItem>>

    @GET("me/statistics/monthly")
    fun getMonthlyStatistics(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Call<TeacherStatisticsResponse>

    @GET("me/students/follow-up")
    fun getStudentsFollowUp(
        @Query("search") search: String? = null
    ): Call<StudentFollowUpResponse>

    @GET("me/homeroom/dashboard")
    fun getHomeroomDashboard(): Call<HomeroomDashboardResponse>

    @GET("mobile/notifications")
    fun getNotifications(): Call<NotificationResponse>

    @GET("me/homeroom/attendance")
    fun getHomeroomAttendance(
        @Query("from") fromDate: String? = null,
        @Query("to") toDate: String? = null,
        @Query("status") status: String? = null
    ): Call<List<HomeroomAttendanceItem>>

    @GET("me/schedules")
    fun getStudentSchedules(
        @Query("date") date: String? = null
    ): Call<StudentScheduleResponse>

    // Alternative: If backend returns list directly
    @GET("me/schedules")
    fun getStudentSchedulesList(
        @Query("date") date: String? = null
    ): Call<List<StudentScheduleItem>>

    @GET("me/class/attendance")
    fun getClassAttendance(
        @Query("from") fromDate: String? = null,
        @Query("to") toDate: String? = null
    ): Call<List<HomeroomAttendanceItem>>

    @POST("majors")
    fun createMajorLegacy(@Body request: CreateMajorRequest): Call<GeneralResponse>

    @PUT("majors/{id}")
    fun updateMajor(
        @Path("id") id: Int,
        @Body request: CreateMajorRequest
    ): Call<GeneralResponse>

    @DELETE("majors/{id}")
    fun deleteMajor(@Path("id") id: Int): Call<GeneralResponse>

    // ===== STUDENT CRUD =====
    @GET("students")
    fun getStudents(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = 1
    ): Call<StudentListResponse>

    @POST("students")
    fun createStudent(@Body request: CreateStudentRequest): Call<GeneralResponse>

    @PUT("students/{id}")
    fun updateStudent(
        @Path("id") id: Int,
        @Body request: CreateStudentRequest
    ): Call<GeneralResponse>

    @DELETE("students/{id}")
    fun deleteStudent(@Path("id") id: Int): Call<GeneralResponse>

    @GET("classes/{id}")
    fun getClassDetail(@Path("id") id: Int): Call<ClassDetailResponse>

    @GET("waka/dashboard/summary")
    fun getWakaDashboard(): Call<WakaDashboardResponse>

    @POST("classes")
    fun createClassLegacy(@Body request: CreateClassRequest): Call<GeneralResponse>

    @PUT("classes/{id}")
    fun updateClass(
        @Path("id") id: Int,
        @Body request: ClassCreate
    ): Call<GeneralResponse>

    @DELETE("classes/{id}")
    fun deleteClass(@Path("id") id: Int): Call<GeneralResponse>

    @GET("admin/summary")
    fun getAdminDashboard(): Call<AdminDashboardResponse>

    @GET("admin/attendance/history")
    fun getSchoolAttendanceHistory(
        @Query("date") date: String? = null,
        @Query("status") status: String? = null,
        @Query("role") role: String? = null,
        @Query("page") page: Int? = 1
    ): Call<SchoolAttendanceResponse>

    @GET("teachers")
    fun getTeachers(): Call<TeacherListResponse>

    @POST("teachers")
    fun createTeacher(@Body request: CreateTeacherRequest): Call<GeneralResponse>

    @PUT("teachers/{id}")
    fun updateTeacher(
        @Path("id") id: Int,
        @Body request: UpdateTeacherRequest
    ): Call<GeneralResponse>

    @DELETE("teachers/{id}")
    fun deleteTeacher(@Path("id") id: Int): Call<GeneralResponse>

    @POST("attendance/bulk-manual")
    fun submitBulkAttendance(@Body request: BulkAttendanceRequest): Call<GeneralResponse>

    @Multipart
    @POST("classes/{id}/schedule-image")
    fun uploadClassScheduleImage(
        @Path("id") id: Int,
        @Part file: okhttp3.MultipartBody.Part
    ): Call<GeneralResponse>

    @POST("auth/profile")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<GeneralResponse>

    @GET("classes/{class}/attendance")
    fun getClassAttendanceByDate(
        @Path("class") classId: Int,
        @Query("date") date: String
    ): Call<ClassAttendanceByDateResponse>

    @GET("students/absences")
    fun getStudentAttendanceAdmin(
        @Query("student_id") studentId: Int,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Call<List<StudentAttendanceAdminResponse>>

    @GET("teachers/{id}/attendance")
    fun getTeacherAttendanceAdmin(
        @Path("id") id: Int,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Call<List<TeachingAttendanceItem>>

    @GET("waka/attendance/summary")
    fun getWakaAttendanceSummary(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: String? = "student"
    ): Call<WakaAttendanceSummaryResponse>

    @GET("settings")
    fun getSettings(): Call<SettingResponse>
}
