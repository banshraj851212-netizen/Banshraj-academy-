package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AcademyDao
import com.example.data.local.entity.AccessRequestEntity
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.DoubtEntity
import com.example.data.local.entity.DppEntity
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        BatchEntity::class,
        LectureEntity::class,
        DppEntity::class,
        TestEntity::class,
        DoubtEntity::class,
        AnnouncementEntity::class,
        AccessRequestEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "banshraj_academy_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AcademyDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AcademyDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.academyDao())
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.academyDao())
                }
            }
        }

        suspend fun populateInitialData(dao: AcademyDao) {
            // Seed Users
            val users = listOf(
                UserEntity(
                    id = 1,
                    name = "Banshraj Sir",
                    email = "banshraj851212@gmail.com",
                    phone = "9876543212",
                    role = "ADMIN",
                    isBlocked = false,
                    canCreateContent = true,
                    enrolledBatchIds = "1,2,3,4",
                    assignedBatchIds = "",
                    studyHours = 120.0f,
                    solvedDpps = 150,
                    testsAttempted = 25,
                    streakDays = 45
                ),
                UserEntity(
                    id = 2,
                    name = "Rahul Sharma",
                    email = "student@banshraj.edu",
                    phone = "9876543210",
                    role = "STUDENT",
                    isBlocked = false,
                    canCreateContent = false,
                    enrolledBatchIds = "1",
                    assignedBatchIds = "",
                    studyHours = 18.5f,
                    solvedDpps = 34,
                    testsAttempted = 8,
                    streakDays = 14
                ),
                UserEntity(
                    id = 3,
                    name = "Er. Alok Sharma",
                    email = "teacher@banshraj.edu",
                    phone = "9876543211",
                    role = "TEACHER",
                    isBlocked = false,
                    canCreateContent = true,
                    enrolledBatchIds = "1,2",
                    assignedBatchIds = "1,2",
                    studyHours = 45.0f,
                    solvedDpps = 60,
                    testsAttempted = 12,
                    streakDays = 22
                ),
                UserEntity(
                    id = 4,
                    name = "Priya Patel",
                    email = "priya.neet@banshraj.edu",
                    phone = "9876543213",
                    role = "STUDENT",
                    isBlocked = false,
                    canCreateContent = false,
                    enrolledBatchIds = "2",
                    assignedBatchIds = "",
                    studyHours = 12.0f,
                    solvedDpps = 22,
                    testsAttempted = 5,
                    streakDays = 9
                )
            )
            dao.insertUsers(users)

            // Seed Batches (PW inspired)
            val batches = listOf(
                BatchEntity(
                    id = 1,
                    title = "Lakshya JEE 2025",
                    tagline = "Complete Class 12 + JEE Main & Advanced Preparation Batch",
                    category = "JEE",
                    originalPrice = 4999,
                    discountPrice = 2499,
                    instructor = "Banshraj Sir & Top Star Faculty",
                    startDate = "15 Oct 2024",
                    validity = "Till JEE Advanced 2025",
                    isEnrolled = true,
                    rating = 4.9f,
                    badge = "PW Flagship",
                    features = "Daily Live Lectures, DPP with Video Solutions, Weekly All India Tests, Personal Mentorship"
                ),
                BatchEntity(
                    id = 2,
                    title = "Yakeen NEET 2025",
                    tagline = "Ultimate Dropper & 12th Batch for NEET UG 680+ Score",
                    category = "NEET",
                    originalPrice = 4499,
                    discountPrice = 2199,
                    instructor = "Banshraj Sir (Physics) & Medical Experts",
                    startDate = "01 Nov 2024",
                    validity = "Till NEET Exam 2025",
                    isEnrolled = false,
                    rating = 4.9f,
                    badge = "Bestseller",
                    features = "NCERT Line by Line Analysis, 15000+ Question Bank, High Yield DPPs"
                ),
                BatchEntity(
                    id = 3,
                    title = "Arjuna Class 11th JEE",
                    tagline = "Build Rock Solid Foundations for IIT-JEE from Class 11",
                    category = "CLASS 11",
                    originalPrice = 3999,
                    discountPrice = 1999,
                    instructor = "Banshraj Sir & IITian Faculty",
                    startDate = "20 Aug 2024",
                    validity = "2 Years Full Access",
                    isEnrolled = false,
                    rating = 4.8f,
                    badge = "Foundation Star",
                    features = "Mechanics Mastery, Vector Calculus, DPPs with Live Doubt Engine"
                ),
                BatchEntity(
                    id = 4,
                    title = "Udaan 10th Board + NTSE",
                    tagline = "Comprehensive Foundation for Class 10 Board Excellence",
                    category = "FOUNDATION",
                    originalPrice = 2999,
                    discountPrice = 1299,
                    instructor = "Senior Foundation Mentors",
                    startDate = "05 Sep 2024",
                    validity = "Till March 2025",
                    isEnrolled = false,
                    rating = 4.7f,
                    badge = "Top Rated",
                    features = "CBSE Boards Exam Booster, NCERT Solutions, Formula Booklets"
                )
            )
            dao.insertBatches(batches)

            // Seed Lectures for Lakshya JEE & Yakeen NEET
            val lectures = listOf(
                LectureEntity(
                    id = 1,
                    batchId = 1,
                    subject = "Physics",
                    chapter = "Electrostatics",
                    title = "L01: Coulomb's Law & Principle of Superposition",
                    instructor = "Banshraj Sir",
                    duration = "1h 18m",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    isLive = false,
                    isCompleted = true,
                    notesAvailable = true,
                    dppAvailable = true,
                    orderIndex = 1,
                    notesContent = "Coulomb's Law: F = k * |q1 * q2| / r^2 in vacuum. Vector form: F_12 = -F_21. Superposition principle states net electrostatic force on any charge is vector sum of forces exerted by individual charges."
                ),
                LectureEntity(
                    id = 2,
                    batchId = 1,
                    subject = "Physics",
                    chapter = "Electrostatics",
                    title = "L02: Electric Field Lines & Dipole in Uniform Field",
                    instructor = "Banshraj Sir",
                    duration = "1h 25m",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    isLive = false,
                    isCompleted = false,
                    notesAvailable = true,
                    dppAvailable = true,
                    orderIndex = 2,
                    notesContent = "Electric field E = F/q. Dipole moment p = q * 2a. Torque on dipole in uniform field: Tau = p x E. Potential energy U = -p . E."
                ),
                LectureEntity(
                    id = 3,
                    batchId = 1,
                    subject = "Physics",
                    chapter = "Current Electricity",
                    title = "L01: Ohm's Law, Drift Velocity & Kirchhoff's Laws",
                    instructor = "Banshraj Sir",
                    duration = "1h 32m",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    isLive = true,
                    isCompleted = false,
                    notesAvailable = true,
                    dppAvailable = true,
                    orderIndex = 3,
                    notesContent = "Drift velocity v_d = -e*E*tau/m. Current density j = sigma * E = n*e*v_d. KCL: Sum of currents entering node is zero. KVL: Sum of potential drops around loop is zero."
                ),
                LectureEntity(
                    id = 4,
                    batchId = 1,
                    subject = "Chemistry",
                    chapter = "Solid State & Solutions",
                    title = "L01: Raoult's Law & Colligative Properties",
                    instructor = "Dr. Ananya Ray",
                    duration = "1h 10m",
                    videoUrl = "",
                    isLive = false,
                    isCompleted = false,
                    notesAvailable = true,
                    dppAvailable = true,
                    orderIndex = 4,
                    notesContent = "Relative lowering of vapour pressure, Elevation in boiling point (Delta Tb = Kb * m), Depression in freezing point (Delta Tf = Kf * m), Osmotic pressure Pi = CRT."
                ),
                LectureEntity(
                    id = 5,
                    batchId = 1,
                    subject = "Mathematics",
                    chapter = "Calculus & Limits",
                    title = "L01: Standard Limits & L'Hopital's Rule",
                    instructor = "Prof. R. K. Gupta",
                    duration = "1h 15m",
                    videoUrl = "",
                    isLive = false,
                    isCompleted = false,
                    notesAvailable = true,
                    dppAvailable = true,
                    orderIndex = 5,
                    notesContent = "Standard limits: lim (x->0) sin(x)/x = 1, lim (x->0) (e^x - 1)/x = 1. L'Hopital rule applies for 0/0 and inf/inf indeterminate forms."
                )
            )
            dao.insertLectures(lectures)

            // Seed DPPs
            val dpps = listOf(
                DppEntity(
                    id = 1,
                    batchId = 1,
                    lectureId = 1,
                    subject = "Physics",
                    title = "DPP 01: Coulomb's Law & Force Vectors",
                    questionsCount = 10,
                    totalMarks = 40,
                    isSolved = true,
                    score = 36
                ),
                DppEntity(
                    id = 2,
                    batchId = 1,
                    lectureId = 2,
                    subject = "Physics",
                    title = "DPP 02: Electric Dipole & Flux Calculations",
                    questionsCount = 12,
                    totalMarks = 48,
                    isSolved = false,
                    score = 0
                ),
                DppEntity(
                    id = 3,
                    batchId = 1,
                    lectureId = 3,
                    subject = "Physics",
                    title = "DPP 03: Kirchhoff's Mesh & Nodal Analysis",
                    questionsCount = 10,
                    totalMarks = 40,
                    isSolved = false,
                    score = 0
                )
            )
            dao.insertDpps(dpps)

            // Seed Mock Tests with real JEE/NEET physics questions
            val questionsJson = """
            [
              {
                "id": 1,
                "question": "Two equal point charges +q are placed at distance 2d apart. At the midpoint between them, what is the electric field and potential?",
                "options": [
                  "Field is zero, Potential is zero",
                  "Field is zero, Potential is non-zero (2kq/d)",
                  "Field is non-zero, Potential is zero",
                  "Both are non-zero"
                ],
                "correctIndex": 1,
                "explanation": "Due to symmetry, electric fields oppose each other and cancel out: E_net = 0. Potentials are scalar quantities so they add up: V_net = kq/d + kq/d = 2kq/d."
              },
              {
                "id": 2,
                "question": "An electric dipole of moment p is placed in a uniform electric field E. The torque acting on it is maximum when the angle between p and E is:",
                "options": [
                  "0 degrees",
                  "45 degrees",
                  "90 degrees",
                  "180 degrees"
                ],
                "correctIndex": 2,
                "explanation": "Torque tau = p * E * sin(theta). The sine function reaches its maximum value of 1 when theta = 90 degrees."
              },
              {
                "id": 3,
                "question": "If the length of a wire of resistance R is stretched uniformly to twice its original length, its new resistance becomes:",
                "options": [
                  "2 R",
                  "4 R",
                  "R / 2",
                  "R / 4"
                ],
                "correctIndex": 1,
                "explanation": "Since volume is conserved (V = A * L), doubling length (L' = 2L) halves cross-sectional area (A' = A/2). Resistance R' = rho * L' / A' = rho * (2L) / (A/2) = 4R."
              }
            ]
            """.trimIndent()

            val tests = listOf(
                TestEntity(
                    id = 1,
                    batchId = 1,
                    title = "All India Major Test 01 - Electrostatics & Mechanics",
                    subject = "Physics",
                    durationMinutes = 45,
                    totalMarks = 120,
                    questionsJson = questionsJson,
                    isAttempted = false,
                    userScore = 0,
                    rank = 0
                ),
                TestEntity(
                    id = 2,
                    batchId = 1,
                    title = "Weekly Practice Test: Current Electricity & Circuits",
                    subject = "Physics",
                    durationMinutes = 30,
                    totalMarks = 60,
                    questionsJson = questionsJson,
                    isAttempted = true,
                    userScore = 52,
                    rank = 14
                )
            )
            dao.insertTests(tests)

            // Seed Doubts
            val doubts = listOf(
                DoubtEntity(
                    id = 1,
                    studentName = "Rahul Sharma",
                    studentEmail = "rahul.jee@banshraj.edu",
                    subject = "Physics",
                    batchName = "Lakshya JEE 2025",
                    question = "Sir, why is electrostatic field inside a conductor always zero in electrostatic equilibrium?",
                    answer = "Great question! When an external field is applied, free electrons in conductor quickly redistribute to the surface until their induced internal field exactly cancels the external field. Hence net E = 0 inside! - Banshraj Sir",
                    answeredBy = "Banshraj Sir",
                    isAnswered = true,
                    upvotes = 18
                ),
                DoubtEntity(
                    id = 2,
                    studentName = "Priya Patel",
                    studentEmail = "priya.neet@banshraj.edu",
                    subject = "Physics",
                    batchName = "Yakeen NEET 2025",
                    question = "How to easily identify whether to use Nodal Analysis or Mesh Analysis in complex circuit problems?",
                    answer = "Thumb rule: If circuit has many parallel branches with voltage sources, Nodal is much faster! If it has independent single loops with current sources, use Mesh. - Banshraj Sir",
                    answeredBy = "Banshraj Sir",
                    isAnswered = true,
                    upvotes = 24
                ),
                DoubtEntity(
                    id = 3,
                    studentName = "Amit Kumar",
                    studentEmail = "amit@gmail.com",
                    subject = "Chemistry",
                    batchName = "Lakshya JEE 2025",
                    question = "What is the physical significance of Van 't Hoff factor i > 1?",
                    answer = null,
                    answeredBy = null,
                    isAnswered = false,
                    upvotes = 6
                )
            )
            dao.insertDoubts(doubts)

            // Seed Announcements
            val announcements = listOf(
                AnnouncementEntity(
                    id = 1,
                    title = "🚀 Banshraj Academy JEE Marathon Session Tonight at 7:00 PM",
                    content = "Live 4-hour revision of Electrostatics and Current Electricity with 50+ Top PYQs solved live by Banshraj Sir. Keep your formula notes and notebooks ready!",
                    postedBy = "Banshraj Sir",
                    date = "Today",
                    isImportant = true
                ),
                AnnouncementEntity(
                    id = 2,
                    title = "📝 All India Test 01 Syllabus & Guidelines Released",
                    content = "The syllabus covers Full Class 11 Mechanics and Class 12 Electrostatics. Test goes live this Sunday 10:00 AM on the Test Series tab.",
                    postedBy = "Academic Head",
                    date = "Yesterday",
                    isImportant = false
                ),
                AnnouncementEntity(
                    id = 3,
                    title = "✨ New DPP Solutions & Hand-Written Formula Sheets Uploaded",
                    content = "Chapter-wise mind maps and formula booklets for Physics are now accessible under the Classroom -> Notes tab for all enrolled students.",
                    postedBy = "Banshraj Academy Team",
                    date = "2 days ago",
                    isImportant = false
                )
            )
            dao.insertAnnouncements(announcements)

            // Seed Access Requests
            val accessRequests = listOf(
                AccessRequestEntity(
                    id = 1,
                    studentName = "Priya Patel",
                    studentEmail = "priya.neet@banshraj.edu",
                    batchId = 1,
                    batchTitle = "Lakshya JEE 2025",
                    status = "PENDING",
                    requestDate = "Today, 10:15 AM"
                ),
                AccessRequestEntity(
                    id = 2,
                    studentName = "Arjun Sinha",
                    studentEmail = "arjun.sinha@gmail.com",
                    batchId = 2,
                    batchTitle = "Yakeen NEET 2025",
                    status = "APPROVED",
                    requestDate = "Yesterday, 4:20 PM"
                )
            )
            dao.insertAccessRequests(accessRequests)
        }
    }
}
