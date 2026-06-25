package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class AosRepository(private val dao: AosDao) {

    // --- Flows ---
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allDailyLogs: Flow<List<DailyLogEntity>> = dao.getAllDailyLogs()
    val allHabitLogs: Flow<List<HabitLogEntity>> = dao.getAllHabitLogs()
    val allTestResults: Flow<List<TestResultEntity>> = dao.getAllTestResults()
    val allPYQEntries: Flow<List<PYQEntryEntity>> = dao.getAllPYQEntries()
    val allErrorLogs: Flow<List<ErrorLogEntity>> = dao.getAllErrorLogs()
    val allFormulas: Flow<List<FormulaEntity>> = dao.getAllFormulas()
    val allCurrentAffairs: Flow<List<CurrentAffairsEntity>> = dao.getAllCurrentAffairs()
    val allRevisionTasks: Flow<List<RevisionTaskEntity>> = dao.getAllRevisionTasks()
    val allWeeklyMilestones: Flow<List<WeeklyMilestoneEntity>> = dao.getAllWeeklyMilestones()

    // --- Custom methods ---
    fun getHabitsForDate(date: String): Flow<List<HabitLogEntity>> = dao.getHabitsForDateFlow(date)

    fun searchFormulas(query: String): Flow<List<FormulaEntity>> = dao.searchFormulas(query)

    // --- Suspend mutations ---
    suspend fun insertChapter(chapter: ChapterEntity) = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = dao.updateChapter(chapter)

    suspend fun insertDailyLog(log: DailyLogEntity) = dao.insertDailyLog(log)
    suspend fun getDailyLogForDate(date: String): DailyLogEntity? = dao.getDailyLogForDate(date)

    suspend fun insertHabitLog(log: HabitLogEntity) = dao.insertHabitLog(log)
    suspend fun updateHabitLog(log: HabitLogEntity) = dao.updateHabitLog(log)

    suspend fun insertTestResult(result: TestResultEntity) = dao.insertTestResult(result)
    suspend fun deleteTestResult(result: TestResultEntity) = dao.deleteTestResult(result)

    suspend fun insertPYQEntry(entry: PYQEntryEntity) = dao.insertPYQEntry(entry)
    suspend fun updatePYQEntry(entry: PYQEntryEntity) = dao.updatePYQEntry(entry)

    suspend fun insertErrorLog(log: ErrorLogEntity) = dao.insertErrorLog(log)
    suspend fun updateErrorLog(log: ErrorLogEntity) = dao.updateErrorLog(log)
    suspend fun deleteErrorLog(log: ErrorLogEntity) = dao.deleteErrorLog(log)

    suspend fun insertFormula(formula: FormulaEntity) = dao.insertFormula(formula)
    suspend fun deleteFormula(formula: FormulaEntity) = dao.deleteFormula(formula)

    suspend fun insertCurrentAffairs(entry: CurrentAffairsEntity) = dao.insertCurrentAffairs(entry)
    suspend fun deleteCurrentAffairs(entry: CurrentAffairsEntity) = dao.deleteCurrentAffairs(entry)

    suspend fun insertRevisionTask(task: RevisionTaskEntity) = dao.insertRevisionTask(task)
    suspend fun updateRevisionTask(task: RevisionTaskEntity) = dao.updateRevisionTask(task)
    suspend fun deleteRevisionTask(task: RevisionTaskEntity) = dao.deleteRevisionTask(task)

    suspend fun insertWeeklyMilestone(milestone: WeeklyMilestoneEntity) = dao.insertWeeklyMilestone(milestone)
    suspend fun updateWeeklyMilestone(milestone: WeeklyMilestoneEntity) = dao.updateWeeklyMilestone(milestone)

    // --- Database Backup/Restore ---
    suspend fun getAllChaptersDirect() = dao.getAllChapters().firstOrNull() ?: emptyList()
    suspend fun getAllDailyLogsDirect() = dao.getAllDailyLogs().firstOrNull() ?: emptyList()
    suspend fun getAllHabitLogsDirect() = dao.getAllHabitLogs().firstOrNull() ?: emptyList()
    suspend fun getAllTestResultsDirect() = dao.getAllTestResults().firstOrNull() ?: emptyList()
    suspend fun getAllPYQEntriesDirect() = dao.getAllPYQEntries().firstOrNull() ?: emptyList()
    suspend fun getAllErrorLogsDirect() = dao.getAllErrorLogs().firstOrNull() ?: emptyList()
    suspend fun getAllFormulasDirect() = dao.getAllFormulas().firstOrNull() ?: emptyList()
    suspend fun getAllCurrentAffairsDirect() = dao.getAllCurrentAffairs().firstOrNull() ?: emptyList()
    suspend fun getAllRevisionTasksDirect() = dao.getAllRevisionTasks().firstOrNull() ?: emptyList()
    suspend fun getAllWeeklyMilestonesDirect() = dao.getAllWeeklyMilestones().firstOrNull() ?: emptyList()

    suspend fun restoreBackup(
        chapters: List<ChapterEntity>,
        dailyLogs: List<DailyLogEntity>,
        habitLogs: List<HabitLogEntity>,
        testResults: List<TestResultEntity>,
        pyqEntries: List<PYQEntryEntity>,
        errorLogs: List<ErrorLogEntity>,
        formulas: List<FormulaEntity>,
        currentAffairs: List<CurrentAffairsEntity>,
        revisionTasks: List<RevisionTaskEntity>,
        weeklyMilestones: List<WeeklyMilestoneEntity>
    ) {
        if (chapters.isNotEmpty()) dao.insertChapters(chapters)
        for (log in dailyLogs) dao.insertDailyLog(log)
        if (habitLogs.isNotEmpty()) dao.insertHabitLogs(habitLogs)
        for (test in testResults) dao.insertTestResult(test)
        for (pyq in pyqEntries) dao.insertPYQEntry(pyq)
        for (err in errorLogs) dao.insertErrorLog(err)
        if (formulas.isNotEmpty()) dao.insertFormulas(formulas)
        for (ca in currentAffairs) dao.insertCurrentAffairs(ca)
        for (rev in revisionTasks) dao.insertRevisionTask(rev)
        for (mile in weeklyMilestones) dao.insertWeeklyMilestone(mile)
    }

    // --- Seeding Data ---
    suspend fun seedDefaultDataIfNeeded() {
        val currentChapters = dao.getAllChapters().firstOrNull()
        if (currentChapters.isNullOrEmpty()) {
            val list = mutableListOf<ChapterEntity>()

            // --- Class 12 UP Board / JEE (Physics, Chemistry, Maths) ---
            val phyChapters = listOf(
                "Electrostatics", "Current Electricity", "Magnetic Effects of Current",
                "Electromagnetic Induction & AC", "Electromagnetic Waves", "Ray Optics",
                "Wave Optics", "Dual Nature of Matter", "Atoms & Nuclei", "Electronic Devices"
            )
            phyChapters.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "Physics", category = "JEE", chapterName = name, isCompleted = idx < 2, priority = if (idx % 3 == 0) "A" else "B", pyqsSolved = idx * 15, accuracy = 65f + idx * 3, isWeak = idx == 3))
                list.add(ChapterEntity(subject = "Physics", category = "UP_BOARD", chapterName = name, isCompleted = idx < 3, priority = "B", isPracticalCompleted = idx == 0))
            }

            val chemChapters = listOf(
                "Solutions", "Electrochemistry", "Chemical Kinetics", "d & f Block Elements",
                "Coordination Compounds", "Haloalkanes & Haloarenes", "Alcohols Phenols & Ethers",
                "Aldehydes Ketones & Carboxylic Acids", "Amines", "Biomolecules"
            )
            chemChapters.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "Chemistry", category = "JEE", chapterName = name, isCompleted = idx < 2, priority = if (idx % 3 == 1) "A" else "B", pyqsSolved = idx * 12, accuracy = 70f + idx * 2, isWeak = idx == 2))
                list.add(ChapterEntity(subject = "Chemistry", category = "UP_BOARD", chapterName = name, isCompleted = idx < 3, priority = "B", isPracticalCompleted = idx == 0))
            }

            val mathChapters = listOf(
                "Relations & Functions", "Inverse Trigonometric Functions", "Matrices", "Determinants",
                "Continuity & Differentiability", "Application of Derivatives", "Integrals",
                "Application of Integrals", "Differential Equations", "Vector Algebra",
                "Three Dimensional Geometry", "Linear Programming", "Probability"
            )
            mathChapters.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "Mathematics", category = "JEE", chapterName = name, isCompleted = idx < 3, priority = if (idx % 3 == 0) "A" else "B", pyqsSolved = idx * 18, accuracy = 60f + idx * 3, isWeak = idx == 5))
                list.add(ChapterEntity(subject = "Mathematics", category = "UP_BOARD", chapterName = name, isCompleted = idx < 4, priority = "B"))
            }

            // --- Class 11 Bridge Program ---
            val bPhy = listOf("Units & Measurements", "Kinematics", "Laws of Motion", "Work Energy Power", "Rotational Motion", "Gravitation")
            bPhy.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "Physics", category = "BRIDGE_CLASS_11", chapterName = name, isCompleted = idx < 4, priority = if (idx < 2) "A" else "B"))
            }
            val bChem = listOf("Some Basic Concepts", "Structure of Atom", "Classification of Elements", "Chemical Bonding", "Thermodynamics", "Equilibrium")
            bChem.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "Chemistry", category = "BRIDGE_CLASS_11", chapterName = name, isCompleted = idx < 3, priority = if (idx < 2) "A" else "B"))
            }
            val bMath = listOf("Sets", "Relations & Functions", "Trigonometric Functions", "Complex Numbers", "Linear Inequalities", "Permutations & Combinations", "Sequence & Series")
            bMath.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "Mathematics", category = "BRIDGE_CLASS_11", chapterName = name, isCompleted = idx < 4, priority = if (idx < 3) "A" else "B"))
            }

            // --- NDA Modules ---
            val ndaMath = listOf("Algebra", "Matrices & Determinants", "Trigonometry", "Analytical Geometry (2D/3D)", "Differential Calculus", "Integral Calculus", "Vector Algebra", "Statistics & Probability")
            ndaMath.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "NDA Mathematics", category = "NDA_MATHS", chapterName = name, isCompleted = idx < 3, priority = "A", pyqsSolved = idx * 10, accuracy = 65f + idx * 2))
            }

            val ndaGat = listOf("English Grammar", "English Vocabulary", "Physics (GAT)", "Chemistry (GAT)", "General Science", "History & Freedom Movement", "Geography", "Current Affairs")
            ndaGat.forEachIndexed { idx, name ->
                list.add(ChapterEntity(subject = "NDA GAT", category = "NDA_GAT", chapterName = name, isCompleted = idx < 4, priority = "B", pyqsSolved = idx * 8, accuracy = 70f + idx * 2))
            }

            dao.insertChapters(list)

            // --- Formulas ---
            val formulaList = listOf(
                FormulaEntity(subject = "Physics", formula = "F = k * (q1 * q2) / r^2", explanation = "Coulomb's Law: Calculates electrostatic force between two point charges in vacuum. k = 1 / (4πε₀) ≈ 9x10^9 N m^2/C^2.", tags = "Electrostatics,Coulomb,Force"),
                FormulaEntity(subject = "Physics", formula = "∮ E · dA = Q_enclosed / ε₀", explanation = "Gauss's Law: The total electric flux through any closed surface is equal to 1/ε₀ times the net charge enclosed by the surface.", tags = "Electrostatics,Flux,Electric Field"),
                FormulaEntity(subject = "Physics", formula = "V = I * R", explanation = "Ohm's Law: Electric current is directly proportional to voltage across two points, and inversely proportional to resistance.", tags = "Current,Electricity,Ohm"),
                FormulaEntity(subject = "Chemistry", formula = "P_total = x1*P1° + x2*P2°", explanation = "Raoult's Law: The vapor pressure of a solvent above a solution is equal to the vapor pressure of the pure solvent multiplied by its mole fraction in the solution.", tags = "Solutions,Vapor Pressure,Mole Fraction"),
                FormulaEntity(subject = "Chemistry", formula = "E = E° - (RT/nF) * ln(Q)", explanation = "Nernst Equation: Relates the reduction potential of an electrochemical cell to the standard electrode potential, temperature, and activities of the chemical species.", tags = "Electrochemistry,Cell Potential,Nernst"),
                FormulaEntity(subject = "Chemistry", formula = "k = A * e^(-E_a / RT)", explanation = "Arrhenius Equation: Expresses the dependence of the rate constant of a chemical reaction on temperature and activation energy.", tags = "Chemical Kinetics,Rate Constant,Activation Energy"),
                FormulaEntity(subject = "Mathematics", formula = "P(A|B) = [P(B|A) * P(A)] / P(B)", explanation = "Bayes' Theorem: Describes the probability of an event, based on prior knowledge of conditions that might be related to the event.", tags = "Probability,Bayes,Conditional"),
                FormulaEntity(subject = "Mathematics", formula = "d/dx [f(g(x))] = f'(g(x)) * g'(x)", explanation = "Chain Rule: Formula for computing the derivative of the composition of two or more functions.", tags = "Calculus,Differentiation,Chain Rule"),
                FormulaEntity(subject = "Mathematics", formula = "∫ u dv = u*v - ∫ v du", explanation = "Integration by Parts: A rule that transforms the integral of a product of functions into other integrals.", tags = "Calculus,Integration,Parts")
            )
            dao.insertFormulas(formulaList)

            // --- Current Affairs ---
            val currentDayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val caEntries = listOf(
                CurrentAffairsEntity(date = currentDayStr, topic = "India-US Defence Strategic Dialogue 2026", category = "Defense", notes = "Highlights focus on co-production of military hardware and joint cyber-warfare exercises. Crucial for NDA GAT Current Affairs."),
                CurrentAffairsEntity(date = currentDayStr, topic = "ISRO Gaganyaan Mission 2 Launch Prep", category = "Science", notes = "ISRO completes crucial crew-module escape trials. Targets full unmanned flight later this year. NDA/GAT and Board Physics interest."),
                CurrentAffairsEntity(date = currentDayStr, topic = "RCEP Trade Agreement Update 2026", category = "International", notes = "Recent developments in tariff cuts. India remains an observer while strengthening bilateral FTA negotiations.")
            )
            for (ca in caEntries) {
                dao.insertCurrentAffairs(ca)
            }

            // --- Mock Tests ---
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            
            // JEE Mock 1 (15 days ago)
            cal.add(Calendar.DAY_OF_YEAR, -15)
            dao.insertTestResult(TestResultEntity(date = sdf.format(cal.time), examType = "JEE", testName = "JEE Main Full Syllabus - 1", score = 168, maxScore = 300, accuracy = 68f, remarks = "Need to work on speed in Chemistry."))
            
            // JEE Mock 2 (7 days ago)
            cal.add(Calendar.DAY_OF_YEAR, 8)
            dao.insertTestResult(TestResultEntity(date = sdf.format(cal.time), examType = "JEE", testName = "JEE Main Part Test - Electrostatics & Solutions", score = 194, maxScore = 300, accuracy = 74f, remarks = "Improved. Physics was strong. Careless errors in Maths determinants."))
            
            // NDA Mock 1 (10 days ago)
            cal.add(Calendar.DAY_OF_YEAR, -3)
            dao.insertTestResult(TestResultEntity(date = sdf.format(cal.time), examType = "NDA", testName = "NDA GAT Full Test - A", score = 380, maxScore = 600, accuracy = 71f, remarks = "English GAT part was highly scoring. Need revision on Geography section."))

            // Board Mock 1 (5 days ago)
            cal.add(Calendar.DAY_OF_YEAR, 5)
            dao.insertTestResult(TestResultEntity(date = sdf.format(cal.time), examType = "BOARD", testName = "UP Board Physics Paper 1", score = 62, maxScore = 70, accuracy = 88f, remarks = "Excellent writing format. Work on practical log book."))

            // --- Daily logs (for 7 days trend) ---
            val today = Calendar.getInstance()
            val hours = listOf(6.5f, 8.0f, 7.5f, 9.0f, 5.5f, 8.5f, 7.0f)
            val dppStatus = listOf(true, true, false, true, false, true, true)
            val revStatus = listOf(false, true, true, true, false, true, false)
            for (i in 6 downTo 0) {
                val tempCal = Calendar.getInstance()
                tempCal.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = sdf.format(tempCal.time)
                val logIdx = 6 - i
                dao.insertDailyLog(DailyLogEntity(
                    date = dateStr,
                    studyHours = hours[logIdx],
                    attendance = true,
                    dppDone = dppStatus[logIdx],
                    revisionDone = revStatus[logIdx]
                ))

                // Habits for these 7 days
                val habits = listOf("Wake Up Early", "Swimming", "Study Hours Completed", "Revision", "Current Affairs")
                for (h in habits) {
                    val comp = when (h) {
                        "Wake Up Early" -> logIdx % 3 != 0
                        "Swimming" -> logIdx % 2 == 0
                        "Study Hours Completed" -> hours[logIdx] >= 7.0f
                        "Revision" -> revStatus[logIdx]
                        "Current Affairs" -> logIdx % 2 != 0
                        else -> true
                    }
                    dao.insertHabitLog(HabitLogEntity(
                        date = dateStr,
                        habitName = h,
                        isCompleted = comp
                    ))
                }
            }

            // --- PYQ Entries ---
            dao.insertPYQEntry(PYQEntryEntity(examType = "JEE", subject = "Physics", chapterName = "Electrostatics", attempted = 45, remaining = 15, accuracy = 75f))
            dao.insertPYQEntry(PYQEntryEntity(examType = "JEE", subject = "Chemistry", chapterName = "Solutions", attempted = 30, remaining = 20, accuracy = 80f))
            dao.insertPYQEntry(PYQEntryEntity(examType = "NDA", subject = "Mathematics", chapterName = "Algebra", attempted = 60, remaining = 40, accuracy = 68f))
            dao.insertPYQEntry(PYQEntryEntity(examType = "BOARD", subject = "Mathematics", chapterName = "Matrices", attempted = 25, remaining = 5, accuracy = 90f))

            // --- Error Logs ---
            dao.insertErrorLog(ErrorLogEntity(subject = "Physics", chapter = "Electrostatics", errorType = "Formula", description = "Forgot factor of 1/2 in electric field energy density formula.", isSolved = false))
            dao.insertErrorLog(ErrorLogEntity(subject = "Chemistry", chapter = "Solutions", errorType = "Calculation", description = "Calculation mistake in molecular weight determination.", isSolved = true))
            dao.insertErrorLog(ErrorLogEntity(subject = "Mathematics", chapter = "Determinants", errorType = "Careless", description = "Swapped signs of cofactor row elements in 3x3 expansion.", isSolved = false))
            dao.insertErrorLog(ErrorLogEntity(subject = "NDA Mathematics", chapter = "Trigonometry", errorType = "Concept", description = "Applied incorrect identity for cos(2A) expansion under GAT constraints.", isSolved = false))

            // --- Revision Tasks (Spaced Repetition) ---
            val tCal = Calendar.getInstance()
            dao.insertRevisionTask(RevisionTaskEntity(chapterName = "Electrostatics", subject = "Physics", category = "JEE", dueDate = sdf.format(tCal.time), stage = 1, isCompleted = false))
            dao.insertRevisionTask(RevisionTaskEntity(chapterName = "Solutions", subject = "Chemistry", category = "JEE", dueDate = sdf.format(tCal.time), stage = 7, isCompleted = false))
            tCal.add(Calendar.DAY_OF_YEAR, -2) // Overdue
            dao.insertRevisionTask(RevisionTaskEntity(chapterName = "Relations & Functions", subject = "Mathematics", category = "JEE", dueDate = sdf.format(tCal.time), stage = 21, isCompleted = false))
            tCal.add(Calendar.DAY_OF_YEAR, 5) // Future
            dao.insertRevisionTask(RevisionTaskEntity(chapterName = "English Vocabulary", subject = "NDA GAT", category = "NDA_GAT", dueDate = sdf.format(tCal.time), stage = 45, isCompleted = false))

            // --- Weekly Milestones ---
            val mondayCal = Calendar.getInstance()
            mondayCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            dao.insertWeeklyMilestone(WeeklyMilestoneEntity(
                weekStartDate = sdf.format(mondayCal.time),
                physicsFocus = "Complete Electrostatics PYQs & start Current Electricity",
                chemistryFocus = "Solutions final revision & start Electrochemistry",
                mathsFocus = "Matrices & Determinants full practice",
                mockTests = "1 Full Syllabus JEE Test",
                revision = "Class 11 Bridge (Kinematics)",
                isCompleted = false
            ))
        }
    }
}
