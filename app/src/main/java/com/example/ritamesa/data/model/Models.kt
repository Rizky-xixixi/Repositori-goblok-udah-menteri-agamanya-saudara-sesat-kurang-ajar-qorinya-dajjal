package com.example.ritamesa.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String = "Android"
)

data class LoginResponse(
    val message: String,
    val access_token: String,
    val token_type: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String?,
    @SerializedName("user_type") val role: String, // Map user_type from backend to role
    @SerializedName("is_class_officer") val isClassOfficer: Boolean = false,
    val profile: UserProfile?
)

data class DeviceRequest(
    val identifier: String,
    val name: String,
    val platform: String = "Android"
)

data class Device(
    val id: Int,
    val identifier: String,
    val name: String,
    val platform: String?,
    val active: Boolean
)

data class UserProfile(
    val nis: String?,
    val nip: String?,
    @SerializedName("class_name") val className: String?,
    @SerializedName("photo_url") val photoUrl: String?
)

data class DashboardGuruResponse(
     val date: String,
     val day_name: String,
     @SerializedName("schedule_today") val schedule: List<JadwalItem>,
     @SerializedName("attendance_summary") val attendance: AttendanceSummary,
     val teacher: TeacherInfo
)

data class TeacherInfo(
    val name: String,
    val nip: String?,
    val code: String?,
    @SerializedName("photo_url") val photoUrl: String?
)

data class DashboardSiswaResponse(
     val date: String,
     val day_name: String,
     @SerializedName("schedule_today") val schedule: List<JadwalItem>,
     val student: StudentInfo
)

data class StudentInfo(
    val name: String,
    @SerializedName("class_name") val className: String?,
    val nis: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("is_class_officer") val isClassOfficer: Boolean
)

data class JadwalItem(
    val id: Int,
    @SerializedName("subject") val mataPelajaran: String,
    @SerializedName("class_name") val kelas: String? = null, // Nullable for student (implicitly their class)
    @SerializedName("time_slot") val jam: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("teacher") val teacherName: String? = null, // For student dashboard
    @SerializedName("class_id") val classId: Int? = null, // ADDED for deep linking
    val status: String? = null, // For student dashboard
    @SerializedName("status_label") val statusLabel: String? = null,
    @SerializedName("check_in_time") val checkInTime: String? = null
) {
    val waktuPelajaran: String
        get() = "$startTime - $endTime"
    val statusDispensasi: String
        get() = when(status) {
            "approved" -> "Disetujui"
            "rejected" -> "Ditolak"
            else -> "Menunggu"
        }
}

data class StudentAttendanceAdminResponse(
    val student: StudentProfile?,
    val items: List<StudentAttendanceItem> // items are basically attendance records
)

// ===== TEACHER RECAP MODELS =====
data class TeacherListResponse(
    val data: List<TeacherItem>,
    val meta: Meta? = null,
    val links: Links? = null
)

data class TeacherItem(
    val id: Int,
    val nip: String?,
    @SerializedName("subject") val subject: String?,
    val user: UserInfo?,
    @SerializedName("homeroom_class") val homeroomClass: ClassInfoSimple?
) {
    val name: String get() = user?.name ?: "-"
    val mapel: String get() = subject ?: "-"
}

data class Meta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int
)

data class Links(
    val first: String?,
    val last: String?,
    val prev: String?,
    val next: String?
)

data class AttendanceSummary(
    val present: Int,
    @SerializedName("excused") val permission: Int,
    @SerializedName("izin") val izin: Int = 0, // Backend splits excused and izin? TODO check
    val sick: Int,
    @SerializedName("absent") val alpha: Int
)

data class ScanRequest(
    @SerializedName("token") val token: String,
    @SerializedName("device_id") val deviceId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class ScanResponse(
    val message: String,
    val attendance: Attendance?
)

data class Attendance(
    val id: Int,
    val status: String,
    @SerializedName("check_in_time") val checkInTime: String?,
    val schedule: JadwalItem?,
    val teacher: TeacherProfile?
)

data class TeachingAttendanceItem(
    val id: Int,
    val date: String,
    val status: String,
    val schedule: ScheduleInfo?
)

data class ScheduleInfo(
    val id: Int,
    @SerializedName("class") val classInfo: ClassInfo?,
    @SerializedName("subject") val subjectInfo: SubjectInfo?,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val teacher: TeacherProfile?
)

data class TeacherProfile(
    val id: Int,
    val nip: String?,
    val user: User?
)

data class ClassInfo(val name: String)
data class SubjectInfo(val name: String)

data class StudentAttendanceItem(
    val id: Int,
    val date: String,
    val status: String,
    @SerializedName("check_in_time") val checkInTime: String?,
    val schedule: StudentScheduleInfo?
)

data class StudentScheduleInfo(
    val id: Int,
    @SerializedName("subject") val subjectInfo: SubjectInfo?,
    @SerializedName("teacher") val teacherInfo: TeacherProfileNested?,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String
)

data class TeacherProfileNested(
    val user: UserNested
)

data class UserNested(val name: String)

data class StudentFollowUpResponse(
    val data: List<StudentFollowUpItem>
)

data class StudentFollowUpItem(
    val id: Int,
    val name: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("attendance_summary") val attendanceSummary: AttendanceSummarySimple,
    val badge: BadgeInfo,
    @SerializedName("severity_score") val severityScore: Int
)

data class AttendanceSummarySimple(
    val absent: Int,
    val excused: Int,
    val sick: Int
)

data class BadgeInfo(
    val type: String, // danger, warning, success
    val label: String
)

data class HomeroomDashboardResponse(
    val date: String,
    @SerializedName("homeroom_class") val homeroomClass: HomeroomClassInfo,
    @SerializedName("attendance_summary") val attendanceSummary: AttendanceSummaryDetailed,
    @SerializedName("schedule_today") val scheduleToday: List<HomeroomScheduleItem>
)

data class HomeroomClassInfo(
    val id: Int,
    val name: String,
    @SerializedName("total_students") val totalStudents: Int
)

data class AttendanceSummaryDetailed(
    val present: Int,
    val late: Int,
    val sick: Int,
    val excused: Int,
    val absent: Int
)

data class HomeroomScheduleItem(
    val id: Int,
    val subject: String,
    val teacher: String,
    @SerializedName("time_slot") val timeSlot: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String
)

data class NotificationResponse(
    val date: String,
    val notifications: List<NotificationItem>
)

data class NotificationItem(
    val id: String, // String to handle both int IDs and string IDs
    val type: String,
    val message: String,
    val detail: String,
    val time: String,
    @SerializedName("created_at") val createdAt: String
)

data class HomeroomAttendanceItem(
    val id: Int,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    val student: StudentProfile?,
    val schedule: ScheduleInfo?
)

data class StudentProfile(
    val id: Int,
    val user: User?
)

data class StudentScheduleResponse(
    val date: String,
    val day: String,
    val items: List<StudentScheduleItem>
)

data class StudentScheduleItem(
    val id: Int,
    @SerializedName("subject_name") val subjectName: String?,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val room: String?,
    val teacher: TeacherProfile?
)

data class ClassItem(
    val id: Int,
    val name: String,
    val grade: String,
    val label: String,
    @SerializedName("major") val major: MajorInfo?,
    @SerializedName("homeroom_teacher") val homeroomTeacher: TeacherProfile?,
    @SerializedName("schedule_image_path") val scheduleImagePath: String?
)

data class MajorInfo(
    val id: Int,
    val name: String,
    val code: String
)

data class ClassDetailResponse(
    val id: Int,
    val name: String,
    val grade: String,
    val label: String,
    @SerializedName("major") val major: MajorInfo?,
    @SerializedName("homeroom_teacher") val homeroomTeacher: TeacherProfile?,
    @SerializedName("schedule_image_path") val scheduleImagePath: String?,
    val students: List<StudentItem>? // List of students in the class
)

// ===== NEW MODELS FOR STUDENT CRUD =====

data class StudentListResponse(
    val data: List<StudentItem>,
    val current_page: Int,
    val last_page: Int
)

data class StudentItem(
    val id: Int,
    val user: UserInfo?,
    @SerializedName("class_room") val classRoom: ClassInfoSimple?,
    val nisn: String?,
    val nis: String?,
    val gender: String?,
    val address: String?
) {
    // Helper accessors
    val name: String get() = user?.name ?: "-"
    val className: String get() = classRoom?.name ?: "-"
    val majorName: String get() = classRoom?.major?.name ?: "-"
}

data class UserInfo(
    val id: Int,
    val name: String,
    val username: String?,
    val email: String?
)

data class ClassInfoSimple(
    val id: Int,
    val name: String?,
    val grade: String?,
    val label: String?,
    val major: MajorInfo?
) {
    // Construct name if null
    val displayName: String get() = name ?: "$grade $label"
}

data class CreateStudentRequest(
    val name: String,
    val nisn: String,
    val nis: String,
    @SerializedName("class_id") val classId: Int,
    val gender: String, // "L" or "P"
    val username: String,
    val password: String = "password123",
    val address: String = "-"
)

data class UpdateStudentRequest(
    val name: String?,
    val nisn: String?,
    @SerializedName("class_id") val classId: Int?
    // Add others as needed
)

data class GeneralResponse(
    val message: String
)

data class TeacherStatisticsResponse(
    val month: String,
    val year: String,
    val summary: StatisticsSummary,
    @SerializedName("chart_data") val chartData: List<DailyStatistic>
)

data class StatisticsSummary(
    val hadir: Int,
    val sakit: Int,
    val izin: Int,
    val alfa: Int,
    val terlambat: Int = 0
)

data class DailyStatistic(
    val day: Int,
    val status: String
)

data class WakaDashboardResponse(
    val date: String,
    val statistik: WakaStatistik,
    val trend: List<WakaTrendItem>
)

data class WakaStatistik(
    val hadir: Int,
    val izin: Int,
    val sakit: Int,
    val alpha: Int,
    val terlambat: Int,
    val pulang: Int
)

data class WakaTrendItem(
    val date: String,
    val label: String,
    val hadir: Int,
    val izin: Int,
    val sakit: Int,
    val alpha: Int,
    val terlambat: Int
)

data class CreateMajorRequest(
    val name: String,
    val code: String
)

data class CreateClassRequest(
    val grade: String,
    val label: String,
    @SerializedName("major_id") val majorId: Int?
)

data class AdminDashboardResponse(
    @SerializedName("students_count") val totalSiswa: Int,
    @SerializedName("teachers_count") val totalGuru: Int,
    @SerializedName("classes_count") val totalKelas: Int,
    @SerializedName("majors_count") val totalJurusan: Int,
    @SerializedName("attendance_today") val attendance: AdminAttendanceStats
)

data class AdminAttendanceStats(
    val hadir: Int,
    val izin: Int,
    val sakit: Int,
    val alpha: Int,
    val terlambat: Int,
    val pulang: Int
)

data class WakaAttendanceSummaryResponse(
    @SerializedName("status_summary") val statusSummary: List<StatusSummaryItem>,
    // class_summary and student_summary omitted for now as not needed for chart
)

data class StatusSummaryItem(
    val status: String,
    val total: Int
)

data class CreateTeacherRequest(
    val name: String,
    val username: String,
    val email: String?,
    val password: String,
    val nip: String,
    val phone: String?,
    val contact: String?,
    @SerializedName("homeroom_class_id") val homeroomClassId: Int?,
    val subject: String?
)

data class UpdateTeacherRequest(
    val name: String?,
    val email: String?,
    val phone: String?,
    val contact: String?,
    @SerializedName("homeroom_class_id") val homeroomClassId: Int?,
    val subject: String?
)

data class BulkAttendanceRequest(
    @SerializedName("schedule_id") val scheduleId: Int,
    val date: String,
    val items: List<BulkAttendanceItem>
)

data class BulkAttendanceItem(
    @SerializedName("student_id") val studentId: Int,
    val status: String
)

data class ClassAttendanceByDateResponse(
    val items: List<ClassAttendanceScheduleItem>
)

data class ClassAttendanceScheduleItem(
    val schedule: ScheduleInfo?,
    val attendances: List<Attendance>
)


data class SchoolAttendanceResponse(
    val data: List<SchoolAttendanceItem>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int
)

data class SchoolAttendanceItem(
    val id: Int,
    val date: String,
    val status: String,
    @SerializedName("checked_in_at") val checkedInTime: String?,
    @SerializedName("attendee_type") val attendeeType: String,
    val student: StudentProfile?,
    val teacher: TeacherProfile?,
    val schedule: ScheduleInfo?
)

data class AbsenceRequestRequest(
    @SerializedName("type") val type: String, // "sakit", "izin", "izin_pulang"
    @SerializedName("date") val date: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("schedule_id") val scheduleId: Int? = null,
    @SerializedName("student_id") val studentId: Int? = null // For teacher submitting on behalf of student (Dispensasi)
)

data class AbsenceRequestResponse(
    val data: List<AbsenceRequestItem>
)

data class AbsenceRequestItem(
    val id: Int,
    val type: String,
    val date: String,
    val reason: String,
    val status: String,
    val user: User?,
    val schedule: ScheduleInfo?
)

data class UpdateProfileRequest(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null
)
