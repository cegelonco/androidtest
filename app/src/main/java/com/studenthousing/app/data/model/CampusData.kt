package com.studenthousing.app.data.model

data class Campus(
    val name: String,
    val lat: Double,
    val lng: Double
)

object CampusData {

    // Universities shown in the main spinner
    // USJ is handled separately with a sub-campus picker
    val universities = listOf(
        "Select University",
        "AUB",
        "LAU Beirut",
        "LAU Byblos",
        "USJ",
        "NDU",
        "USEK",
        "Lebanese University",
        "BAU",
        "AUST",
        "Haigazian University"
    )

    // USJ sub-campuses shown only when USJ is selected
    val usjCampuses = listOf(
        Campus("ESIB / CST – Mansourieh",       33.8654, 35.5642),
        Campus("CSH – Sciences Humaines",        33.8811, 35.5116),
        Campus("CSM – Sciences Médicales",       33.8824, 35.5121),
        Campus("CSS – Sciences Sociales",        33.8901, 35.5097)
    )

    // Coordinates for all non-USJ universities
    val campusCoordinates: Map<String, Campus> = mapOf(
        "AUB"                  to Campus("AUB",                  33.9003, 35.4785),
        "LAU Beirut"           to Campus("LAU Beirut",           33.8938, 35.4953),
        "LAU Byblos"           to Campus("LAU Byblos",           34.1156, 35.6744),
        "NDU"                  to Campus("NDU",                  33.9841, 35.6369),
        "USEK"                 to Campus("USEK",                 33.9822, 35.6089),
        "Lebanese University"  to Campus("Lebanese University",  33.8731, 35.5447),
        "BAU"                  to Campus("BAU",                  33.8833, 35.5167),
        "AUST"                 to Campus("AUST",                 33.9000, 35.5333),
        "Haigazian University" to Campus("Haigazian University", 33.8897, 35.5078)
    )

    // Helper: get Campus object from university name
    // Returns null if "Select University" or USJ (USJ uses sub-campus picker)
    fun getCampus(universityName: String): Campus? {
        return campusCoordinates[universityName]
    }

    // List of majors shown in the signup / profile screen
    val majors = listOf(
        "Select Major",
        "Computer Science",
        "Computer Engineering",
        "Electrical Engineering",
        "Mechanical Engineering",
        "Civil Engineering",
        "Architecture",
        "Business Administration",
        "Finance",
        "Marketing",
        "Economics",
        "Medicine",
        "Pharmacy",
        "Nursing",
        "Dentistry",
        "Law",
        "Psychology",
        "Graphic Design",
        "Communication Arts",
        "Literature",
        "Biology",
        "Chemistry",
        "Mathematics",
        "Physics",
        "Other"
    )
}
