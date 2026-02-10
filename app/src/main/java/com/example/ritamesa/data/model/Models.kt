package com.example.ritamesa.data.model

import com.google.gson.annotations.SerializedName

// ===== AUTH MODELS =====
data class LoginRequest(
    val login: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String = "Android"
)

data class LoginResponse(
    val message: String? = null,
    val token: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    val token_type: String? = null,
    val user: User
) {
    // Compatibility getter - API may return either "token" or "access_token"
    val access_token: String get() = token ?: accessToken ?: ""
}

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
     val date: String? = null,
     val day_name: String? = null,
     @SerializedName("schedule_today") val schedule: List<JadwalItem> = emptyList(),
     @SerializedName("attendance_summary") val attendance: AttendanceSummary? = null,
     val teacher: TeacherInfo? = null
)

data class TeacherInfo(
    val name: String? = null,
    val nip: String? = null,
    val code: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null
)

data class DashboardSiswaResponse(
     val date: String? = null,
     val day_name: String? = null,
     @SerializedName("schedule_today") val schedule: List<JadwalItem> = emptyList(),
     val student: StudentInfo? = null
)

data class StudentInfo(
    val name: String? = null,
    @SerializedName("class_name") val className: String? = null,
    val nis: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("is_class_officer") val isClassOfficer: Boolean = false
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
    @SerializedName("homeroom_class") val homeroomClass: ClassInfoSimple?,
    @SerializedName("code") val code: String? = null
) {
    val name: String get() = user?.name ?: "-"
    val nama: String get() = user?.name ?: "-"
    val kode: String get() = code ?: "-"
    val mapel: String get() = subject ?: "-"
    val keterangan: String get() = homeroomClass?.displayName ?: "-"
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
    @SerializedName("izin") val izin: Int = 0,
    val sick: Int,
    @SerializedName("absent") val alpha: Int,
    val late: Int = 0
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
    val teacher: TeacherProfile?,
    val student: StudentProfile? = null,
    @SerializedName("student_id") val studentId: Int? = null
)

data class TeachingAttendanceItem(
    val id: Int,
    val date: String? = null,
    val status: String? = null,
    val schedule: ScheduleInfo? = null
)

data class ScheduleInfo(
    val id: Int,
    @SerializedName("class") val classInfo: ClassInfo? = null,
    @SerializedName("subject") val subjectInfo: SubjectInfo? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    val teacher: TeacherProfile? = null
)

data class TeacherProfile(
    val id: Int,
    val nip: String?,
    val user: User?
)

data class ClassInfo(val name: String? = null)
data class SubjectInfo(val name: String? = null)

data class StudentAttendanceItem(
    val id: Int,
    val date: String? = null,
    val status: String? = null,
    @SerializedName("check_in_time") val checkInTime: String? = null,
    val schedule: StudentScheduleInfo? = null
)

data class StudentScheduleInfo(
    val id: Int,
    @SerializedName("subject") val subjectInfo: SubjectInfo? = null,
    @SerializedName("teacher") val teacherInfo: TeacherProfileNested? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null
)

data class TeacherProfileNested(
    val id: Int? = null,
    val nip: String? = null,
    val user: UserNested? = null
)

data class UserNested(val name: String? = null)

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
    @SerializedName("subject") val subject: SubjectInfo? = null,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val room: String?,
    val teacher: TeacherProfile?
) {
    // Helper to get subject name from either field
    val displaySubjectName: String
        get() = subjectName ?: subject?.name ?: "Mata Pelajaran"
}

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
    @SerializedName("students_count") val totalSiswa: Int = 0,
    @SerializedName("teachers_count") val totalGuru: Int = 0,
    @SerializedName("classes_count") val totalKelas: Int = 0,
    @SerializedName("majors_count") val totalJurusan: Int = 0,
    @SerializedName("rooms_count") val totalRuangan: Int = 0,
    @SerializedName("attendance_today") val attendance: AdminAttendanceStats? = null
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
    @SerializedName("type") val type: String, // "sick", "excused", "permission" based on API spec
    @SerializedName("date") val date: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
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
// ===== NEW API MODELS FROM OPENAPI SPEC =====

data class Schedule(
    val id: Int,
    val day: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("subject_name") val subjectName: String?,
    val title: String?,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("teacher_id") val teacherId: Int
)

data class ScheduleListResponse(
    val data: List<Schedule>
)

data class ScheduleBulk(
    val day: String,
    val semester: Int,
    val year: Int,
    val items: List<ScheduleBulkItem>
)

data class ScheduleBulkItem(
    @SerializedName("subject_name") val subjectName: String?,
    @SerializedName("subject_id") val subjectId: Int?,
    @SerializedName("teacher_id") val teacherId: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val room: String?
)

data class QRCodeListResponse(
    val data: List<QRCode>
)

data class QRCode(
    val id: Int,
    val token: String,
    @SerializedName("schedule_id") val scheduleId: Int,
    val type: String,
    @SerializedName("expires_at") val expiresAt: String?,
    val active: Boolean
)

data class QrGenerate(
    @SerializedName("schedule_id") val scheduleId: Int,
    val type: String, // "student", "teacher"
    @SerializedName("expires_in_minutes") val expiresInMinutes: Int? = null
)

data class QrGenerateResponse(
    val qrcode: QRCode,
    val qr_svg: String,
    val payload: Map<String, Any>
)

data class AttendanceRecapResponse(
    val month: String,
    val data: List<AttendanceRecapItem>
)

data class AttendanceRecapItem(
    val date: String,
    val status: String,
    @SerializedName("check_in_time") val checkInTime: String?
)

data class AttendanceExportResponse(
    val data: List<AttendanceExportItem>,
    val filename: String
)

data class AttendanceExportItem(
    val student: StudentProfile?,
    val schedule: ScheduleInfo?,
    val status: String,
    @SerializedName("check_in_time") val checkInTime: String?
)

data class ScheduleSummaryResponse(
    val schedule: ScheduleInfo?,
    val summary: AttendanceSummaryDetailed
)

data class ClassSummaryResponse(
    @SerializedName("class") val classInfo: ClassInfoSimple,
    val summary: AttendanceSummaryDetailed
)

data class ExcuseRequest(
    val excuse: String
)

data class AbsenceRequestCreate(
    @SerializedName("student_id") val studentId: Int,
    val type: String, // "sick", "permission", "leave"
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val reason: String?
)

data class AbsenceRequest(
    val id: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("class_id") val classId: Int,
    val type: String,
    val status: String, // "pending", "approved", "rejected"
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String
)

data class AbsenceRequestApproval(
    @SerializedName("approver_signature") val approverSignature: String? = null
)

data class MajorCreate(
    val code: String,
    val name: String,
    val category: String? = null
)

data class MajorListResponse(
    val data: List<MajorItem>
)

data class MajorItem(
    val id: Int,
    val code: String,
    val name: String,
    val category: String?
)

data class ClassCreate(
    val grade: String,
    val label: String,
    @SerializedName("major_id") val majorId: Int?
)

data class ClassListResponse(
    val data: List<ClassItem>
)

data class WaText(
    val to: String,
    val message: String
)

data class WaMedia(
    val to: String,
    @SerializedName("mediaBase64") val mediaBase64: String,
    val filename: String,
    val caption: String? = null
)

@Data
data class SettingResponse(
    val status: String,
    @SerializedName("data") val data: Map<String, String>
)

data class SubjectItem(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)

data class SubjectListResponse(
    @SerializedName("data") val data: List<SubjectItem>
)