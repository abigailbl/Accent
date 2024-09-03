pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // הוסף את זה כדי לוודא שכל ה-plugins יימצאו
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Accent Recognition"
include(":app")